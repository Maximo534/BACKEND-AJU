package pe.gob.pj.prueba.infraestructure.reports;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
// Importa tus clases hijas (DetalleAtendida, etc.) si están en clases internas o externas
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante.DetalleBeneficiada;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante.DetalleAtendida;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante.DetalleCaso;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante.DetalleTarea;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Service
public class ReporteJusticiaItineranteExcelService {

    // Estilos globales
    private CellStyle headerStyle;
    private CellStyle numberStyle;
    private CellStyle textStyle;

    public byte[] generarExcelListado(List<JusticiaItinerante> lista) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            inicializarEstilos(workbook);

            generarHojaResumen(workbook, lista);
            generarHojaBeneficiadas(workbook, lista);
            generarHojaAtendidas(workbook, lista);
            generarHojaCasos(workbook, lista);
            generarHojaTareas(workbook, lista);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void inicializarEstilos(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short) 10);

        headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setWrapText(true);

        numberStyle = workbook.createCellStyle();
        numberStyle.setAlignment(HorizontalAlignment.CENTER);
        numberStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        textStyle = workbook.createCellStyle();
        textStyle.setAlignment(HorizontalAlignment.LEFT);
        textStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    // ==========================================
    // HOJA 1: RESUMEN (TOTALES)
    // ==========================================
    private void generarHojaResumen(Workbook workbook, List<JusticiaItinerante> lista) {
        Sheet sheet = workbook.createSheet("Resumen Eventos");
        Row row0 = sheet.createRow(0);
        Row row1 = sheet.createRow(1);

        int c = 0;

        // --- DATOS GENERALES ---
        String[] headersGen = {"N°", "CÓDIGO", "DISTRITO JUDICIAL", "FECHA INICIO", "LUGAR"};
        for (String h : headersGen) {
            crearCeldaEncabezado(row0, c, h, headerStyle);
            crearCeldaEncabezado(row1, c, h, headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, c, c));
            c++;
        }

        // --- GRUPO: TOTAL PERSONAS (Beneficiados + Atendidos) ---
        // Puedes agruparlos o dejarlos sueltos. Aquí ejemplo suelto pero usando 2 filas
        crearCeldaEncabezado(row0, c, "TOTAL BENEFICIADOS", headerStyle);
        crearCeldaEncabezado(row1, c, "TOTAL BENEFICIADOS", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, c, c));
        c++;

        crearCeldaEncabezado(row0, c, "TOTAL ATENDIDOS", headerStyle);
        crearCeldaEncabezado(row1, c, "TOTAL ATENDIDOS", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, c, c));
        c++;

        // --- GRUPO: TOTAL CASOS (NUEVO AGRUPADOR) ---
        String[] subHeadersCasos = {"DEMANDAS", "AUDIENCIAS", "SENTENCIAS", "PROCESOS", "NOTIFIC.", "ORIENTAC."};
        crearGrupo(sheet, row0, row1, c, "TOTAL CASOS", subHeadersCasos, headerStyle);
        c += subHeadersCasos.length;

        // --- COLUMNA: TOTAL TAREAS (NUEVO) ---
        crearCeldaEncabezado(row0, c, "TOTAL TAREAS", headerStyle);
        crearCeldaEncabezado(row1, c, "TOTAL TAREAS", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, c, c));
        c++;

        // --- LLENADO ---
        int rowIdx = 2;
        for (JusticiaItinerante item : lista) {
            Row row = sheet.createRow(rowIdx++);
            int col = 0;
            crearCelda(row, col++, String.valueOf(rowIdx - 2), textStyle);
            crearCelda(row, col++, item.getCodigo(), textStyle);
            crearCelda(row, col++, item.getDistritoJudicialNombre(), textStyle);
            crearCelda(row, col++, item.getFechaInicio() != null ? item.getFechaInicio().toString() : "", textStyle);
            crearCelda(row, col++, item.getLugarActividad(), textStyle);

            // Totales Personas
            int totBen = (item.getTotalBeneficiadosFem() + item.getTotalBeneficiadosMas() + item.getTotalBeneficiadosLgtbi());
            int totAte = (item.getTotalAtendidosFem() + item.getTotalAtendidosMas() + item.getTotalAtendidosLgtbi());
            crearCeldaNum(row, col++, totBen, numberStyle);
            crearCeldaNum(row, col++, totAte, numberStyle);

            // Totales Casos
            crearCeldaNum(row, col++, item.getTotalDemandas(), numberStyle);
            crearCeldaNum(row, col++, item.getTotalAudiencias(), numberStyle);
            crearCeldaNum(row, col++, item.getTotalSentencias(), numberStyle);
            crearCeldaNum(row, col++, item.getTotalProcesos(), numberStyle);
            crearCeldaNum(row, col++, item.getTotalNotificaciones(), numberStyle);
            crearCeldaNum(row, col++, item.getTotalOrientaciones(), numberStyle);

            // Total Tareas
            crearCeldaNum(row, col++, item.getCantidadTareas(), numberStyle);
        }

        // --- AUTOAJUSTE Y FILTRO ---
        autoSize(sheet, c);
        // Habilitar filtro en la fila de encabezados (Fila índice 1 que es la inferior)
        sheet.setAutoFilter(new CellRangeAddress(1, rowIdx - 1, 0, c - 1));
    }

    // ==========================================
    // 2. HOJA ATENDIDAS (Con Descripción Vulnerabilidad)
    // ==========================================
    private void generarHojaAtendidas(Workbook workbook, List<JusticiaItinerante> lista) {
        Sheet sheet = workbook.createSheet("Detalle Atendidas");
        // Cambiamos "ID VULNERABILIDAD" por "TIPO VULNERABILIDAD" (Nombre)
        String[] headers = {"CÓDIGO EVENTO", "DISTRITO JUDICIAL", "TIPO VULNERABILIDAD", "RANGO EDAD", "FEMENINO", "MASCULINO", "LGTBIQ+"};

        crearFilaCabeceraSimple(sheet, headers);

        int rowIdx = 1;
        for (JusticiaItinerante padre : lista) {
            if (padre.getPersonasAtendidas() != null) {
                for (DetalleAtendida hija : padre.getPersonasAtendidas()) {
                    Row row = sheet.createRow(rowIdx++);
                    int c = 0;
                    crearCelda(row, c++, padre.getCodigo(), textStyle);
                    crearCelda(row, c++, padre.getDistritoJudicialNombre(), textStyle);
                    // Usamos la DESCRIPCIÓN en lugar del ID
                    crearCelda(row, c++, hija.getDescripcionVulnerabilidad(), textStyle);
                    crearCelda(row, c++, hija.getRangoEdad(), textStyle);
                    crearCeldaNum(row, c++, hija.getCantFemenino(), numberStyle);
                    crearCeldaNum(row, c++, hija.getCantMasculino(), numberStyle);
                    crearCeldaNum(row, c++, hija.getCantLgtbiq(), numberStyle);
                }
            }
        }
        autoSize(sheet, headers.length);
        sheet.setAutoFilter(new CellRangeAddress(0, rowIdx - 1, 0, headers.length - 1));
    }

    // ==========================================
    // 3. HOJA CASOS (Con Descripción Materia)
    // ==========================================
    private void generarHojaCasos(Workbook workbook, List<JusticiaItinerante> lista) {
        Sheet sheet = workbook.createSheet("Detalle Casos");
        String[] headers = {"CÓDIGO EVENTO", "DISTRITO JUDICIAL", "MATERIA", "DEMANDAS", "AUDIENCIAS", "SENTENCIAS", "PROCESOS", "NOTIFICACIONES", "ORIENTACIONES"};

        crearFilaCabeceraSimple(sheet, headers);

        int rowIdx = 1;
        for (JusticiaItinerante padre : lista) {
            if (padre.getCasosAtendidos() != null) {
                for (DetalleCaso hija : padre.getCasosAtendidos()) {
                    Row row = sheet.createRow(rowIdx++);
                    int c = 0;
                    crearCelda(row, c++, padre.getCodigo(), textStyle);
                    crearCelda(row, c++, padre.getDistritoJudicialNombre(), textStyle);
                    // Usamos la DESCRIPCIÓN MATERIA
                    crearCelda(row, c++, hija.getDescripcionMateria(), textStyle);

                    crearCeldaNum(row, c++, hija.getNumDemandas(), numberStyle);
                    crearCeldaNum(row, c++, hija.getNumAudiencias(), numberStyle);
                    crearCeldaNum(row, c++, hija.getNumSentencias(), numberStyle);
                    crearCeldaNum(row, c++, hija.getNumProcesos(), numberStyle);
                    crearCeldaNum(row, c++, hija.getNumNotificaciones(), numberStyle);
                    crearCeldaNum(row, c++, hija.getNumOrientaciones(), numberStyle);
                }
            }
        }
        autoSize(sheet, headers.length);
        sheet.setAutoFilter(new CellRangeAddress(0, rowIdx - 1, 0, headers.length - 1));
    }

    // ==========================================
    // 4. HOJA TAREAS (Con Descripción Tarea)
    // ==========================================
    private void generarHojaTareas(Workbook workbook, List<JusticiaItinerante> lista) {
        Sheet sheet = workbook.createSheet("Detalle Tareas");
        String[] headers = {"CÓDIGO EVENTO", "DISTRITO JUDICIAL", "TAREA REALIZADA", "FECHA INICIO"};

        crearFilaCabeceraSimple(sheet, headers);

        int rowIdx = 1;
        for (JusticiaItinerante padre : lista) {
            if (padre.getTareasRealizadas() != null) {
                for (DetalleTarea hija : padre.getTareasRealizadas()) {
                    Row row = sheet.createRow(rowIdx++);
                    int c = 0;
                    crearCelda(row, c++, padre.getCodigo(), textStyle);
                    crearCelda(row, c++, padre.getDistritoJudicialNombre(), textStyle);
                    // Usamos la DESCRIPCIÓN TAREA
                    crearCelda(row, c++, hija.getDescripcionTarea(), textStyle);
                    crearCelda(row, c++, hija.getFechaInicio() != null ? hija.getFechaInicio().toString() : "", textStyle);
                }
            }
        }
        autoSize(sheet, headers.length);
        sheet.setAutoFilter(new CellRangeAddress(0, rowIdx - 1, 0, headers.length - 1));
    }

    // ==========================================
    // 5. HOJA BENEFICIADAS (Sin cambios mayores, solo filtro)
    // ==========================================
    private void generarHojaBeneficiadas(Workbook workbook, List<JusticiaItinerante> lista) {
        Sheet sheet = workbook.createSheet("Detalle Beneficiadas");
        String[] headers = {"CÓDIGO EVENTO", "DISTRITO JUDICIAL", "FECHA", "DESCRIPCIÓN RANGO", "CÓDIGO RANGO", "FEMENINO", "MASCULINO", "LGTBIQ+"};

        crearFilaCabeceraSimple(sheet, headers);
        // ... (Logica de llenado igual que antes) ...
        int rowIdx = 1;
        for (JusticiaItinerante padre : lista) {
            if (padre.getPersonasBeneficiadas() != null) {
                for (DetalleBeneficiada hija : padre.getPersonasBeneficiadas()) {
                    Row row = sheet.createRow(rowIdx++);
                    int c = 0;
                    crearCelda(row, c++, padre.getCodigo(), textStyle);
                    crearCelda(row, c++, padre.getDistritoJudicialNombre(), textStyle);
                    crearCelda(row, c++, padre.getFechaInicio().toString(), textStyle);
                    crearCelda(row, c++, hija.getDescripcionRango(), textStyle);
                    crearCelda(row, c++, hija.getCodigoRango(), textStyle);
                    crearCeldaNum(row, c++, hija.getCantFemenino(), numberStyle);
                    crearCeldaNum(row, c++, hija.getCantMasculino(), numberStyle);
                    crearCeldaNum(row, c++, hija.getCantLgtbiq(), numberStyle);
                }
            }
        }
        autoSize(sheet, headers.length);
        sheet.setAutoFilter(new CellRangeAddress(0, rowIdx - 1, 0, headers.length - 1));
    }
    private void crearGrupo(Sheet sheet, Row row0, Row row1, int startCol, String tituloGrupo, String[] subtitulos, CellStyle style) {
        crearCeldaEncabezado(row0, startCol, tituloGrupo, style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, startCol, startCol + subtitulos.length - 1));
        for (int i = 0; i < subtitulos.length; i++) {
            crearCeldaEncabezado(row1, startCol + i, subtitulos[i], style);
        }
    }

    private void crearCeldaEncabezado(Row row, int col, String val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val);
        cell.setCellStyle(style);
    }

    // --- UTILS ---
    private void crearFilaCabeceraSimple(Sheet sheet, String[] headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            crearCelda(row, i, headers[i], headerStyle);
        }
    }

    private void crearCelda(Row row, int col, String val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val != null ? val : "");
        cell.setCellStyle(style);
    }

    private void crearCeldaNum(Row row, int col, Integer val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val != null ? val : 0);
        cell.setCellStyle(style);
    }

    private void autoSize(Sheet sheet, int cols) {
        for(int i=0; i<cols; i++) sheet.autoSizeColumn(i);
    }
}