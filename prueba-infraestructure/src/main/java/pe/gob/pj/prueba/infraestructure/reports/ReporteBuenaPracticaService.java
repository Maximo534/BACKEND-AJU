package pe.gob.pj.prueba.infraestructure.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.port.persistence.negocio.BuenaPracticaPersistencePort;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteBuenaPracticaService {

    // Inyectamos el puerto de persistencia para buscar la data por ID
    private final BuenaPracticaPersistencePort persistencePort;

    // Constantes de Estilo
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_VALUE = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, Color.WHITE);
    private static final Color COLOR_HEADER_BG = new Color(145, 16, 3);
    private static final Color COLOR_LABEL_BG = new Color(240, 240, 240);

    // ✅ MÉTODO PÚBLICO QUE RECIBE EL ID (Como en JI)
    public byte[] generarFichaBuenaPractica(String id) throws Exception {
        // 1. Buscamos la data aquí mismo
        BuenaPractica bp = persistencePort.buscarPorId(id);

        if (bp == null) {
            throw new Exception("No se encontró información para la Buena Práctica con ID: " + id);
        }

        // 2. Generamos el PDF con la data encontrada
        return crearDocumentoPdf(bp);
    }

    // --- LÓGICA PRIVADA DE GENERACIÓN PDF ---
    private byte[] crearDocumentoPdf(BuenaPractica bp) throws Exception {
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();
            agregarEncabezado(document);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{25f, 75f});
            table.setSpacingBefore(10f);

            // Bloque I
            addSectionTitle(table, "I. DATOS GENERALES");
            addRow(table, "CÓDIGO:", bp.getId());
            addRow(table, "TÍTULO:", bp.getTitulo());
            addRow(table, "RESPONSABLE:", bp.getResponsable());
            addRow(table, "CATEGORÍA:", bp.getCategoria());
            addRow(table, "FECHA INICIO:", formatearFecha(bp));

            // Bloque II
            addSectionTitle(table, "II. ANÁLISIS DE LA REALIDAD");
            addRow(table, "PROBLEMA:", bp.getProblema());
            addRow(table, "CAUSA:", bp.getCausa());
            addRow(table, "CONSECUENCIA:", bp.getConsecuencia());
            addRow(table, "DESCRIPCIÓN:", bp.getDescripcionGeneral());

            // Bloque III
            addSectionTitle(table, "III. PLANIFICACIÓN");
            addRow(table, "LOGRO:", bp.getLogro());
            addRow(table, "OBJETIVO:", bp.getObjetivo());
            addRow(table, "ALIADO:", bp.getAliado());
            addRow(table, "DIFICULTAD:", bp.getDificultad());
            addRow(table, "NORMA:", bp.getNorma());

            // Bloque IV
            addSectionTitle(table, "IV. EJECUCIÓN");
            addRow(table, "DESARROLLO:", bp.getDesarrollo());
            addRow(table, "EJECUCIÓN:", bp.getEjecucion());
            addRow(table, "ACTIVIDAD:", bp.getActividad());

            // Bloque V
            addSectionTitle(table, "V. RESULTADOS E IMPACTO");
            addRow(table, "APORTE:", bp.getAporte());
            addRow(table, "RESULTADO:", bp.getResultado());
            addRow(table, "IMPACTO:", bp.getImpacto());
            addRow(table, "PÚBLICO OBJ.:", bp.getPublicoObjetivo());
            addRow(table, "LECCIONES:", bp.getLeccionAprendida());
            addRow(table, "INFO ADICIONAL:", bp.getInfoAdicional());

            document.add(table);
            agregarFirma(document, bp);

            document.close();
            return out.toByteArray();

        } catch (DocumentException e) {
            log.error("Error PDF", e);
            throw new Exception("Error al construir el PDF");
        }
    }

    // --- HELPERS ---
    private void agregarEncabezado(Document document) throws DocumentException {
        try {
            Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.JPG"));
            logo.scaleToFit(500, 60);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
        } catch (Exception e) {
            Paragraph p = new Paragraph("PODER JUDICIAL DEL PERÚ", FONT_TITULO);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);
        }
        document.add(Chunk.NEWLINE);
        Paragraph t = new Paragraph("BUENAS PRÁCTICAS\n(INFORME TÉCNICO)", FONT_TITULO);
        t.setAlignment(Element.ALIGN_CENTER);
        document.add(t);
        document.add(Chunk.NEWLINE);
    }

    private void agregarFirma(Document document, BuenaPractica bp) throws DocumentException {
        document.add(Chunk.NEWLINE);
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(
                "Registrado por: " + (bp.getUsuarioRegistro() != null ? bp.getUsuarioRegistro() : "SISTEMA") + "\n" +
                        "Fecha: " + java.time.LocalDate.now(), FONT_VALUE));
        cell.setBorder(Rectangle.TOP);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderWidthTop(0.5f);
        cell.setBorderColorTop(Color.GRAY);
        // Quitamos los otros bordes
        cell.setBorderWidthBottom(0);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);

        tabla.addCell(cell);
        document.add(tabla);
    }

    private void addSectionTitle(PdfPTable table, String title) {
        PdfPCell cell = new PdfPCell(new Phrase(title, FONT_SECTION));
        cell.setColspan(2);
        cell.setBackgroundColor(COLOR_HEADER_BG);
        cell.setPadding(5f);
        cell.setBorderColor(Color.WHITE);
        table.addCell(cell);
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label, FONT_LABEL));
        l.setBackgroundColor(COLOR_LABEL_BG);
        l.setPadding(5f);
        table.addCell(l);

        PdfPCell v = new PdfPCell(new Phrase((value != null && !value.isBlank()) ? value : "-", FONT_VALUE));
        v.setPadding(5f);
        table.addCell(v);
    }

    private String formatearFecha(BuenaPractica bp) {
        return (bp.getFechaInicio() != null) ?
                bp.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-";
    }

    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase("Página " + writer.getPageNumber(), FONT_VALUE),
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }
}