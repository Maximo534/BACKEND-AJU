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

    @Value("${app.frontend.url:http://localhost:4200}") // Para armar los links de anexos
    private String baseUrl;

    // --- FUENTES (Réplica del estilo PHP FPDF) ---
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_12 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, new Color(5, 64, 209));

    // Separador visual usado en PHP
    private static final String SEPARATOR_LINE = "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";

    public byte[] generarFichaBuenaPractica(String id) throws Exception {
        BuenaPractica bp = persistencePort.buscarPorId(id);
        if (bp == null) throw new Exception("No se encontró Buena Práctica con ID: " + id);

        return crearDocumentoPdf(bp);
    }

    private byte[] crearDocumentoPdf(BuenaPractica bp) throws Exception {
        // Márgenes similares a FPDF (Left 25 -> ~10mm, Top 10...)
        // Ajustamos a OpenPDF standard A4
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // --- TÍTULO DISTRITO JUDICIAL ---
            // PHP: $pdf->Cell(0, 8, utf8_decode("").$rowbp['x_nom_distrito'],0,1,'C',0);
            Paragraph pDistrito = new Paragraph(val(bp.getDistritoJudicialNombre()), FONT_BOLD_12);
            pDistrito.setAlignment(Element.ALIGN_CENTER);
            pDistrito.setSpacingAfter(5f);
            document.add(pDistrito);

            // --- NÚMERO DE FICHA ---
            // PHP: Alineado a la derecha "N°: [ID]" (usando tabla para layout)
            PdfPTable tableNum = new PdfPTable(2);
            tableNum.setWidthPercentage(100);
            tableNum.setWidths(new float[]{85, 15});

            PdfPCell cVacia = new PdfPCell(new Phrase(""));
            cVacia.setBorder(Rectangle.NO_BORDER);

            PdfPCell cNumLabel = new PdfPCell(new Phrase("N°: ", FONT_BOLD_8));
            cNumLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cNumLabel.setBorder(Rectangle.NO_BORDER);

            PdfPCell cNumVal = new PdfPCell(new Phrase(bp.getId(), FONT_BOLD_8));
            cNumVal.setHorizontalAlignment(Element.ALIGN_CENTER);
            cNumVal.setBorder(Rectangle.BOX); // Con borde como en PHP

            // Truco para alinear "N°" y el recuadro
            PdfPTable tableRight = new PdfPTable(2);
            tableRight.setWidths(new float[]{40, 60});
            tableRight.addCell(cNumLabel);
            tableRight.addCell(cNumVal);

            PdfPCell cWrapper = new PdfPCell(tableRight);
            cWrapper.setBorder(Rectangle.NO_BORDER);

            tableNum.addCell(cVacia);
            tableNum.addCell(cWrapper);
            document.add(tableNum);

            // --- I. DATOS DE LA CORTE ---
            document.add(new Paragraph("I. DATOS DE LA CORTE SUPERIOR DE JUSTICIA: ", FONT_BOLD_8));
            document.add(Chunk.NEWLINE);

            agregarFilaDato(document, "Responsable de buena práctica", ": " + val(bp.getResponsable()));
            agregarFilaDato(document, "Correo electrónico", ": " + val(bp.getEmail()));
            agregarFilaDato(document, "Teléfono celular o fijo", ": " + val(bp.getTelefono()));
            agregarFilaDato(document, "Fecha de inicio de la aplicación de la buena práctica", ": " + formatearFecha(bp));

            agregarBloquePreguntaRespuesta(document, "Integrantes, cargos y acción que realiza cada persona que ejecuta la buena práctica:", bp.getIntegrantes());

            agregarSeparador(document);

            // --- II. BUENA PRÁCTICA ---
            document.add(new Paragraph("II. BUENA PRÁCTICA: ", FONT_BOLD_8));
            document.add(Chunk.NEWLINE);

            // A & B
            agregarBloquePreguntaRespuesta(document, "A. Título:", bp.getTitulo());
            agregarBloquePreguntaRespuesta(document, "B. Categoría:", bp.getCategoria());

            // C. Situación previa
            document.add(new Paragraph("C. Situación previa (cifras y porcentajes): ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. Cuál es el problema:", bp.getProblema());
            agregarBloquePreguntaRespuesta(document, "2. Cuáles son las causas del problema:", bp.getCausa());
            agregarBloquePreguntaRespuesta(document, "3. Cuáles son las consecuencias del problema:", bp.getConsecuencia());

            // D. ¿Qué hizo?
            document.add(new Paragraph("D. ¿Qué hizo? explique en qué consistió la buena práctica, operatividad, medidas adoptadas para la solución del problema: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. Describe la buena práctica, informe de una manera objetiva, clara y verificable:", bp.getDescripcionGeneral());
            agregarBloquePreguntaRespuesta(document, "2. ¿Qué se busca lograr con la iniciativa? Detallar objetivos principales y específicos:", bp.getObjetivo()); // Asumimos objetivo general
            agregarBloquePreguntaRespuesta(document, "3. ¿De qué manera los objetivos responden a la problemática presentada?:", bp.getLogro()); // Mapping aproximado según tu entidad
            agregarBloquePreguntaRespuesta(document, "4. ¿Con qué aliados se contó para mejorar la implementación de la iniciativa?:", bp.getAliado());
            agregarBloquePreguntaRespuesta(document, "5. ¿Cuáles fueron las dificultades, obstáculos y/o amenazas encontradas...?:", bp.getDificultad());
            agregarBloquePreguntaRespuesta(document, "6. ¿Qué regla de brasilia se cumple con la mencionada práctica?:", bp.getNorma());

            // E. Practicidad
            document.add(new Paragraph("E. Practicidad y operatividad: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Cómo se desarrolla la buena práctica?:", bp.getDesarrollo());
            agregarBloquePreguntaRespuesta(document, "2. ¿Cómo se ejecuta la buena práctica?:", bp.getEjecucion());

            // F. Resultados
            document.add(new Paragraph("F. Resultados o impactos de la buena práctica: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Qué actividad y medidas se realizaron como parte de la implementación...?:", bp.getActividad());
            agregarBloquePreguntaRespuesta(document, "2. ¿Cuál es el aporte innovador de su práctica?:", bp.getAporte()); // 'Aporte' mapado a innovador
            agregarBloquePreguntaRespuesta(document, "3. ¿Qué resultados obtuvieron (en cifras y porcentajes)...?:", bp.getResultado());
            agregarBloquePreguntaRespuesta(document, "4. ¿El impacto está pensado a corto, mediano o largo plazo?:", bp.getImpacto());

            // Nota: Estos campos no estaban en tu entidad base 'BuenaPractica' que vi antes,
            // pero si existen en BD, deberías agregarlos al dominio. Usaré placeholders o campos existentes.
            agregarBloquePreguntaRespuesta(document, "5. ¿Por qué consideran que su iniciativa es un aporte relevante...?:", bp.getAporte());
            agregarBloquePreguntaRespuesta(document, "6. Antes:", bp.getProblema()); // Placeholder si no tienes campo 'antes'
            agregarBloquePreguntaRespuesta(document, "7. Ahora:", bp.getResultado()); // Placeholder si no tienes campo 'ahora'

            // G. Población
            document.add(new Paragraph("G. Población beneficiaria: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Cuál es el público objetivo, y que se benefició con la buena práctica?:", bp.getPublicoObjetivo());

            // H. Indicadores
            document.add(new Paragraph("H. Indicadores de impacto: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Cuál es el principal impacto logrado?:", bp.getImpacto());
            agregarBloquePreguntaRespuesta(document, "2. ¿Cómo se ha mejorado la situación de las personas?:", bp.getResultado());

            // I. Replicabilidad
            document.add(new Paragraph("I. Replicabilidad: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿La experiencia es replicable?:", bp.getInfoAdicional()); // Usando info adicional si no hay campo específico

            // J. Eficiencia
            document.add(new Paragraph("J. Eficiencia: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Se llevaron a cabo acciones para asegurar el uso eficiente de los recursos?:", bp.getDesarrollo());

            // K. Integralidad
            document.add(new Paragraph("K. Integralidad: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿A qué objetivo institucional está vinculada esta buena práctica?:", bp.getObjetivo());
            agregarBloquePreguntaRespuesta(document, "2. ¿A qué política pública nacional está vinculada...?:", bp.getNorma());

            // L. Aporte trascendencia
            document.add(new Paragraph("L. Aporte trascendencia: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Cuál es la importancia de la buena práctica?:", bp.getAporte());
            agregarBloquePreguntaRespuesta(document, "2. ¿La buena práctica implementada promueve el efectivo acceso a la justicia?:", bp.getResultado());
            agregarBloquePreguntaRespuesta(document, "3. ¿Cuál es el aporte a la sociedad?:", bp.getImpacto());

            // LL. Sostenibilidad
            document.add(new Paragraph("LL. Sostenibilidad: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿Cuáles fueron las medidas adoptadas por la Corte...?:", bp.getEjecucion());
            agregarBloquePreguntaRespuesta(document, "2. ¿Se incorporaron políticas, normas o instrumentos...?:", bp.getNorma());

            // M. Dificultades
            document.add(new Paragraph("M. Dificultades: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. Interna:", bp.getDificultad());
            agregarBloquePreguntaRespuesta(document, "2. Externa:", bp.getDificultad());

            // N. Aliados
            document.add(new Paragraph("N. Aliados: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. Internos:", bp.getAliado());
            agregarBloquePreguntaRespuesta(document, "2. Externos:", bp.getAliado());

            // O. Lecciones
            document.add(new Paragraph("O. Lecciones aprendidas: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. Describe las principales lecciones...:", bp.getLeccionAprendida());

            // P. Información adicional
            document.add(new Paragraph("P. Información adicional: ", FONT_BOLD_8));
            agregarBloquePreguntaRespuesta(document, "1. ¿De acuerdo a la buena práctica presentada...?:", bp.getInfoAdicional());

            document.add(Chunk.NEWLINE);

            // --- Q. ANEXOS (Con Enlaces) ---
            document.add(new Paragraph("Q. Anexos: ", FONT_BOLD_8));
            document.add(Chunk.NEWLINE);

            // Enlace PPT
            // Apunta a: /publico/v1/buenas-practicas/{id}/ppt
            addLink(document, "Descargar diapositiva (Haz clic aquí)", baseUrl + "/publico/v1/buenas-practicas/" + bp.getId() + "/ppt");

            // Enlace Documento (Anexo)
            addLink(document, "Ver documento (Haz clic aquí)", baseUrl + "/publico/v1/buenas-practicas/" + bp.getId() + "/documento");

            // Enlace Videos (Si tienes visor frontend)
            // Si es descarga directa: .../video
            // Si es visor: .../visor/videos/{id}
            addLink(document, "Ver videos (Haz clic aquí)", baseUrl + "/visor/bp/videos/" + bp.getId());

            // Enlace Fotos
            addLink(document, "Ver fotografías (Haz clic aquí)", baseUrl + "/visor/bp/fotos/" + bp.getId());

            agregarSeparador(document);

            // --- PIE DE REGISTRO ---
            String usuario = (bp.getUsuarioRegistro() != null) ? bp.getUsuarioRegistro() : "SISTEMA";
            document.add(new Paragraph("Registrado por: " + usuario, FONT_NORMAL_8));
            agregarSeparador(document);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Firma centrada
            Paragraph pFirmaNombre = new Paragraph(usuario, FONT_NORMAL_8);
            pFirmaNombre.setAlignment(Element.ALIGN_CENTER);
            document.add(pFirmaNombre);

            // Si tuvieras cargo/sede en el objeto BP, lo pondrías aquí
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

    private void agregarFilaDato(Document doc, String label, String value) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 65});

        PdfPCell c1 = new PdfPCell(new Phrase(label, FONT_BOLD_8));
        c1.setBorder(Rectangle.NO_BORDER);

        PdfPCell c2 = new PdfPCell(new Phrase(value, FONT_NORMAL_8));
        c2.setBorder(Rectangle.NO_BORDER);

        table.addCell(c1);
        table.addCell(c2);
        doc.add(table);
    }

    // Replica el estilo: Pregunta (Texto) + Salto + Respuesta (Multicell con borde)
    private void agregarBloquePreguntaRespuesta(Document doc, String pregunta, String respuesta) throws DocumentException {
        // 1. Pregunta
        Paragraph pPreg = new Paragraph(pregunta, FONT_BOLD_8);
        pPreg.setSpacingBefore(4f);
        doc.add(pPreg);

        // 2. Respuesta en recuadro
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        String texto = (respuesta != null && !respuesta.isBlank()) ? respuesta : " "; // Espacio para que dibuje borde si es null
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_NORMAL_8));
        cell.setPadding(5f);
        cell.setBorder(Rectangle.BOX); // Borde completo (1 en PHP)

        table.addCell(cell);
        doc.add(table);
    }

    private void addLink(Document doc, String text, String url) throws DocumentException {
        Anchor anchor = new Anchor(text, FONT_LINK);
        anchor.setReference(url);
        doc.add(new Paragraph(anchor));
    }

    private void agregarSeparador(Document doc) throws DocumentException {
        Paragraph p = new Paragraph(SEPARATOR_LINE, FONT_NORMAL_8);
        p.setSpacingBefore(5f);
        p.setSpacingAfter(5f);
        doc.add(p);
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
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                // Pie de página: Página X/
                String text = "Página " + writer.getPageNumber();
                float textBase = document.bottom() - 15;
                float center = (document.right() - document.left()) / 2 + document.leftMargin();

                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                cb.beginText();

                // ✅ CORRECCIÓN: Manejo de excepción aquí
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                cb.setFontAndSize(bf, 8);

                cb.setTextMatrix(center, textBase);
                cb.showText(text);
                cb.endText();
                cb.restoreState(); // Es importante restaurar el estado gráfico

            } catch (Exception e) {
                // En un evento de página no podemos lanzar la excepción hacia arriba,
                // así que solo la ignoramos o logueamos. El PDF seguirá generándose.
                log.error("Error al poner pie de página", e);
            }
        }
    }
}