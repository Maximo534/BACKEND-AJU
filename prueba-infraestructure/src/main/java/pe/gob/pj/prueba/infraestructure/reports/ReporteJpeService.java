package pe.gob.pj.prueba.infraestructure.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeJuezPazEscolarEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJpeCasoAtendidoEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MaeJuezPazEscolarRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovJpeCasoAtendidoRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteJpeService {

    private final MovJpeCasoAtendidoRepository repository;

    // --- REPOSITORIOS MAESTROS ---
    private final MaeJuezPazEscolarRepository repoJuez;
    private final MaeDistritoJudicialRepository repoCorte;
    private final MaeInstitucionEducativaRepository repoColegio;
    private final MaeUgelRepository repoUgel;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String baseUrl;

    // --- FUENTES & ESTILOS (Estandarizados) ---
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font FONT_BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_NORMAL_7 = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, new Color(5, 64, 209));

    // COLORES
    private static final Color COLOR_FONDO_TITULO = new Color(232, 232, 232); // Gris para subtítulos

    @Transactional(readOnly = true)
    public byte[] generarFichaJpe(String id) throws Exception {

        MovJpeCasoAtendidoEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Caso JPE no encontrado con ID: " + id));

        // --- PREPARACIÓN DE DATOS MAESTROS ---
        String nombreUgel = "";
        String nombreColegio = "";

        if (entity.getJuezEscolarId() != null) {
            MaeJuezPazEscolarEntity juez = repoJuez.findById(entity.getJuezEscolarId()).orElse(null);
            if (juez != null) {
                // Colegio
                nombreColegio = repoColegio.findById(juez.getInstitucionEducativaId())
                        .map(c -> c.getNombre()).orElse("-");

                // UGEL (via Colegio)
                String ugelId = repoColegio.findById(juez.getInstitucionEducativaId())
                        .map(c -> c.getUgelId()).orElse(null);
                if (ugelId != null) {
                    nombreUgel = repoUgel.findById(ugelId).map(u -> u.getNombre()).orElse("-");
                }
            }
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Margen superior 120 para cabecera fija
            Document document = new Document(PageSize.A4, 30, 30, 120, 30);

            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // --- TÍTULO CORTE ---
            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pCorte = new Paragraph(nombreCorte.toUpperCase(), FONT_TITULO);
            pCorte.setAlignment(Element.ALIGN_CENTER);
            pCorte.setSpacingAfter(10);
            document.add(pCorte);

            // --- NÚMERO DE FICHA (Caja Derecha) ---
            PdfPTable tableNum = new PdfPTable(3);
            tableNum.setWidthPercentage(100);
            tableNum.setWidths(new float[]{80, 5, 15});

            PdfPCell cVacia = new PdfPCell(new Phrase(""));
            cVacia.setBorder(Rectangle.NO_BORDER);

            PdfPCell cLabelNum = new PdfPCell(new Phrase("N°:", FONT_BOLD_8));
            cLabelNum.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cLabelNum.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cLabelNum.setBorder(Rectangle.NO_BORDER);

            PdfPCell cValNum = new PdfPCell(new Phrase(entity.getId(), FONT_NORMAL_8));
            cValNum.setHorizontalAlignment(Element.ALIGN_CENTER);
            cValNum.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cValNum.setBorder(Rectangle.BOX);
            cValNum.setPadding(3);

            tableNum.addCell(cVacia);
            tableNum.addCell(cLabelNum);
            tableNum.addCell(cValNum);
            document.add(tableNum);

            agregarEspacio(document);

            // --- DATOS INSTITUCIONALES ---
            agregarFila(document, "UGEL", ": " + nombreUgel);
            agregarFila(document, "Institución educativa", ": " + nombreColegio);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechaReg = entity.getFechaRegistro() != null ? entity.getFechaRegistro().format(fmt) : "";
            agregarFila(document, "Fecha de Registro", ": " + fechaReg);

            agregarLineaSeparadora(document);

            // --- I. LUGAR DE LA ACTIVIDAD ---
            agregarSubtitulo(document, "I. LUGAR DE LA ACTIVIDAD:");

            agregarFila(document, "Anexo/Localidad/Institución", ": " + val(entity.getLugarActividad()));
            agregarFila(document, "Región", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoId()));

            agregarEspacio(document);

            // --- IV. DESCRIPCIÓN (Incidente) ---
            // Usamos "agregarBloqueTexto" que ya incluye espacio y subtítulo gris
            agregarBloqueTexto(document, "IV. DESCRIPCIÓN DE LA ACTIVIDAD REALIZADA (DETALLAR OBJETIVO Y FINALIDAD):", entity.getResumenHechos());

            // --- V. INSTITUCIONES ALIADAS ---
            // PHP tenía un bug repitiendo campos, aquí ponemos vacío o texto real si existiera
            agregarBloqueTexto(document, "V. INSTITUCIONES ALIADAS:", " ");

            // --- VI. OBSERVACIONES ---
            // Usamos acuerdos como observación, o vacío si prefieres
            agregarBloqueTexto(document, "VI. OBSERVACIONES:", entity.getAcuerdos());

            // --- VII. ACTIVIDAD OPERATIVA ---
            agregarBloqueTexto(document, "VII. ACTIVIDAD OPERATIVA REALIZADA:", " ");

            // --- VIII. ANEXOS ---
            agregarEspacio(document);
            agregarSubtitulo(document, "VIII. ANEXOS:");

            Anchor linkFicha = new Anchor("Ver formato de atención (Haz clic aquí)", FONT_LINK);
            linkFicha.setReference(baseUrl + "/publico/v1/jueces-escolares/casos/" + id + "/acta");
            document.add(new Paragraph(linkFicha));

            Anchor linkVideo = new Anchor("Ver videos (Haz clic aquí)", FONT_LINK);
            linkVideo.setReference(baseUrl + "/visor/jpe/casos/videos/" + id);
            document.add(new Paragraph(linkVideo));

            Anchor linkFoto = new Anchor("Ver fotografías (Haz clic aquí)", FONT_LINK);
            linkFoto.setReference(baseUrl + "/visor/jpe/casos/fotos/" + id);
            document.add(new Paragraph(linkFoto));

            agregarLineaSeparadora(document);

            // --- PIE ---
            agregarFila(document, "Fecha de registro", ": " + fechaReg);
            agregarFila(document, "Registrado por", ": " + val(entity.getUsuarioRegistro()));

            document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE);

            Paragraph pLinea = new Paragraph("--------------------------------------------------", FONT_NORMAL_8);
            pLinea.setAlignment(Element.ALIGN_CENTER);
            document.add(pLinea);

            Paragraph pFirma = new Paragraph(val(entity.getUsuarioRegistro()) + "\nUSUARIO RESPONSABLE", FONT_BOLD_8);
            pFirma.setAlignment(Element.ALIGN_CENTER);
            document.add(pFirma);

            document.close();
            return out.toByteArray();
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

    private void agregarFila(Document doc, String label, String value) throws DocumentException {
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

    private void agregarBloqueTexto(Document doc, String titulo, String contenido) throws DocumentException {
        agregarEspacio(doc);
        agregarSubtitulo(doc, titulo);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        String texto = (contenido == null || contenido.trim().isEmpty()) ? "SIN REGISTROS" : contenido;
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_NORMAL_8));
        cell.setPadding(6);
        cell.setBorder(Rectangle.BOX);
        if(texto.equals("SIN REGISTROS")) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        doc.add(table);
    }

    private void agregarEspacio(Document doc) throws DocumentException {
        doc.add(new Paragraph(" ", FONT_NORMAL_7));
    }

    private void agregarLineaSeparadora(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
        doc.add(Chunk.NEWLINE);
    }

    private String val(String s) { return s != null ? s : ""; }

    // Helpers Maestros
    private String obtenerNombreCorte(String id) { return id==null?"":repoCorte.findById(id.trim()).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreDepa(String id) { return id==null?"":repoDepa.findById(id.trim()).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreProv(String id) { return id==null?"":repoProv.findById(id.trim()).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreDist(String id) { return id==null?"":repoDist.findById(id.trim()).map(e -> e.getNombre()).orElse(id); }

    // ==========================================
    //           HEADER & FOOTER EVENT
    // ==========================================
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                float pageTop = document.getPageSize().getHeight();
                try {
                    // Logo
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.jpg"));
                    logo.scaleToFit(500, 50);
                    // Posición Fija: 30, Top-70
                    logo.setAbsolutePosition(30, pageTop - 70);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}

                // Títulos Cabecera
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("JUECES DE PAZ ESCOLAR", FONT_BOLD_10),
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