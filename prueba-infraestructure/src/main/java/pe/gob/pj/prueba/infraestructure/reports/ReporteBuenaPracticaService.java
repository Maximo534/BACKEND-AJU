package pe.gob.pj.prueba.infraestructure.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private final BuenaPracticaPersistencePort persistencePort;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String baseUrl;

    // --- FUENTES & ESTILOS (Estandarizados) ---
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font FONT_BOLD_12 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, new Color(5, 64, 209));

    // COLORES
    private static final Color COLOR_FONDO_TITULO = new Color(232, 232, 232); // Gris para títulos de sección

    public byte[] generarFichaBuenaPractica(String id) throws Exception {
        BuenaPractica bp = persistencePort.buscarPorId(id);
        if (bp == null) throw new Exception("No se encontró Buena Práctica con ID: " + id);

        return crearDocumentoPdf(bp);
    }

    private byte[] crearDocumentoPdf(BuenaPractica bp) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Margen superior 120 para respetar la cabecera fija
            Document document = new Document(PageSize.A4, 30, 30, 120, 30);

            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // --- TÍTULO DISTRITO JUDICIAL ---
            Paragraph pDistrito = new Paragraph(val(bp.getDistritoJudicialNombre()).toUpperCase(), FONT_TITULO);
            pDistrito.setAlignment(Element.ALIGN_CENTER);
            pDistrito.setSpacingAfter(10);
            document.add(pDistrito);

            // --- NÚMERO DE FICHA (Estilo Caja a la derecha) ---
            PdfPTable tableNum = new PdfPTable(3);
            tableNum.setWidthPercentage(100);
            tableNum.setWidths(new float[]{80, 5, 15});

            PdfPCell cVacia = new PdfPCell(new Phrase(""));
            cVacia.setBorder(Rectangle.NO_BORDER);

            PdfPCell cNumLabel = new PdfPCell(new Phrase("N°: ", FONT_BOLD_8));
            cNumLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cNumLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cNumLabel.setBorder(Rectangle.NO_BORDER);

            PdfPCell cNumVal = new PdfPCell(new Phrase(bp.getId(), FONT_NORMAL_8));
            cNumVal.setHorizontalAlignment(Element.ALIGN_CENTER);
            cNumVal.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cNumVal.setBorder(Rectangle.BOX);
            cNumVal.setPadding(3);

            tableNum.addCell(cVacia);
            tableNum.addCell(cNumLabel);
            tableNum.addCell(cNumVal);
            document.add(tableNum);

            agregarEspacio(document);

            // --- I. DATOS DE LA CORTE ---
            agregarSubtitulo(document, "I. DATOS DE LA CORTE SUPERIOR DE JUSTICIA:");

            agregarFilaDato(document, "Responsable de buena práctica", ": " + val(bp.getResponsable()));
            agregarFilaDato(document, "Correo electrónico", ": " + val(bp.getEmail()));
            agregarFilaDato(document, "Teléfono celular o fijo", ": " + val(bp.getTelefono()));
            agregarFilaDato(document, "Fecha de inicio de la aplicación de la buena práctica", ": " + formatearFecha(bp));

            agregarBloquePreguntaRespuesta(document, "Integrantes, cargos y acción que realiza cada persona que ejecuta la buena práctica:", bp.getIntegrantes());

            agregarLineaSeparadora(document);

            // --- II. BUENA PRÁCTICA ---
            agregarSubtitulo(document, "II. BUENA PRÁCTICA:");
            agregarEspacio(document);

            // A & B
            agregarBloquePreguntaRespuesta(document, "A. Título:", bp.getTitulo());
            agregarBloquePreguntaRespuesta(document, "B. Categoría:", bp.getCategoria());

            // C. Situación previa
            agregarEspacio(document);
            document.add(new Paragraph("C. Situación previa (cifras y porcentajes):", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. Cuál es el problema:", bp.getProblema());
            agregarBloquePreguntaRespuesta(document, "2. Cuáles son las causas del problema:", bp.getCausa());
            agregarBloquePreguntaRespuesta(document, "3. Cuáles son las consecuencias del problema:", bp.getConsecuencia());

            // D. ¿Qué hizo?
            agregarEspacio(document);
            document.add(new Paragraph("D. ¿Qué hizo? explique en qué consistió la buena práctica, operatividad, medidas adoptadas para la solución del problema:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. Describe la buena práctica, informe de una manera objetiva, clara y verificable:", bp.getDescripcionGeneral());
            agregarBloquePreguntaRespuesta(document, "2. ¿Qué se busca lograr con la iniciativa? Detallar objetivos principales y específicos:", bp.getObjetivo());
            agregarBloquePreguntaRespuesta(document, "3. ¿De qué manera los objetivos responden a la problemática presentada?:", bp.getLogro());
            agregarBloquePreguntaRespuesta(document, "4. ¿Con qué aliados se contó para mejorar la implementación de la iniciativa?:", bp.getAliado());
            agregarBloquePreguntaRespuesta(document, "5. ¿Cuáles fueron las dificultades, obstáculos y/o amenazas encontradas en el proceso de creación...?:", bp.getDificultad());
            agregarBloquePreguntaRespuesta(document, "6. ¿Qué regla de brasilia se cumple con la mencionada práctica?:", bp.getNorma());

            // E. Practicidad
            agregarEspacio(document);
            document.add(new Paragraph("E. Practicidad y operatividad:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Cómo se desarrolla la buena práctica?:", bp.getDesarrollo());
            agregarBloquePreguntaRespuesta(document, "2. ¿Cómo se ejecuta la buena práctica?:", bp.getEjecucion());

            // F. Resultados
            agregarEspacio(document);
            document.add(new Paragraph("F. Resultados o impactos de la buena práctica:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Qué actividad y medidas se realizaron como parte de la implementación de la iniciativa?:", bp.getActividad());
            agregarBloquePreguntaRespuesta(document, "2. ¿Cuál es el aporte innovador de su práctica?:", bp.getAporte());
            agregarBloquePreguntaRespuesta(document, "3. ¿Qué resultados obtuvieron (en cifras y porcentajes) con la implementación de la buena práctica?:", bp.getResultado());
            agregarBloquePreguntaRespuesta(document, "4. ¿El impacto está pensado a corto, mediano o largo plazo?:", bp.getImpacto());

            // Reutilizamos campos existentes o placeholders si faltan datos en el objeto BP
            agregarBloquePreguntaRespuesta(document, "5. ¿Por qué consideran que su iniciativa es un aporte relevante que garantiza el acceso a la justicia?:", val(bp.getAporte()));
            agregarBloquePreguntaRespuesta(document, "6. Antes:", val(bp.getProblema()));
            agregarBloquePreguntaRespuesta(document, "7. Ahora:", val(bp.getResultado()));

            // G. Población
            agregarEspacio(document);
            document.add(new Paragraph("G. Población beneficiaria:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Cuál es el público objetivo, y que se benefició con la buena práctica?:", bp.getPublicoObjetivo());

            // H. Indicadores
            agregarEspacio(document);
            document.add(new Paragraph("H. Indicadores de impacto:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Cuál es el principal impacto logrado?:", bp.getImpacto());
            agregarBloquePreguntaRespuesta(document, "2. ¿Cómo se ha mejorado la situación de las personas?:", bp.getResultado());

            // I. Replicabilidad
            agregarEspacio(document);
            document.add(new Paragraph("I. Replicabilidad:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿La experiencia es replicable?:", bp.getInfoAdicional());

            // J. Eficiencia
            agregarEspacio(document);
            document.add(new Paragraph("J. Eficiencia:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Se llevaron a cabo acciones para asegurar el uso eficiente de los recursos?:", bp.getDesarrollo());

            // K. Integralidad
            agregarEspacio(document);
            document.add(new Paragraph("K. Integralidad:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿A qué objetivo institucional está vinculada esta buena práctica?:", bp.getObjetivo());
            agregarBloquePreguntaRespuesta(document, "2. ¿A qué política pública nacional está vinculada esta buena práctica?:", bp.getNorma());

            // L. Aporte trascendencia
            agregarEspacio(document);
            document.add(new Paragraph("L. Aporte trascendencia:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Cuál es la importancia de la buena práctica?:", bp.getAporte());
            agregarBloquePreguntaRespuesta(document, "2. ¿La buena práctica implementada promueve el efectivo acceso a la justicia?:", bp.getResultado());
            agregarBloquePreguntaRespuesta(document, "3. ¿Cuál es el aporte a la sociedad?:", bp.getImpacto());

            // LL. Sostenibilidad
            agregarEspacio(document);
            document.add(new Paragraph("LL. Sostenibilidad:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Cuáles fueron las medidas adoptadas por la Corte Superior de Justicia?:", bp.getEjecucion());
            agregarBloquePreguntaRespuesta(document, "2. ¿Se incorporaron políticas, normas o instrumentos de gestión?:", bp.getNorma());

            // M. Dificultades
            agregarEspacio(document);
            document.add(new Paragraph("M. Dificultades:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. Interna:", bp.getDificultad());
            agregarBloquePreguntaRespuesta(document, "2. Externa:", bp.getDificultad());

            // N. Aliados
            agregarEspacio(document);
            document.add(new Paragraph("N. Aliados:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. Internos:", bp.getAliado());
            agregarBloquePreguntaRespuesta(document, "2. Externos:", bp.getAliado());

            // O. Lecciones
            agregarEspacio(document);
            document.add(new Paragraph("O. Lecciones aprendidas:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. Describe las principales lecciones en el diseño, implementación, evaluación...:", bp.getLeccionAprendida());

            // P. Información adicional
            agregarEspacio(document);
            document.add(new Paragraph("P. Información adicional:", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿De acuerdo a la buena práctica presentada se requiere que incorpore información adicional...?:", bp.getInfoAdicional());

            // --- Q. ANEXOS ---
            agregarEspacio(document);
            agregarSubtitulo(document, "Q. ANEXOS:");

            addLink(document, "Descargar diapositiva (Haz clic aquí)", baseUrl + "/publico/v1/buenas-practicas/" + bp.getId() + "/ppt");
            addLink(document, "Ver documento (Haz clic aquí)", baseUrl + "/publico/v1/buenas-practicas/" + bp.getId() + "/documento");
            addLink(document, "Ver videos (Haz clic aquí)", baseUrl + "/visor/bp/videos/" + bp.getId());
            addLink(document, "Ver fotografías (Haz clic aquí)", baseUrl + "/visor/bp/fotos/" + bp.getId());

            agregarLineaSeparadora(document);

            // --- PIE DE REGISTRO ---
            String usuario = (bp.getUsuarioRegistro() != null) ? bp.getUsuarioRegistro() : "SISTEMA";
            agregarFilaDato(document, "Registrado por", ": " + usuario);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Firma
            Paragraph pLinea = new Paragraph("------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8);
            pLinea.setAlignment(Element.ALIGN_CENTER);
            document.add(pLinea);

            Paragraph pFirmaNombre = new Paragraph(usuario, FONT_BOLD_8);
            pFirmaNombre.setAlignment(Element.ALIGN_CENTER);
            document.add(pFirmaNombre);

            Paragraph pFirmaCargo = new Paragraph("RESPONSABLE DEL REGISTRO", FONT_NORMAL_8);
            pFirmaCargo.setAlignment(Element.ALIGN_CENTER);
            document.add(pFirmaCargo);

            document.close();
            return out.toByteArray();

        } catch (DocumentException e) {
            log.error("Error generando PDF BP", e);
            throw new Exception("Error al construir el PDF de Buena Práctica");
        }
    }

    // ==========================================
    //              HELPERS VISUALES
    // ==========================================

    private void agregarSubtitulo(Document doc, String texto) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_BOLD_8));
        cell.setBackgroundColor(COLOR_FONDO_TITULO);
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(4f);
        table.addCell(cell);
        doc.add(table);
    }

    private void agregarFilaDato(Document doc, String label, String value) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 65});

        PdfPCell c1 = new PdfPCell(new Phrase(label, FONT_NORMAL_8));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(3f);

        PdfPCell c2 = new PdfPCell(new Phrase(value, FONT_NORMAL_8));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(3f);

        table.addCell(c1);
        table.addCell(c2);
        doc.add(table);
    }

    private void agregarBloquePreguntaRespuesta(Document doc, String pregunta, String respuesta) throws DocumentException {
        // 1. Pregunta
        Paragraph pPreg = new Paragraph(pregunta, FONT_BOLD_8);
        pPreg.setSpacingBefore(6f);
        pPreg.setSpacingAfter(3f);
        doc.add(pPreg);

        // 2. Respuesta en recuadro
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        String texto = (respuesta != null && !respuesta.isBlank()) ? respuesta : " ";
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_NORMAL_8));
        cell.setPadding(5f);
        cell.setBorder(Rectangle.BOX);

        table.addCell(cell);
        doc.add(table);
    }

    private void addLink(Document doc, String text, String url) throws DocumentException {
        Anchor anchor = new Anchor(text, FONT_LINK);
        anchor.setReference(url);
        doc.add(new Paragraph(anchor));
    }

    private void agregarLineaSeparadora(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
        doc.add(Chunk.NEWLINE);
    }

    private void agregarEspacio(Document doc) throws DocumentException {
        doc.add(new Paragraph(" ", FONT_NORMAL_8));
    }

    private String val(String s) {
        return s != null ? s : "";
    }

    private String formatearFecha(BuenaPractica bp) {
        return (bp.getFechaInicio() != null) ?
                bp.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-";
    }

    // ==========================================
    //           HEADER & FOOTER EVENT
    // ==========================================
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                float pageTop = document.getPageSize().getHeight();

                // Logo
                try {
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.jpg"));
                    logo.scaleToFit(500, 50);
                    // Posición fija
                    logo.setAbsolutePosition(30, pageTop - 70);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}

                // Títulos Cabecera
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("BUENAS PRÁCTICAS", FONT_BOLD_10),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        pageTop - 90, 0);

                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("(INFORME)", FONT_BOLD_10),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        pageTop - 105, 0);

            } catch (Exception e) {}
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase("Página " + writer.getPageNumber(), FONT_NORMAL_8),
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 20, 0);
        }
    }
}