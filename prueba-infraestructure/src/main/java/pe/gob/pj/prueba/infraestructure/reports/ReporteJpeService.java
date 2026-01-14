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

    // --- FUENTES ESTILO PHP ---
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_NORMAL_7 = FontFactory.getFont(FontFactory.HELVETICA, 7); // Para footer
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, new Color(5, 64, 209));

    @Transactional(readOnly = true)
    public byte[] generarFichaJpe(String id) throws Exception {

        MovJpeCasoAtendidoEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Caso JPE no encontrado con ID: " + id));

        // --- PREPARACIÓN DE DATOS MAESTROS ---
        String nombreUgel = "";
        String nombreColegio = "";

        // Recuperamos datos a través del Juez Escolar relacionado
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
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // --- TÍTULO CORTE (Centrado) ---
            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pCorte = new Paragraph(nombreCorte, FONT_TITULO);
            pCorte.setAlignment(Element.ALIGN_CENTER);
            document.add(pCorte);

            // --- NÚMERO DE FICHA (Derecha) ---
            PdfPTable tableNum = new PdfPTable(2);
            tableNum.setWidthPercentage(100);
            tableNum.setWidths(new float[]{85, 15});

            PdfPCell cVacia = new PdfPCell(new Phrase(""));
            cVacia.setBorder(Rectangle.NO_BORDER);

            // Recuadro con ID
            PdfPCell cNum = new PdfPCell(new Phrase(entity.getId(), FONT_BOLD_8));
            cNum.setHorizontalAlignment(Element.ALIGN_CENTER);
            cNum.setBorder(Rectangle.BOX);

            // Etiqueta "N°: "
            PdfPTable tRight = new PdfPTable(2);
            tRight.setWidths(new float[]{30, 70});
            PdfPCell cLbl = new PdfPCell(new Phrase("N°: ", FONT_BOLD_8));
            cLbl.setBorder(Rectangle.NO_BORDER);
            cLbl.setHorizontalAlignment(Element.ALIGN_RIGHT);

            tRight.addCell(cLbl);
            tRight.addCell(cNum);

            PdfPCell cWrapper = new PdfPCell(tRight);
            cWrapper.setBorder(Rectangle.NO_BORDER);

            tableNum.addCell(cVacia);
            tableNum.addCell(cWrapper);
            document.add(tableNum);

            // --- DATOS INSTITUCIONALES (Sin borde en PHP) ---
            agregarFila(document, "UGEL", ": " + nombreUgel);
            agregarFila(document, "Institución educativa", ": " + nombreColegio);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechaReg = entity.getFechaRegistro() != null ? entity.getFechaRegistro().format(fmt) : "";
            agregarFila(document, "Fecha de Registro", ": " + fechaReg);

            agregarSeparador(document);

            // --- I. LUGAR ---
            document.add(new Paragraph("I. LUGAR DE LA ACTIVIDAD:", FONT_BOLD_8));
            agregarFila(document, "Anexo/Localidad/Institución", ": " + val(entity.getLugarActividad()));

            // Ubigeo (Usamos Helpers con Trim)
            agregarFila(document, "Región", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoId())); // Ojo: en entity es distritoId

            document.add(Chunk.NEWLINE);

            // --- IV. DESCRIPCIÓN (Incidente) ---
            // PHP salta de I a IV directo
            document.add(new Paragraph("IV. DESCRIPCIÓN DE LA ACTIVIDAD REALIZADA (DETALLAR OBJETIVO Y FINALIDAD): ", FONT_BOLD_8));
            agregarBloqueTexto(document, entity.getResumenHechos()); // Mapeado a jpe_reshech

            // --- V. INSTITUCIONES ALIADAS ---
            document.add(new Paragraph("V. INSTITUCIONES ALIADAS: ", FONT_BOLD_8));
            // En el PHP parece que repite jpe_reshech por error o falta de columna,
            // aquí usaremos un campo dummy o vacío si no existe en tu entidad
            agregarBloqueTexto(document, " ");

            // --- VI. OBSERVACIONES ---
            document.add(new Paragraph("VI. OBSERVACIONES: ", FONT_BOLD_8));
            // En PHP repite jpe_reshech, aquí ponemos vacío o acuerdos si aplica
            agregarBloqueTexto(document, entity.getAcuerdos());

            // --- VII. ACTIVIDAD OPERATIVA ---
            document.add(new Paragraph("VII. ACTIVIDAD OPERATIVA REALIZADA: ", FONT_BOLD_8));
            agregarBloqueTexto(document, " "); // PHP lo deja vacío o repite variable errada

            document.add(Chunk.NEWLINE);

            // --- VIII. ANEXOS ---
            document.add(new Paragraph("VIII. ANEXOS: ", FONT_BOLD_8));

            // Link Acta
            agregarLink(document, "Ver formato de atención (Haz clic aquí)", baseUrl + "/publico/v1/jueces-escolares/casos/" + id + "/acta");

            // Link Videos
            agregarLink(document, "Ver videos (Haz clic aquí)", baseUrl + "/visor/jpe/casos/videos/" + id);

            // Link Fotos
            agregarLink(document, "Ver fotografías (Haz clic aquí)", baseUrl + "/visor/jpe/casos/fotos/" + id);

            agregarSeparador(document);

            // --- PIE DE PÁGINA ---
            // Fecha y Usuario en una línea o dos
            agregarFila(document, "Fecha de registro", ": " + fechaReg);
            agregarFila(document, "Registrado por", ": " + val(entity.getUsuarioRegistro()));

            document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE);

            // Firma centrada
            Paragraph pLinea = new Paragraph("------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8);
            pLinea.setAlignment(Element.ALIGN_CENTER);
            document.add(pLinea);

            // Datos del usuario que registra (mock por ahora, idealmente buscar en tabla usuario)
            Paragraph pFirma = new Paragraph(val(entity.getUsuarioRegistro()), FONT_BOLD_8); // Nombre usuario
            pFirma.setAlignment(Element.ALIGN_CENTER);
            document.add(pFirma);

            // Cargo y Sede (Mock según PHP)
            Paragraph pCargo = new Paragraph("ESPECIALISTA / RESPONSABLE - " + nombreCorte, FONT_NORMAL_8);
            pCargo.setAlignment(Element.ALIGN_CENTER);
            document.add(pCargo);

            document.close();
            return out.toByteArray();
        }
    }

    // ==========================================
    //              HELPERS VISUALES
    // ==========================================

    private void agregarFila(Document doc, String label, String value) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 65});

        PdfPCell c1 = new PdfPCell(new Phrase(label, FONT_NORMAL_8));
        c1.setBorder(Rectangle.NO_BORDER);

        PdfPCell c2 = new PdfPCell(new Phrase(value, FONT_NORMAL_8));
        c2.setBorder(Rectangle.NO_BORDER);

        table.addCell(c1);
        table.addCell(c2);
        doc.add(table);
    }

    private void agregarBloqueTexto(Document doc, String texto) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        String contenido = (texto != null && !texto.trim().isEmpty()) ? texto : " ";
        PdfPCell c = new PdfPCell(new Phrase(contenido, FONT_NORMAL_8));
        c.setBorder(Rectangle.BOX);
        c.setPadding(5f);

        table.addCell(c);
        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarLink(Document doc, String texto, String url) throws DocumentException {
        Anchor anchor = new Anchor(texto, FONT_LINK);
        anchor.setReference(url);
        doc.add(new Paragraph(anchor));
    }

    private void agregarSeparador(Document doc) throws DocumentException {
        Paragraph p = new Paragraph("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8);
        p.setSpacingBefore(2f);
        p.setSpacingAfter(2f);
        doc.add(p);
    }

    private String val(String s) { return s != null ? s : ""; }

    // ==========================================
    //        HELPERS MAESTROS (Con Trim)
    // ==========================================
    private String obtenerNombreCorte(String id) {
        if (id == null) return "";
        return repoCorte.findById(id.trim()).map(e -> e.getNombre()).orElse(id);
    }
    private String obtenerNombreDepa(String id) {
        if (id == null) return "";
        return repoDepa.findById(id.trim()).map(e -> e.getNombre()).orElse(id);
    }
    private String obtenerNombreProv(String id) {
        if (id == null) return "";
        return repoProv.findById(id.trim()).map(e -> e.getNombre()).orElse(id);
    }
    private String obtenerNombreDist(String id) {
        if (id == null) return "";
        return repoDist.findById(id.trim()).map(e -> e.getNombre()).orElse(id);
    }

    // ==========================================
    //           HEADER & FOOTER EVENT
    // ==========================================
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                try {
                    // Logo
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.JPG"));
                    logo.scaleToFit(300, 50); // Escala aproximada al PHP (25, 10, -300)
                    logo.setAbsolutePosition(25, document.getPageSize().getHeight() - 50);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}

                // Títulos Cabecera
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("JUECES DE PAZ ESCOLAR", FONT_BOLD_8),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.top() - 30, 0);

                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("(INFORME)", FONT_BOLD_8),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.top() - 42, 0);

            } catch (Exception e) {}
        }

        public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase("Página " + writer.getPageNumber(), FONT_NORMAL_7),
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 15, 0);
        }
    }
}