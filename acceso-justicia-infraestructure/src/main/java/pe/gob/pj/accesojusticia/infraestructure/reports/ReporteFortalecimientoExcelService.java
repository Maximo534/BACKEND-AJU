package pe.gob.pj.accesojusticia.infraestructure.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pe.gob.pj.accesojusticia.domain.model.negocio.FortalecimientoCapacidades;
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
public class ReporteFortalecimientoExcelService {

    private final MaeDepartamentoRepository departamentoRepo;
    private final MaeProvinciaRepository provinciaRepo;
    private final MaeDistritoRepository distritoRepo;

    private CellStyle textStyle;
    private CellStyle numberStyle;

    // --- CONFIGURACIÓN DE COLUMNAS (Sin ENLACE) ---
    private static final String[] COL_INFO_EVENTO = {
            "TIPO", "MODALIDAD", "PUBLICO OBJETIVO + observación", "FECHA DE INICIO", "FECHA DE TÉRMINO", "R.A. O AUTORIZACIÓN"
    };

    private static final String[] COL_DATOS_GENERALES = {
            "TEMA: nombre del evento", "DURACIÓN", "N° DE SESIONES", "EXPOSITORES", "INTERPRETE DE LENGUA DE SEÑAS",
            "SE DICTO EN LENGUA NATIVA", "IDIOMA (agregar)", "LUGAR", "DISTRITO", "PROVINCIA", "REGION",
            "INSTITUCIONES ALIADAS", "OBJETIVO (DESCRIPCION DE LA ACTIVIDAD REALIZADA)"
    };

    private static final String[] COL_PART_TIPO = {
            "JUEZAS Y JUECES", "SERVIDORES JURISDICCIONALES Y ADMINISTRATIVOS", "SERVIDORES DE OTRAS INSTITUCIONES PÚBLICAS", "OTROS( PÚBLICO EN GENERAL)"
    };

    private static final String[] COL_PART_GENERO = {"FEMENINO", "MASCULINO", "LGTBIQ+"};

    private static final String[] COL_PART_RANGOS = {
            "- 17 F", "- 17 M", "- 17 LGBTIQ +", "18-29 F", "18-29 M", "18-29 LGBTIQ +", "30-59 F", "30-59 M", "30-59 LGBTIQ +", "60 F", "60 M", "60 LGBTIQ +"
    };

    public byte[] generarExcel(List<FortalecimientoCapacidades> lista) throws Exception {

        // 1. CARGA DE UBIGEO
        Map<Long, String> mapaDep = departamentoRepo.findAll().stream().collect(Collectors.toMap(MaeDepartamentoEntity::getId, MaeDepartamentoEntity::getNombre));
        Map<Long, String> mapaProv = provinciaRepo.findAll().stream().collect(Collectors.toMap(MaeProvinciaEntity::getId, MaeProvinciaEntity::getNombre));
        Map<Long, String> mapaDist = distritoRepo.findAll().stream().collect(Collectors.toMap(MaeDistritoEntity::getId, MaeDistritoEntity::getNombre));

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Fortalecimiento");
            inicializarEstilosBase(workbook);

            // Colores institucionales
            CellStyle stlBlue = crearEstiloCabecera(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle stlRed = crearEstiloCabecera(workbook, IndexedColors.RED.getIndex());
            CellStyle stlTeal = crearEstiloCabecera(workbook, IndexedColors.TEAL.getIndex());

            Row row0 = sheet.createRow(0); row0.setHeightInPoints(25);
            Row row1 = sheet.createRow(1); row1.setHeightInPoints(35);
            Row row2 = sheet.createRow(2); row2.setHeightInPoints(90); // Altura para títulos largos

            int col = 0;

            // --- BLOQUE IDENTIFICACIÓN (AZUL) ---
            col = dibujarCabeceraTriple(sheet, row0, row1, row2, col, "ID", stlBlue);
            col = dibujarCabeceraTriple(sheet, row0, row1, row2, col, "DISTRITO JUDICIAL", stlBlue);

            // --- BLOQUE INFO EVENTO (ROJO) ---
            for (String h : COL_INFO_EVENTO) {
                col = dibujarCabeceraTriple(sheet, row0, row1, row2, col, h, stlRed);
            }

            // --- BLOQUE DATOS GENERALES (ROJO) ---
            int dgCols = COL_DATOS_GENERALES.length;
            // Dibujamos las celdas en row0 y row1 antes de combinar para evitar descuadres en los bordes
            for (int i = 0; i < dgCols; i++) {
                crearCeldaEncabezado(row0, col + i, "DATOS GENERALES DEL EVENTO ACADÉMICO", stlBlue);
                crearCeldaEncabezado(row1, col + i, "DATOS GENERALES DEL EVENTO ACADÉMICO", stlBlue);
            }
            sheet.addMergedRegion(new CellRangeAddress(0, 1, col, col + dgCols - 1));

            for (String h : COL_DATOS_GENERALES) {
                crearCeldaEncabezado(row2, col++, h, stlRed);
            }

            // --- BLOQUE PARTICIPANTES (TURQUESA/ROJO) ---
            int partCols = COL_PART_TIPO.length + 1 + COL_PART_GENERO.length + 1 + 1 + COL_PART_RANGOS.length + 1; // Total: 23 columnas exactas

            // Dibujamos las celdas de las super cabeceras antes de combinar para conservar los bordes THIN
            for (int i = 0; i < partCols; i++) {
                crearCeldaEncabezado(row0, col + i, "ACTIVIDADES ACADÉMICAS: FORTALECIMIENTO DE CAPACIDADES", stlBlue);
                crearCeldaEncabezado(row1, col + i, "DATOS DE LOS PARTICIPANTES", stlTeal);
            }
            sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + partCols - 1));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, col, col + partCols - 1));

            for (String h : COL_PART_TIPO) crearCeldaEncabezado(row2, col++, h, stlRed);
            crearCeldaEncabezado(row2, col++, "TOTAL 1", stlRed);

            for (String h : COL_PART_GENERO) crearCeldaEncabezado(row2, col++, h, stlRed);
            crearCeldaEncabezado(row2, col++, "TOTAL 2", stlRed);

            crearCeldaEncabezado(row2, col++, "PERSONAS CON DISCAPACIDAD", stlRed);

            for (String h : COL_PART_RANGOS) crearCeldaEncabezado(row2, col++, h, stlRed);
            crearCeldaEncabezado(row2, col++, "TOTAL", stlRed);

            int totalCols = col;

            // ==========================================
            // LLENADO DE DATOS
            // ==========================================
            int rowIdx = 3;
            for (FortalecimientoCapacidades fc : lista) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;

                // Ubigeo
                String nomReg = fc.getDepartamentoId() != null ? mapaDep.getOrDefault(fc.getDepartamentoId(), "-") : "-";
                String nomProv = fc.getProvinciaId() != null ? mapaProv.getOrDefault(fc.getProvinciaId(), "-") : "-";
                String nomDist = fc.getDistritoId() != null ? mapaDist.getOrDefault(fc.getDistritoId(), "-") : "-";

                // --- Datos Identificación ---
                crearCelda(row, c++, String.valueOf(fc.getId()), textStyle);
                crearCelda(row, c++, fc.getDistritoJudicialNombre(), textStyle);

                // --- Info Evento ---
                crearCelda(row, c++, fc.getTipoEvento(), textStyle);
                crearCelda(row, c++, fc.getModalidad(), textStyle);
                // NOTA: Se eliminó la celda "ENLACE"
                crearCelda(row, c++, fc.getPublicoObjetivo() + " " + (fc.getPublicoObjetivoDetalle() != null ? fc.getPublicoObjetivoDetalle() : ""), textStyle);
                crearCelda(row, c++, fc.getFechaInicio() != null ? fc.getFechaInicio().toString() : "", textStyle);
                crearCelda(row, c++, fc.getFechaFin() != null ? fc.getFechaFin().toString() : "", textStyle);
                crearCelda(row, c++, fc.getDocumentoAutoriza(), textStyle);

                // --- Datos Generales ---
                crearCelda(row, c++, fc.getNombreEvento(), textStyle);
                crearCeldaNum(row, c++, fc.getDuracionHoras(), numberStyle);
                crearCeldaNum(row, c++, fc.getNumeroSesiones(), numberStyle);
                crearCelda(row, c++, fc.getDocenteExpositor(), textStyle);
                crearCelda(row, c++, fc.getInterpreteSenias(), textStyle);
                crearCelda(row, c++, fc.getSeDictoLenguaNativa(), textStyle);
                crearCelda(row, c++, fc.getLenguaNativaDesc(), textStyle);
                crearCelda(row, c++, fc.getNombreInstitucion(), textStyle); // Lugar
                crearCelda(row, c++, nomDist, textStyle);
                crearCelda(row, c++, nomProv, textStyle);
                crearCelda(row, c++, nomReg, textStyle);
                crearCelda(row, c++, fc.getInstitucionesAliadas(), textStyle);
                crearCelda(row, c++, fc.getDescripcionActividad(), textStyle);

                // --- Participantes (Mapeo por tipo) ---
                int sumTipo = 0;
                for (String tipo : COL_PART_TIPO) {
                    int val = getParticipantesPorTipo(fc, tipo);
                    crearCeldaNum(row, c++, val, numberStyle);
                    sumTipo += val;
                }
                crearCeldaNum(row, c++, sumTipo, numberStyle); // TOTAL 1

                // Genero
                int f = fc.getTotalParticipantesFem() != null ? fc.getTotalParticipantesFem() : 0;
                int m = fc.getTotalParticipantesMas() != null ? fc.getTotalParticipantesMas() : 0;
                int l = fc.getTotalParticipantesLgtbi() != null ? fc.getTotalParticipantesLgtbi() : 0;
                crearCeldaNum(row, c++, f, numberStyle);
                crearCeldaNum(row, c++, m, numberStyle);
                crearCeldaNum(row, c++, l, numberStyle);
                crearCeldaNum(row, c++, f + m + l, numberStyle); // TOTAL 2

                crearCeldaNum(row, c++, fc.getNumeroDiscapacitados(), numberStyle);

                // Rangos de Edad
                int sumRangos = 0;
                for (String rango : COL_PART_RANGOS) {
                    int val = getParticipantesPorRango(fc, rango);
                    crearCeldaNum(row, c++, val, numberStyle);
                    sumRangos += val;
                }
                crearCeldaNum(row, c++, sumRangos, numberStyle); // TOTAL FINAL
            }

            // --- AJUSTES FINALES ---
            sheet.createFreezePane(2, 3);
            for (int i = 0; i < totalCols; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 3500) sheet.setColumnWidth(i, 3500);
                else sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 800);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================
    // LÓGICA DE SUMATORIAS DINÁMICAS
    // =========================================================

    private int getParticipantesPorTipo(FortalecimientoCapacidades fc, String columnaExcel) {
        if (fc.getParticipantes() == null) return 0;
        return fc.getParticipantes().stream()
                .filter(p -> p.getDescripcionTipoParticipante() != null && matchContains(columnaExcel, p.getDescripcionTipoParticipante()))
                .mapToInt(p -> (p.getCantidadFemenino() != null ? p.getCantidadFemenino() : 0) +
                        (p.getCantidadMasculino() != null ? p.getCantidadMasculino() : 0) +
                        (p.getCantidadLgtbiq() != null ? p.getCantidadLgtbiq() : 0))
                .sum();
    }

    private int getParticipantesPorRango(FortalecimientoCapacidades fc, String columnaExcel) {
        if (fc.getParticipantes() == null) return 0;
        return fc.getParticipantes().stream()
                .filter(p -> p.getRangoEdad() != null && matchContains(columnaExcel, p.getRangoEdad()))
                .mapToInt(p -> matchGenero(columnaExcel, p.getCantidadFemenino(), p.getCantidadMasculino(), p.getCantidadLgtbiq()))
                .sum();
    }

    // --- HELPERS DE APOYO Y ESTILOS ---

    private int dibujarCabeceraTriple(Sheet s, Row r0, Row r1, Row r2, int col, String val, CellStyle st) {
        crearCeldaEncabezado(r0, col, val, st);
        crearCeldaEncabezado(r1, col, val, st);
        crearCeldaEncabezado(r2, col, val, st);
        s.addMergedRegion(new CellRangeAddress(0, 2, col, col));
        return col + 1;
    }

    private void inicializarEstilosBase(Workbook wb) {
        // Estilo para texto (Bordes en los 4 lados)
        textStyle = wb.createCellStyle();
        textStyle.setAlignment(HorizontalAlignment.LEFT);
        textStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        textStyle.setBorderBottom(BorderStyle.HAIR);
        textStyle.setBorderTop(BorderStyle.HAIR);
        textStyle.setBorderRight(BorderStyle.HAIR);
        textStyle.setBorderLeft(BorderStyle.HAIR);

        // Estilo para números (Bordes en los 4 lados)
        numberStyle = wb.createCellStyle();
        numberStyle.setAlignment(HorizontalAlignment.CENTER);
        numberStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        numberStyle.setBorderBottom(BorderStyle.HAIR);
        numberStyle.setBorderTop(BorderStyle.HAIR);
        numberStyle.setBorderRight(BorderStyle.HAIR);
        numberStyle.setBorderLeft(BorderStyle.HAIR);
    }

    private CellStyle crearEstiloCabecera(Workbook wb, short color) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        f.setFontHeightInPoints((short) 9);

        CellStyle s = wb.createCellStyle();
        s.setFont(f);
        s.setFillForegroundColor(color);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);

        // Bordes de cabecera más definidos (THIN)
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);

        return s;
    }

    private void crearCeldaEncabezado(Row r, int c, String v, CellStyle s) {
        Cell cell = r.createCell(c);
        cell.setCellValue(v);
        cell.setCellStyle(s);
    }

    private void crearCelda(Row r, int c, String v, CellStyle s) {
        Cell cell = r.createCell(c);
        cell.setCellValue(v != null ? v : "");
        cell.setCellStyle(s);
    }

    private void crearCeldaNum(Row r, int c, Integer v, CellStyle s) {
        Cell cell = r.createCell(c);
        cell.setCellValue(v != null ? v : 0);
        cell.setCellStyle(s);
    }

    private boolean matchContains(String excel, String bd) {
        String t1 = normalizar(excel); String t2 = normalizar(bd);
        return t1.contains(t2) || t2.contains(t1);
    }

    private String normalizar(String input) {
        if (input == null) return "";
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toUpperCase().replaceAll("\\s+", "");
    }

    private int matchGenero(String col, Integer f, Integer m, Integer lg) {
        String n = col.toUpperCase().replaceAll("\\s+", "");
        if (n.contains("LGBTIQ")) return lg != null ? lg : 0;
        if (n.endsWith("F")) return f != null ? f : 0;
        if (n.endsWith("M")) return m != null ? m : 0;
        return 0;
    }
}