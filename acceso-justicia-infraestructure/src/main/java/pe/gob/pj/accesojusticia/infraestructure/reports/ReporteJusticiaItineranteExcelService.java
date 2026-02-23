package pe.gob.pj.accesojusticia.infraestructure.reports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pe.gob.pj.accesojusticia.domain.model.negocio.JusticiaItinerante;
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
public class ReporteJusticiaItineranteExcelService {

    // Inyección de repositorios maestras para el Ubigeo
    private final MaeDepartamentoRepository departamentoRepo;
    private final MaeProvinciaRepository provinciaRepo;
    private final MaeDistritoRepository distritoRepo;

    private CellStyle textStyle;
    private CellStyle numberStyle;

    // ==========================================
    // DEFINICIÓN DE PLANTILLA ESTÁTICA
    // ==========================================
    private static final String[] COL_GENERALES = {
            "ID", "DISTRITO JUDICIAL", "RESOLUCIÓN QUE AUTORIZA", "PÚBLICO OBJETIVO", "FECHA DE INICIO",
            "FECHA DE TÉRMINO", "LUGAR: CP-COMUNIDAD-AAHH-BARRIO", "DISTRITO", "PROVINCIA", "REGIÓN",
            "CONVENIOS PAIS", "NOMBRE DE TAMBO", "OBSERVACIONES", "¿SE ATENDIÓ EN LENGUA ORIGINARIA?",
            "LENGUA ORIGINARIA", "INSTITUCIONES ALIADAS", "OBSERVACIONES ", "N° DE MESAS DE PARTES INSTALADAS",
            "N° DE JUECES QUE BRINDARON EL SERVICIO", "N° DE SERVIDORES QUE BRINDARON EL SERVICIO"
    };

    private static final String[] COL_BEN_RANGOS = {"-17 F", "-17 M", "-17 LGBTIQ+", "18-59 F", "18-59 M", "18-59 LGBTIQ+", "60 F", "60 M", "60 LGBTIQ+"};

    private static final String[] COL_ATE_VULN = {"SITUACIÓN DE POBREZA", "PERSONA CON DISCAPACIDAD", "POBLACIÓN INDÍGENA, NATIVA, AFRODESCENDIENTE", "PRIVADOS DE LIBERTAD", "MIGRANTES"};
    private static final String[] COL_ATE_EDAD = {"- 17 F", "- 17 M", "- 17 LGBTIQ +", "18-29 F", "18-29 M", "18-29 LGBTIQ +", "30-59 F", "30-59 M", "30-59 LGBTIQ +", "60 F", "60 M", "60 LGBTIQ +"};

    private static final String[] COL_CASOS_ESP = {"Familia Civil", "Familia Penal", "Familia Tutelar", "Civil", "Constitucional", "Laboral", "Penal", "Otro"};
    private static final String[] COL_CASOS_SUB = {
            "Pensión de Alimentos", "Ejecución de acta de conciliación extrajudicial de alimentos", "Demanda acumulada de filiación y alimentos",
            "Aumento de pensión de alimentos", "Reconocimiento judicial de paternidad extramatrimonial", "Rectificación judicial de partidas de nacimiento",
            "Rectificación judicial de partidas de matrimonio", "Rectificación judicial de partidas de defunción", "Designación de apoyos y salvaguardias para personas con discapacidad",
            "Declaración judicial de ausencia por desaparición forzada 1980-2000", "Violencia contra la mujer e integrantes del grupo familiar",
            "Omisión a la asistencia familiar", "Desnaturalización de contrato laboral", "Pago de beneficios o bonificaciones laborales", "Otro(especificar)"
    };

    private static final String[] COL_RESUMEN = {"Demandas", "Audiencias", "Sentencias", "Ejecucion", "Notificaciones", "Orientaciones", "TOTALES"};

    public byte[] generarExcelListado(List<JusticiaItinerante> lista) throws Exception {

        // --- 1. CARGA RÁPIDA DE UBIGEO EN MEMORIA (Evita Problema N+1) ---
        Map<Long, String> mapaDep = departamentoRepo.findAll().stream()
                .collect(Collectors.toMap(MaeDepartamentoEntity::getId, MaeDepartamentoEntity::getNombre));

        Map<Long, String> mapaProv = provinciaRepo.findAll().stream()
                .collect(Collectors.toMap(MaeProvinciaEntity::getId, MaeProvinciaEntity::getNombre));

        Map<Long, String> mapaDist = distritoRepo.findAll().stream()
                .collect(Collectors.toMap(MaeDistritoEntity::getId, MaeDistritoEntity::getNombre));

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte General");

            // 2. Inicializar Estilos
            textStyle = crearEstiloDato(workbook, false);
            numberStyle = crearEstiloDato(workbook, true);

            CellStyle stlBlue = crearEstiloCabecera(workbook, IndexedColors.DARK_BLUE.getIndex());
            CellStyle stlOrange = crearEstiloCabecera(workbook, IndexedColors.CORAL.getIndex());
            CellStyle stlPurple = crearEstiloCabecera(workbook, IndexedColors.VIOLET.getIndex()); // Corregido a VIOLET

            Row row0 = sheet.createRow(0);
            row0.setHeightInPoints(30); // Da espacio a las super-cabeceras

            Row row1 = sheet.createRow(1);
            row1.setHeightInPoints(45); // Da espacio al nivel medio

            Row row2 = sheet.createRow(2);
            row2.setHeightInPoints(100); // Altura grande para las materias largas

            int col = 0;

            // ==========================================
            // DIBUJAR CABECERAS
            // ==========================================

            // --- BLOQUE GENERALES ---
            for (int i = 0; i < COL_GENERALES.length; i++) {
                if (i >= 8) { // Desde PROVINCIA hasta el final hay una Super Cabecera
                    if (i == 8) {
                        crearCeldaEncabezado(row0, col, "SERVICIO DE JUSTICIA ITINERANTE", stlBlue);
                        sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + 11));
                    }
                    crearCeldaEncabezado(row1, col, COL_GENERALES[i], stlBlue);
                    crearCeldaEncabezado(row2, col, COL_GENERALES[i], stlBlue);
                    sheet.addMergedRegion(new CellRangeAddress(1, 2, col, col));
                } else {
                    crearCeldaEncabezado(row0, col, COL_GENERALES[i], stlBlue);
                    crearCeldaEncabezado(row1, col, COL_GENERALES[i], stlBlue);
                    crearCeldaEncabezado(row2, col, COL_GENERALES[i], stlBlue);
                    sheet.addMergedRegion(new CellRangeAddress(0, 2, col, col));
                }
                col++;
            }

            // --- BLOQUE BENEFICIARIAS ---
            crearCeldaEncabezado(row0, col, "PERSONAS BENEFICIARIAS", stlOrange);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + 13));
            String[] benHeaders = {"F", "M", "LGTBIQ+", "TOTAL1"};
            for (String bh : benHeaders) col = dibujarSubCabecera2Niveles(sheet, row1, row2, col, bh, stlOrange);
            for (String br : COL_BEN_RANGOS) col = dibujarSubCabecera2Niveles(sheet, row1, row2, col, br, stlOrange);
            col = dibujarSubCabecera2Niveles(sheet, row1, row2, col, "TOTAL2", stlOrange);

            // --- BLOQUE ATENDIDAS ---
            crearCeldaEncabezado(row0, col, "PERSONAS ATENDIDAS", stlPurple);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + 22));

            crearCeldaEncabezado(row1, col, "TIPO DE VULNERABILIDAD", stlPurple);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, col, col + 5));
            for (String v : COL_ATE_VULN) crearCeldaEncabezado(row2, col++, v, stlPurple);
            crearCeldaEncabezado(row2, col++, "TOTALES 1", stlPurple);

            crearCeldaEncabezado(row1, col, "GÉNERO", stlPurple);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, col, col + 3));
            String[] genHeaders = {"F", "M", "LGTBIQ +", "TOTALES 2"};
            for (String g : genHeaders) crearCeldaEncabezado(row2, col++, g, stlPurple);

            crearCeldaEncabezado(row1, col, "RANGO DE EDAD", stlPurple);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, col, col + 12));
            for (String r : COL_ATE_EDAD) crearCeldaEncabezado(row2, col++, r, stlPurple);
            crearCeldaEncabezado(row2, col++, "Totales3", stlPurple);

            // --- BLOQUES DE CASOS (23 columnas cada uno) ---
            col = dibujarBloqueCasos(sheet, row0, row1, row2, col, "DEMANDAS", workbook, IndexedColors.DARK_RED.getIndex(), IndexedColors.ROSE.getIndex(), IndexedColors.RED.getIndex());
            col = dibujarBloqueCasos(sheet, row0, row1, row2, col, "AUDIENCIAS", workbook, IndexedColors.GREEN.getIndex(), IndexedColors.LIGHT_GREEN.getIndex(), IndexedColors.SEA_GREEN.getIndex());
            col = dibujarBloqueCasos(sheet, row0, row1, row2, col, "SENTENCIAS", workbook, IndexedColors.DARK_BLUE.getIndex(), IndexedColors.PALE_BLUE.getIndex(), IndexedColors.BLUE_GREY.getIndex());
            col = dibujarBloqueCasos(sheet, row0, row1, row2, col, "EJECUCION DE SENTENCIAS", workbook, IndexedColors.BROWN.getIndex(), IndexedColors.TAN.getIndex(), IndexedColors.MAROON.getIndex());
            col = dibujarBloqueCasos(sheet, row0, row1, row2, col, "NOTIFICACIONES", workbook, IndexedColors.PLUM.getIndex(), IndexedColors.LAVENDER.getIndex(), IndexedColors.ORCHID.getIndex());
            col = dibujarBloqueCasos(sheet, row0, row1, row2, col, "ORIENTACIONES", workbook, IndexedColors.LIGHT_GREEN.getIndex(), IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex(), IndexedColors.LIME.getIndex());

            // --- BLOQUE RESUMEN ---
            crearCeldaEncabezado(row0, col, "RESUMEN DE DEMANDAS,AUDIENCIAS,SENTENCIAS Y OTROS", stlOrange);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, col, col + 5));
            for (int i = 0; i < COL_RESUMEN.length; i++) {
                crearCeldaEncabezado(row1, col, COL_RESUMEN[i], stlOrange);
                crearCeldaEncabezado(row2, col, COL_RESUMEN[i], stlOrange);
                sheet.addMergedRegion(new CellRangeAddress(1, 2, col, col));
                col++;
            }

            int totalCols = col;

            // ==========================================
            // LLENADO DE DATOS
            // ==========================================
            int rowIdx = 3;
            for (JusticiaItinerante ji : lista) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;

                // Lógica para Región, Provincia, Distrito
                String nomReg = ji.getDepartamentoId() != null ? mapaDep.getOrDefault(ji.getDepartamentoId(), "-") : "-";
                String nomProv = ji.getProvinciaId() != null ? mapaProv.getOrDefault(ji.getProvinciaId(), "-") : "-";
                String nomDist = ji.getDistritoId() != null ? mapaDist.getOrDefault(ji.getDistritoId(), "-") : "-";

                // Lógica de Convenios Pais basada en Tambo
                String tieneConvenio = (ji.getTambo() != null && !ji.getTambo().trim().isEmpty()) ? "SI" : "NO";

                // --- Generales ---
                crearCelda(row, c++, String.valueOf(ji.getId()), textStyle);
                crearCelda(row, c++, ji.getDistritoJudicialNombre(), textStyle);
                crearCelda(row, c++, ji.getDocumentoAutoriza(), textStyle);
                crearCelda(row, c++, ji.getPublicoObjetivo(), textStyle);
                crearCelda(row, c++, ji.getFechaInicio() != null ? ji.getFechaInicio().toString() : "", textStyle);
                crearCelda(row, c++, ji.getFechaFin() != null ? ji.getFechaFin().toString() : "", textStyle);
                crearCelda(row, c++, ji.getLugarActividad(), textStyle);
                crearCelda(row, c++, nomDist, textStyle); // Distrito mapeado
                crearCelda(row, c++, nomProv, textStyle); // Provincia mapeada
                crearCelda(row, c++, nomReg, textStyle); // Región mapeada
                crearCelda(row, c++, tieneConvenio, textStyle); // Convenios Pais (SI/NO)
                crearCelda(row, c++, ji.getTambo(), textStyle);
                crearCelda(row, c++, ji.getObservaciones(), textStyle);
                crearCelda(row, c++, ji.getCodigoSaeLenguaNativa(), textStyle);
                crearCelda(row, c++, ji.getLenguaNativa(), textStyle);
                crearCelda(row, c++, ji.getInstitucionesAliadas(), textStyle);
                crearCelda(row, c++, ji.getObservaciones(), textStyle);
                crearCeldaNum(row, c++, ji.getNumMesasInstaladas(), numberStyle);
                crearCeldaNum(row, c++, ji.getNumJueces(), numberStyle);
                crearCeldaNum(row, c++, ji.getNumServidores(), numberStyle);

                // --- Beneficiarias ---
                int benF = getBenTotal(ji, "F"); int benM = getBenTotal(ji, "M"); int benLg = getBenTotal(ji, "LGTBIQ");
                crearCeldaNum(row, c++, benF, numberStyle);
                crearCeldaNum(row, c++, benM, numberStyle);
                crearCeldaNum(row, c++, benLg, numberStyle);
                crearCeldaNum(row, c++, benF + benM + benLg, numberStyle); // TOTAL1

                int sumBenRangos = 0;
                for (String rango : COL_BEN_RANGOS) {
                    int val = getBenRangoExacto(ji, rango);
                    crearCeldaNum(row, c++, val, numberStyle);
                    sumBenRangos += val;
                }
                crearCeldaNum(row, c++, sumBenRangos, numberStyle); // TOTAL2

                // --- Atendidas ---
                int sumVuln = 0;
                for (String vuln : COL_ATE_VULN) {
                    int val = getAteVulnExacto(ji, vuln);
                    crearCeldaNum(row, c++, val, numberStyle);
                    sumVuln += val;
                }
                crearCeldaNum(row, c++, sumVuln, numberStyle); // TOTALES 1

                int ateF = getAteGenExacto(ji, "F"); int ateM = getAteGenExacto(ji, "M"); int ateLg = getAteGenExacto(ji, "LGTBIQ");
                crearCeldaNum(row, c++, ateF, numberStyle);
                crearCeldaNum(row, c++, ateM, numberStyle);
                crearCeldaNum(row, c++, ateLg, numberStyle);
                crearCeldaNum(row, c++, ateF + ateM + ateLg, numberStyle); // TOTALES 2

                int sumAteEdades = 0;
                for (String edad : COL_ATE_EDAD) {
                    int val = getAteEdadExacto(ji, edad);
                    crearCeldaNum(row, c++, val, numberStyle);
                    sumAteEdades += val;
                }
                crearCeldaNum(row, c++, sumAteEdades, numberStyle); // Totales 3

                // --- Casos Atendidos ---
                c = llenarBloqueCasos(row, c, ji, "DEMANDAS", numberStyle);
                c = llenarBloqueCasos(row, c, ji, "AUDIENCIAS", numberStyle);
                c = llenarBloqueCasos(row, c, ji, "SENTENCIAS", numberStyle);
                c = llenarBloqueCasos(row, c, ji, "PROCESOS", numberStyle); // Ejecución = Procesos
                c = llenarBloqueCasos(row, c, ji, "NOTIFICACIONES", numberStyle);
                c = llenarBloqueCasos(row, c, ji, "ORIENTACIONES", numberStyle);

                // --- Resumen Final ---
                crearCeldaNum(row, c++, ji.getTotalDemandas(), numberStyle);
                crearCeldaNum(row, c++, ji.getTotalAudiencias(), numberStyle);
                crearCeldaNum(row, c++, ji.getTotalSentencias(), numberStyle);
                crearCeldaNum(row, c++, ji.getTotalProcesos(), numberStyle); // Ejecucion
                crearCeldaNum(row, c++, ji.getTotalNotificaciones(), numberStyle);
                crearCeldaNum(row, c++, ji.getTotalOrientaciones(), numberStyle);

                int superTotal = (ji.getTotalDemandas() != null ? ji.getTotalDemandas() : 0) +
                        (ji.getTotalAudiencias() != null ? ji.getTotalAudiencias() : 0) +
                        (ji.getTotalSentencias() != null ? ji.getTotalSentencias() : 0) +
                        (ji.getTotalProcesos() != null ? ji.getTotalProcesos() : 0) +
                        (ji.getTotalNotificaciones() != null ? ji.getTotalNotificaciones() : 0) +
                        (ji.getTotalOrientaciones() != null ? ji.getTotalOrientaciones() : 0);
                crearCeldaNum(row, c++, superTotal, numberStyle); // TOTALES
            }

            // ==========================================
            // AJUSTES FINALES
            // ==========================================
            sheet.createFreezePane(2, 3); // Congela ID, Distrito Judicial y las 3 filas de cabecera
            for (int i = 0; i < totalCols; i++) {
                sheet.autoSizeColumn(i); // El autoajuste por defecto de POI
                int currentWidth = sheet.getColumnWidth(i);

                // Si la columna quedó muy angosta (menos de ~10 caracteres), la forzamos a ser más ancha
                if (currentWidth < 3200) {
                    sheet.setColumnWidth(i, 3200);
                } else {
                    // A las columnas grandes les damos un poquito de padding (respiro) extra
                    sheet.setColumnWidth(i, currentWidth + 800);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // =========================================================
    // MÉTODOS CREADORES DE BLOQUES
    // =========================================================

    private int dibujarSubCabecera2Niveles(Sheet sheet, Row r1, Row r2, int col, String titulo, CellStyle style) {
        crearCeldaEncabezado(r1, col, titulo, style);
        crearCeldaEncabezado(r2, col, titulo, style);
        sheet.addMergedRegion(new CellRangeAddress(1, 2, col, col));
        return col + 1;
    }

    private int dibujarBloqueCasos(Sheet sheet, Row r0, Row r1, Row r2, int startCol, String titulo, Workbook wb, short cSup, short cEsp, short cSub) {
        CellStyle stSuper = crearEstiloCabecera(wb, cSup);
        CellStyle stEsp = crearEstiloCabecera(wb, cEsp);
        CellStyle stSub = crearEstiloCabecera(wb, cSub);

        crearCeldaEncabezado(r0, startCol, titulo, stSuper);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, startCol, startCol + COL_CASOS_ESP.length + COL_CASOS_SUB.length - 1));

        crearCeldaEncabezado(r1, startCol, "Especialidad", stSuper);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, startCol, startCol + COL_CASOS_ESP.length - 1));
        for (int i = 0; i < COL_CASOS_ESP.length; i++) {
            crearCeldaEncabezado(r2, startCol + i, COL_CASOS_ESP[i], stEsp);
        }

        int subStart = startCol + COL_CASOS_ESP.length;
        crearCeldaEncabezado(r1, subStart, "Sub Especialidad/Materia", stSuper);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, subStart, subStart + COL_CASOS_SUB.length - 1));
        for (int i = 0; i < COL_CASOS_SUB.length; i++) {
            crearCeldaEncabezado(r2, subStart + i, COL_CASOS_SUB[i], stSub);
        }
        return startCol + COL_CASOS_ESP.length + COL_CASOS_SUB.length;
    }

    private int llenarBloqueCasos(Row row, int startCol, JusticiaItinerante ji, String tipoMetrica, CellStyle style) {
        for (String esp : COL_CASOS_ESP) {
            crearCeldaNum(row, startCol++, getValorCaso(ji, esp, tipoMetrica), style);
        }
        for (String sub : COL_CASOS_SUB) {
            crearCeldaNum(row, startCol++, getValorCaso(ji, sub, tipoMetrica), style);
        }
        return startCol;
    }

    // =========================================================
    // MATCHERS INTELIGENTES (Para relacionar texto de BD con Excel)
    // =========================================================

    private int getBenTotal(JusticiaItinerante ji, String genero) {
        if (ji.getPersonasBeneficiadas() == null) return 0;
        return ji.getPersonasBeneficiadas().stream().mapToInt(b ->
                genero.equals("F") ? (b.getCantFemenino() != null ? b.getCantFemenino() : 0) :
                        genero.equals("M") ? (b.getCantMasculino() != null ? b.getCantMasculino() : 0) :
                                (b.getCantLgtbiq() != null ? b.getCantLgtbiq() : 0)).sum();
    }

    private int getBenRangoExacto(JusticiaItinerante ji, String columna) {
        if (ji.getPersonasBeneficiadas() == null) return 0;
        return ji.getPersonasBeneficiadas().stream()
                .filter(b -> b.getDescripcionRango() != null && matchContains(columna, b.getDescripcionRango()))
                .mapToInt(b -> matchGenero(columna, b.getCantFemenino(), b.getCantMasculino(), b.getCantLgtbiq())).sum();
    }

    private int getAteVulnExacto(JusticiaItinerante ji, String vulnerabilidad) {
        if (ji.getPersonasAtendidas() == null) return 0;
        return ji.getPersonasAtendidas().stream()
                .filter(a -> a.getDescripcionVulnerabilidad() != null && matchContains(a.getDescripcionVulnerabilidad(), vulnerabilidad))
                .mapToInt(a -> (a.getCantFemenino()!=null?a.getCantFemenino():0) + (a.getCantMasculino()!=null?a.getCantMasculino():0) + (a.getCantLgtbiq()!=null?a.getCantLgtbiq():0)).sum();
    }

    private int getAteGenExacto(JusticiaItinerante ji, String genero) {
        if (ji.getPersonasAtendidas() == null) return 0;
        return ji.getPersonasAtendidas().stream().mapToInt(a ->
                genero.equals("F") ? (a.getCantFemenino()!=null?a.getCantFemenino():0) :
                        genero.equals("M") ? (a.getCantMasculino()!=null?a.getCantMasculino():0) :
                                (a.getCantLgtbiq()!=null?a.getCantLgtbiq():0)).sum();
    }

    private int getAteEdadExacto(JusticiaItinerante ji, String columna) {
        if (ji.getPersonasAtendidas() == null) return 0;
        return ji.getPersonasAtendidas().stream()
                .filter(a -> a.getRangoEdad() != null && matchContains(columna, a.getRangoEdad()))
                .mapToInt(a -> matchGenero(columna, a.getCantFemenino(), a.getCantMasculino(), a.getCantLgtbiq())).sum();
    }

    private int getValorCaso(JusticiaItinerante ji, String nombreColumna, String tipoMetrica) {
        if (ji.getCasosAtendidos() == null) return 0;
        return ji.getCasosAtendidos().stream()
                .filter(c -> c.getDescripcionMateria() != null && matchContains(c.getDescripcionMateria(), nombreColumna))
                .mapToInt(c -> {
                    switch (tipoMetrica) {
                        case "DEMANDAS": return c.getNumDemandas() != null ? c.getNumDemandas() : 0;
                        case "AUDIENCIAS": return c.getNumAudiencias() != null ? c.getNumAudiencias() : 0;
                        case "SENTENCIAS": return c.getNumSentencias() != null ? c.getNumSentencias() : 0;
                        case "PROCESOS": return c.getNumProcesos() != null ? c.getNumProcesos() : 0; // Mapeado a Ejecución
                        case "NOTIFICACIONES": return c.getNumNotificaciones() != null ? c.getNumNotificaciones() : 0;
                        case "ORIENTACIONES": return c.getNumOrientaciones() != null ? c.getNumOrientaciones() : 0;
                        default: return 0;
                    }
                }).sum();
    }

    // --- Helpers de Normalización y Match ---
    private boolean matchContains(String textoExcel, String textoBD) {
        String t1 = normalizar(textoExcel);
        String t2 = normalizar(textoBD);
        return t1.contains(t2) || t2.contains(t1);
    }

    private String normalizar(String input) {
        if (input == null) return "";
        // Quita acentos, convierte a mayúsculas y quita espacios
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