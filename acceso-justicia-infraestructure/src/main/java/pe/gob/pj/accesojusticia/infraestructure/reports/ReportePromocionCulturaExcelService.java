package pe.gob.pj.accesojusticia.infraestructure.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pe.gob.pj.accesojusticia.domain.model.negocio.PromocionCultura;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeDepartamentoEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeDistritoEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeProvinciaEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters.MaeDepartamentoRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters.MaeDistritoRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters.MaeProvinciaRepository;

import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportePromocionCulturaExcelService {

    // Repositorios para consulta rápida de Ubigeo
    private final MaeDepartamentoRepository departamentoRepo;
    private final MaeProvinciaRepository provinciaRepo;
    private final MaeDistritoRepository distritoRepo;

    private CellStyle textStyle;
    private CellStyle numberStyle;

    // ==========================================
    // DEFINICIÓN DE PLANTILLA ESTÁTICA (Sin campos faltantes)
    // ==========================================
    private static final String[] COL_GENERALES = {
            "ID", "DISTRITO JUDICIAL", "NOMBRE DE LA CAMPAÑA DE PROMOCIÓN DE CULTURA JURÍDICA",
            "PÚBLICO", "PÚBLICO ESPECÍFICO", "R.A", "FECHA DE INICIO", "FECHA FIN", "MODALIDAD",
            "ZONA DE INTERVENCIÓN", "DISTRITO", "PROVINCIA", "REGIÓN", "TEMA",
            "¿SE ATENDIÓ EN LENGUA ORIGINARIA?", "LENGUA ORIGINARIA DEL SERVICIO",
            "INSTITUCIONES ALIADAS", "OBSERVACIONES"
    };

    private static final String[] COL_BEN_RANGOS = {
            "-17 F", "-17 M", "-17 LGBTIQ +", "18-59 F", "18-59 M", "18-59 LGBTIQ+", "60 F", "60 M", "60 LGBTIQ+"
    };

    public byte[] generarExcel(List<PromocionCultura> lista) throws Exception {

        // --- CARGA RÁPIDA DE UBIGEO EN MEMORIA ---
        Map<Long, String> mapaDep = departamentoRepo.findAll().stream().collect(Collectors.toMap(MaeDepartamentoEntity::getId, MaeDepartamentoEntity::getNombre));
        Map<Long, String> mapaProv = provinciaRepo.findAll().stream().collect(Collectors.toMap(MaeProvinciaEntity::getId, MaeProvinciaEntity::getNombre));
        Map<Long, String> mapaDist = distritoRepo.findAll().stream().collect(Collectors.toMap(MaeDistritoEntity::getId, MaeDistritoEntity::getNombre));

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte General");

            // Estilos
            textStyle = crearEstiloDato(workbook, false);
            numberStyle = crearEstiloDato(workbook, true);
            CellStyle stlBlue = crearEstiloCabecera(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle stlOrange = crearEstiloCabecera(workbook, IndexedColors.CORAL.getIndex());
            CellStyle stlPurple = crearEstiloCabecera(workbook, IndexedColors.VIOLET.getIndex());

            Row row0 = sheet.createRow(0);
            row0.setHeightInPoints(25); // Altura para Super-cabeceras

            Row row1 = sheet.createRow(1);
            row1.setHeightInPoints(45); // Altura para que los textos largos respiren

            int col = 0;

            // ==========================================
            // 1. DIBUJAR CABECERAS
            // ==========================================

            // --- BLOQUE AZUL: DATOS GENERALES ---
            for (int i = 0; i < COL_GENERALES.length; i++) {
                if (i >= 2) {
                    if (i == 2) {
                        crearCeldaEncabezado(row0, col, "CAMPAÑAS DE PROMOCIÓN DE CULTURA JURÍDICA", stlBlue);
                        // Combinamos dinámicamente según la cantidad de columnas restantes (length - 3)
                        sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + (COL_GENERALES.length - 3)));
                    }
                    crearCeldaEncabezado(row1, col, COL_GENERALES[i], stlBlue);
                } else {
                    // ID y Distrito Judicial se combinan verticalmente
                    crearCeldaEncabezado(row0, col, COL_GENERALES[i], stlBlue);
                    crearCeldaEncabezado(row1, col, COL_GENERALES[i], stlBlue);
                    sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col));
                }
                col++;
            }

            // --- BLOQUE NARANJA: BENEFICIARIAS ---
            crearCeldaEncabezado(row0, col, "PERSONAS BENEFICIARIAS", stlOrange);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + 13));

            String[] benHeaders = {"F", "M", "LGTBIQ+", "TOTALES"};
            for (String bh : benHeaders) crearCeldaEncabezado(row1, col++, bh, stlOrange);
            for (String br : COL_BEN_RANGOS) crearCeldaEncabezado(row1, col++, br, stlOrange);
            crearCeldaEncabezado(row1, col++, "TOTALES", stlOrange);

            // --- BLOQUE MORADO: ATENDIDAS ---
            crearCeldaEncabezado(row0, col, "PERSONAS ATENDIDAS", stlPurple);
            crearCeldaEncabezado(row1, col++, "N° DE ATENCIONES", stlPurple);

            int totalCols = col;

            // ==========================================
            // 2. LLENADO DE DATOS
            // ==========================================
            int rowIdx = 2;
            for (PromocionCultura pc : lista) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;

                String nomReg = pc.getDepartamentoId() != null ? mapaDep.getOrDefault(pc.getDepartamentoId(), "-") : "-";
                String nomProv = pc.getProvinciaId() != null ? mapaProv.getOrDefault(pc.getProvinciaId(), "-") : "-";
                String nomDist = pc.getDistritoGeograficoId() != null ? mapaDist.getOrDefault(pc.getDistritoGeograficoId(), "-") : "-";

                // --- Generales (Sin los campos que faltan en BD) ---
                crearCelda(row, c++, String.valueOf(pc.getId()), textStyle);
                crearCelda(row, c++, pc.getDistritoJudicialNombre(), textStyle);
                crearCelda(row, c++, pc.getNombreActividad(), textStyle);
                crearCelda(row, c++, pc.getPublicoObjetivo(), textStyle);
                crearCelda(row, c++, pc.getPublicoObjetivoOtros(), textStyle);
                crearCelda(row, c++, pc.getResolucionAdminPlan(), textStyle);
                crearCelda(row, c++, pc.getFechaInicio() != null ? pc.getFechaInicio().toString() : "", textStyle);
                crearCelda(row, c++, pc.getFechaFin() != null ? pc.getFechaFin().toString() : "", textStyle);
                crearCelda(row, c++, pc.getModalidadProyecto(), textStyle);
                crearCelda(row, c++, pc.getZonaIntervencion(), textStyle);
                crearCelda(row, c++, nomDist, textStyle);
                crearCelda(row, c++, nomProv, textStyle);
                crearCelda(row, c++, nomReg, textStyle);
                crearCelda(row, c++, pc.getDescripcionActividad(), textStyle); // Mapeado a TEMA
                crearCelda(row, c++, pc.getSeDictoLenguaNativa(), textStyle);
                crearCelda(row, c++, pc.getLenguaNativa(), textStyle);
                crearCelda(row, c++, pc.getInstitucionesAliadas(), textStyle);
                crearCelda(row, c++, pc.getObservacion(), textStyle);

                // --- Beneficiarias ---
                int benF = getBenTotal(pc, "F");
                int benM = getBenTotal(pc, "M");
                int benLg = getBenTotal(pc, "LGTBIQ");
                crearCeldaNum(row, c++, benF, numberStyle);
                crearCeldaNum(row, c++, benM, numberStyle);
                crearCeldaNum(row, c++, benLg, numberStyle);
                crearCeldaNum(row, c++, benF + benM + benLg, numberStyle); // TOTALES

                int sumBenRangos = 0;
                for (String rango : COL_BEN_RANGOS) {
                    int val = getBenRangoExacto(pc, rango);
                    crearCeldaNum(row, c++, val, numberStyle);
                    sumBenRangos += val;
                }
                crearCeldaNum(row, c++, sumBenRangos, numberStyle); // TOTALES RANGOS

                // --- Atendidas (Asumimos el total de beneficiarias) ---
                crearCeldaNum(row, c++, benF + benM + benLg, numberStyle);
            }

            // ==========================================
            // AJUSTES FINALES Y TAMAÑOS
            // ==========================================
            sheet.createFreezePane(2, 2); // Congela ID, Distrito y las 2 filas de cabecera

            for (int i = 0; i < totalCols; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth < 3500) {
                    sheet.setColumnWidth(i, 3500); // Ancho mínimo para que no se apachurre
                } else {
                    sheet.setColumnWidth(i, currentWidth + 800); // Padding extra
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================
    // MATCHERS INTELIGENTES (Para rangos de edad)
    // =========================================================

    private int getBenTotal(PromocionCultura pc, String genero) {
        if (pc.getPersonasBeneficiadas() == null) return 0;
        return pc.getPersonasBeneficiadas().stream().mapToInt(b ->
                genero.equals("F") ? (b.getCantidadFemenino() != null ? b.getCantidadFemenino() : 0) :
                        genero.equals("M") ? (b.getCantidadMasculino() != null ? b.getCantidadMasculino() : 0) :
                                (b.getCantidadLgtbiq() != null ? b.getCantidadLgtbiq() : 0)).sum();
    }

    private int getBenRangoExacto(PromocionCultura pc, String columna) {
        if (pc.getPersonasBeneficiadas() == null) return 0;
        return pc.getPersonasBeneficiadas().stream()
                .filter(b -> b.getDescripcionRango() != null && matchContains(columna, b.getDescripcionRango()))
                .mapToInt(b -> matchGenero(columna, b.getCantidadFemenino(), b.getCantidadMasculino(), b.getCantidadLgtbiq())).sum();
    }

    private boolean matchContains(String textoExcel, String textoBD) {
        String t1 = normalizar(textoExcel);
        String t2 = normalizar(textoBD);
        return t1.contains(t2) || t2.contains(t1);
    }

    private String normalizar(String input) {
        if (input == null) return "";
        String limpio = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return limpio.toUpperCase().replaceAll("\\s+", "");
    }

    private int matchGenero(String columna, Integer f, Integer m, Integer lg) {
        String colNorm = columna.toUpperCase().replaceAll("\\s+", "");
        if (colNorm.contains("LGBTIQ") || colNorm.contains("LGTBIQ")) return lg != null ? lg : 0;
        if (columna.toUpperCase().endsWith("F") || columna.contains(" F ")) return f != null ? f : 0;
        if (columna.toUpperCase().endsWith("M") || columna.contains(" M ")) return m != null ? m : 0;
        return 0;
    }

    // =========================================================
    // UTILS ESTILOS POI
    // =========================================================

    private CellStyle crearEstiloCabecera(Workbook workbook, short bgColorIndex) {
        Font font = workbook.createFont(); font.setBold(true); font.setColor(IndexedColors.WHITE.getIndex()); font.setFontHeightInPoints((short) 9);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font); style.setFillForegroundColor(bgColorIndex); style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER); style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN); style.setBorderTop(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle crearEstiloDato(Workbook workbook, boolean isNumber) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(isNumber ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT); style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.HAIR); style.setBorderTop(BorderStyle.HAIR); style.setBorderRight(BorderStyle.HAIR); style.setBorderLeft(BorderStyle.HAIR);
        return style;
    }

    private void crearCeldaEncabezado(Row row, int col, String val, CellStyle style) {
        Cell cell = row.createCell(col); cell.setCellValue(val); cell.setCellStyle(style);
    }

    private void crearCelda(Row row, int col, String val, CellStyle style) {
        Cell cell = row.createCell(col); cell.setCellValue(val != null ? val : ""); cell.setCellStyle(style);
    }

    private void crearCeldaNum(Row row, int col, Integer val, CellStyle style) {
        Cell cell = row.createCell(col); cell.setCellValue(val != null ? val : 0); cell.setCellStyle(style);
    }
}