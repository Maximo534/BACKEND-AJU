package pe.gob.pj.accesojusticia.infraestructure.reports;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pe.gob.pj.accesojusticia.domain.model.negocio.PromocionCultura;
import pe.gob.pj.accesojusticia.domain.model.negocio.PromocionCultura.DetalleBeneficiada;
import pe.gob.pj.accesojusticia.domain.model.negocio.PromocionCultura.DetalleTarea;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Service
public class ReportePromocionCulturaExcelService {

    // Estilos globales
    private CellStyle headerStyle;
    private CellStyle numberStyle;
    private CellStyle textStyle;

    public byte[] generarExcel(List<PromocionCultura> lista) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            inicializarEstilos(workbook);

            // Generamos las 3 pestañas
            generarHojaResumen(workbook, lista);
            generarHojaParticipantes(workbook, lista);
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
    // 1. HOJA RESUMEN
    // ==========================================
    private void generarHojaResumen(Workbook workbook, List<PromocionCultura> lista) {
        Sheet sheet = workbook.createSheet("Resumen Promoción");
        Row row0 = sheet.createRow(0);
        Row row1 = sheet.createRow(1);

        int c = 0;

        // --- COLUMNAS GENERALES ---
        String[] headersGen = {"N°", "CÓDIGO", "DISTRITO JUDICIAL", "FECHA INICIO", "AUTORIDAD", "LUGAR"};
        for (String h : headersGen) {
            crearCeldaEncabezado(row0, c, h, headerStyle);
            crearCeldaEncabezado(row1, c, h, headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 1, c, c));
            c++;
        }

        // --- GRUPO: TOTAL PARTICIPANTES ---
        String[] subHeadersPart = {"FEMENINO", "MASCULINO", "LGTBIQ+", "TOTAL"};
        crearGrupo(sheet, row0, row1, c, "TOTAL PARTICIPANTES", subHeadersPart, headerStyle);
        c += subHeadersPart.length;

        // --- COLUMNA: TOTAL TAREAS ---
        crearCeldaEncabezado(row0, c, "TOTAL TAREAS", headerStyle);
        crearCeldaEncabezado(row1, c, "TOTAL TAREAS", headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, c, c));
        c++;

        // --- LLENADO DE DATOS ---
        int rowIdx = 2;
        for (PromocionCultura item : lista) {
            Row row = sheet.createRow(rowIdx++);
            int col = 0;
            // Datos Generales
            crearCelda(row, col++, String.valueOf(rowIdx - 2), textStyle);
            crearCelda(row, col++, item.getCodigo(), textStyle);
            crearCelda(row, col++, item.getDistritoJudicialNombre(), textStyle);
            crearCelda(row, col++, item.getFechaInicio() != null ? item.getFechaInicio().toString() : "", textStyle);
            crearCelda(row, col++, item.getNombreActividad(), textStyle); // Usé nombre actividad/autoridad según convenga
            crearCelda(row, col++, item.getLugarActividad(), textStyle);

            // Totales Participantes
            int f = item.getTotalParticipantesFem() != null ? item.getTotalParticipantesFem() : 0;
            int m = item.getTotalParticipantesMas() != null ? item.getTotalParticipantesMas() : 0;
            int l = item.getTotalParticipantesLgtbi() != null ? item.getTotalParticipantesLgtbi() : 0;

            crearCeldaNum(row, col++, f, numberStyle);
            crearCeldaNum(row, col++, m, numberStyle);
            crearCeldaNum(row, col++, l, numberStyle);
            crearCeldaNum(row, col++, (f + m + l), numberStyle); // Gran Total

            // Total Tareas
            crearCeldaNum(row, col++, item.getCantidadTareas(), numberStyle);
        }

        autoSize(sheet, c);
        // Filtro automático
        sheet.setAutoFilter(new CellRangeAddress(1, rowIdx - 1, 0, c - 1));
    }

    // ==========================================
    // 2. HOJA DETALLE PARTICIPANTES
    // ==========================================
    private void generarHojaParticipantes(Workbook workbook, List<PromocionCultura> lista) {
        Sheet sheet = workbook.createSheet("Detalle Participantes");
        String[] headers = {"CÓDIGO EVENTO", "DISTRITO JUDICIAL", "DESCRIPCIÓN RANGO", "CÓDIGO RANGO", "FEMENINO", "MASCULINO", "LGTBIQ+"};

        crearFilaCabeceraSimple(sheet, headers);

        int rowIdx = 1;
        for (PromocionCultura padre : lista) {
            if (padre.getPersonasBeneficiadas() != null) {
                for (DetalleBeneficiada hija : padre.getPersonasBeneficiadas()) {
                    Row row = sheet.createRow(rowIdx++);
                    int c = 0;
                    // Datos Padre Repetidos
                    crearCelda(row, c++, padre.getCodigo(), textStyle);
                    crearCelda(row, c++, padre.getDistritoJudicialNombre(), textStyle);

                    // Datos Hija
                    crearCelda(row, c++, hija.getDescripcionRango(), textStyle);
                    crearCelda(row, c++, hija.getCodigoRango(), textStyle);
                    crearCeldaNum(row, c++, hija.getCantidadFemenino(), numberStyle);
                    crearCeldaNum(row, c++, hija.getCantidadMasculino(), numberStyle);
                    crearCeldaNum(row, c++, hija.getCantidadLgtbiq(), numberStyle);
                }
            }
        }
        autoSize(sheet, headers.length);
        sheet.setAutoFilter(new CellRangeAddress(0, rowIdx - 1, 0, headers.length - 1));
    }

    // ==========================================
    // 3. HOJA DETALLE TAREAS
    // ==========================================
    private void generarHojaTareas(Workbook workbook, List<PromocionCultura> lista) {
        Sheet sheet = workbook.createSheet("Detalle Tareas");
        String[] headers = {"CÓDIGO EVENTO", "DISTRITO JUDICIAL", "TAREA REALIZADA", "FECHA INICIO"};

        crearFilaCabeceraSimple(sheet, headers);

        int rowIdx = 1;
        for (PromocionCultura padre : lista) {
            if (padre.getTareasRealizadas() != null) {
                for (DetalleTarea hija : padre.getTareasRealizadas()) {
                    Row row = sheet.createRow(rowIdx++);
                    int c = 0;
                    crearCelda(row, c++, padre.getCodigo(), textStyle);
                    crearCelda(row, c++, padre.getDistritoJudicialNombre(), textStyle);

                    // Descripción Tarea (Ya mapeada en el Mapper)
                    crearCelda(row, c++, hija.getDescripcion(), textStyle);
                    crearCelda(row, c++, hija.getFechaInicio() != null ? hija.getFechaInicio().toString() : "", textStyle);
                }
            }
        }
        autoSize(sheet, headers.length);
        sheet.setAutoFilter(new CellRangeAddress(0, rowIdx - 1, 0, headers.length - 1));
    }

    // --- HELPERS (Reutilizables) ---
    private void crearGrupo(Sheet sheet, Row row0, Row row1, int startCol, String tituloGrupo, String[] subtitulos, CellStyle style) {
        crearCeldaEncabezado(row0, startCol, tituloGrupo, style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, startCol, startCol + subtitulos.length - 1));
        for (int i = 0; i < subtitulos.length; i++) {
            crearCeldaEncabezado(row1, startCol + i, subtitulos[i], style);
        }
    }

    private void crearFilaCabeceraSimple(Sheet sheet, String[] headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            crearCeldaEncabezado(row, i, headers[i], headerStyle);
        }
    }

    private void crearCeldaEncabezado(Row row, int col, String val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val);
        cell.setCellStyle(style);
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