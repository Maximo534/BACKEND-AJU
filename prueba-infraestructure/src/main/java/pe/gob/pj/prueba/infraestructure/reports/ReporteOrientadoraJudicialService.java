package pe.gob.pj.prueba.infraestructure.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovOrientadoraJudicialEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovOrientadoraJudicialRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteOrientadoraJudicialService {

    private final MovOrientadoraJudicialRepository repository;

    // --- REPOSITORIOS MAESTROS (Para obtener descripciones) ---
    private final MaeDistritoJudicialRepository repoCorte;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String baseUrl;

    // --- ESTILOS (Estándar del Proyecto) ---
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, new Color(5, 64, 209));
    private static final Color COLOR_CABECERA = new Color(240, 240, 240);

    @Transactional(readOnly = true)
    public byte[] generarFichaPdf(String id) throws Exception {

        MovOrientadoraJudicialEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Atención OJ no encontrada con ID: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // --- TÍTULO CORTE ---
            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pCorte = new Paragraph(nombreCorte, FONT_TITULO);
            pCorte.setAlignment(Element.ALIGN_CENTER);
            document.add(pCorte);

            // --- NÚMERO DE FICHA ---
            PdfPTable tableNum = new PdfPTable(2);
            tableNum.setWidthPercentage(100);
            tableNum.setWidths(new float[]{80, 20});

            PdfPCell cVacia = new PdfPCell(new Phrase(""));
            cVacia.setBorder(Rectangle.NO_BORDER);

            PdfPCell cNum = new PdfPCell(new Phrase("N° CASO: " + entity.getId(), FONT_BOLD_8));
            cNum.setHorizontalAlignment(Element.ALIGN_CENTER);
            cNum.setBorder(Rectangle.BOX);

            tableNum.addCell(cVacia);
            tableNum.addCell(cNum);
            document.add(tableNum);
            document.add(Chunk.NEWLINE);

            // --- I. DATOS GENERALES ---
            addSectionTitle(document, "I. DATOS DE LA ATENCIÓN");

            String fechaStr = (entity.getFechaAtencion() != null) ?
                    entity.getFechaAtencion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";

            agregarFila(document, "Fecha de Atención", ": " + fechaStr);
            agregarFila(document, "Registrado por (Usuario)", ": " + val(entity.getUsuarioRegistro()));
            document.add(Chunk.NEWLINE);

            // --- II. DATOS DE LA USUARIA ---
            addSectionTitle(document, "II. DATOS DE LA PERSONA ATENDIDA");

            agregarFila(document, "Nombre Completo", ": " + val(entity.getNombreCompleto()));
            agregarFila(document, "Documento Identidad", ": " + val(entity.getTipoDocumento()) + " - " + val(entity.getNumeroDocumento()));
            agregarFila(document, "Nacionalidad", ": " + val(entity.getNacionalidad()));
            agregarFila(document, "Edad", ": " + (entity.getEdad() != null ? entity.getEdad() : "-"));
            agregarFila(document, "Teléfono / Celular", ": " + val(entity.getTelefono()));
            agregarFila(document, "Dirección Domiciliaria", ": " + val(entity.getDireccion()));

            // Ubigeo con nombres enriquecidos
            agregarFila(document, "Departamento", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoId()));
            document.add(Chunk.NEWLINE);

            // --- III. DETALLE DEL CASO ---
            addSectionTitle(document, "III. DETALLE DEL CASO / CONSULTA");

            agregarFila(document, "Tipo de Vulnerabilidad", ": " + val(entity.getTipoVulnerabilidad()));
            agregarFila(document, "Género", ": " + val(entity.getGenero()));
            agregarFila(document, "Lengua Materna", ": " + val(entity.getLenguaMaterna()));
            document.add(Chunk.NEWLINE); // Separador visual

            agregarFila(document, "Materia / Tipo Caso", ": " + val(entity.getTipoCasoAtendido()));
            agregarFila(document, "N° Expediente (si aplica)", ": " + val(entity.getNumeroExpediente()));
            agregarFila(document, "Tipo de Violencia", ": " + val(entity.getTipoViolencia()));
            agregarFila(document, "Derivación (Institución)", ": " + val(entity.getDerivacionInstitucion()));

            // Reseña (Bloque de texto grande)
            document.add(Chunk.NEWLINE);
            PdfPTable tResena = new PdfPTable(1);
            tResena.setWidthPercentage(100);

            PdfPCell cTituloResena = new PdfPCell(new Phrase("RESEÑA DEL CASO / MOTIVO DE CONSULTA:", FONT_BOLD_8));
            cTituloResena.setBorder(Rectangle.NO_BORDER);
            tResena.addCell(cTituloResena);

            PdfPCell cTextoResena = new PdfPCell(new Phrase(val(entity.getResenaCaso()), FONT_NORMAL_8));
            cTextoResena.setPadding(6f);
            cTextoResena.setBorder(Rectangle.BOX);
            tResena.addCell(cTextoResena);

            document.add(tResena);
            document.add(Chunk.NEWLINE);

            // --- IV. ANEXOS ---
            addSectionTitle(document, "IV. EVIDENCIAS Y ANEXOS");
            document.add(Chunk.NEWLINE);

            // Link Acta
            Anchor linkAnexo = new Anchor("Ver Formato de Atención (PDF) - Haz clic aquí", FONT_LINK);
            linkAnexo.setReference(baseUrl + "/publico/v1/orientadoras/anexo/" + id);
            document.add(new Paragraph(linkAnexo));

            // Link Foto (Si existe visor, o descarga directa)
            // Asumiendo que existe un endpoint de visor o descarga de fotos similar
            Anchor linkFoto = new Anchor("Ver Fotografía de la Actividad - Haz clic aquí", FONT_LINK);
            // Usamos un endpoint genérico de descarga o visor si tienes
            // Si no tienes visor de fotos OJ aun, puedes apuntar a descarga directa o dejar pendiente
            linkFoto.setReference(baseUrl + "/publico/v1/orientadoras/anexo/" + id + "?tipo=FOTO_OJ");
            document.add(new Paragraph(linkFoto));

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));

            // --- PIE DE REGISTRO ---
            Paragraph pPie = new Paragraph("Fecha de impresión: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), FONT_NORMAL_8);
            pPie.setAlignment(Element.ALIGN_RIGHT);
            document.add(pPie);

            document.close();
            return out.toByteArray();
        }
    }

    // ============================================
    //              HELPERS VISUALES
    // ============================================

    private void addSectionTitle(Document doc, String title) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(title, FONT_BOLD_8));
        cell.setBackgroundColor(COLOR_CABECERA);
        cell.setBorder(Rectangle.NO_BORDER);
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

        PdfPCell c2 = new PdfPCell(new Phrase(value, FONT_NORMAL_8));
        c2.setBorder(Rectangle.NO_BORDER);

        table.addCell(c1);
        table.addCell(c2);
        doc.add(table);
    }

    private String val(String s) { return s != null ? s : ""; }

    // ============================================
    //        HELPERS DE NOMBRES (Trimmed)
    // ============================================
    private String obtenerNombreCorte(String id) {
        if(id == null) return "";
        return repoCorte.findById(id.trim()).map(e -> e.getNombre()).orElse(id);
    }
    private String obtenerNombreDepa(String id) {
        if(id == null) return "";
        return repoDepa.findById(id.trim()).map(e -> e.getNombre()).orElse(id);
    }
    private String obtenerNombreProv(String id) {
        if(id == null) return "";
        return repoProv.findById(id.trim()).map(e -> e.getNombre()).orElse(id);
    }
    private String obtenerNombreDist(String id) {
        if(id == null) return "";
        return repoDist.findById(id.trim()).map(e -> e.getNombre()).orElse(id);
    }

    // ============================================
    //           CABECERA Y PIE
    // ============================================
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                try {
                    // Intento cargar logo
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.JPG"));
                    logo.scaleToFit(500, 50);
                    logo.setAbsolutePosition(30, document.getPageSize().getHeight() - 70);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}

                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("ORIENTADORAS JUDICIALES", FONT_BOLD_10),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.top() - 60, 0);

                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("(INFORME DE ATENCIÓN)", FONT_BOLD_8),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.top() - 72, 0);
            } catch (Exception e) {}
        }

        public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase("Página " + writer.getPageNumber(), FONT_NORMAL_8),
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 20, 0);
        }
    }
}