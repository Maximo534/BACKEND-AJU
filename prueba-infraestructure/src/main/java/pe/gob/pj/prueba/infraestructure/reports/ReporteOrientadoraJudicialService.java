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

    // --- REPOSITORIOS MAESTROS ---
    private final MaeDistritoJudicialRepository repoCorte;
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
    private static final Color COLOR_FONDO_TITULO = new Color(232, 232, 232); // Gris estándar

    @Transactional(readOnly = true)
    public byte[] generarFichaPdf(String id) throws Exception {

        MovOrientadoraJudicialEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Atención OJ no encontrada con ID: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Margen superior 120 para respetar la cabecera fija
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

            // --- I. DATOS GENERALES ---
            agregarSubtitulo(document, "I. DATOS DE LA ATENCIÓN:");

            String fechaStr = (entity.getFechaAtencion() != null) ?
                    entity.getFechaAtencion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";

            agregarFila(document, "Fecha de Atención", ": " + fechaStr);
            agregarFila(document, "Registrado por (Usuario)", ": " + val(entity.getUsuarioRegistro()));

            agregarEspacio(document);

            // --- II. DATOS DE LA PERSONA ---
            agregarSubtitulo(document, "II. DATOS DE LA PERSONA ATENDIDA:");

            agregarFila(document, "Nombre Completo", ": " + val(entity.getNombreCompleto()));
            agregarFila(document, "Documento Identidad", ": " + val(entity.getTipoDocumento()) + " - " + val(entity.getNumeroDocumento()));
            agregarFila(document, "Nacionalidad", ": " + val(entity.getNacionalidad()));
            agregarFila(document, "Edad", ": " + (entity.getEdad() != null ? entity.getEdad() : "-"));
            agregarFila(document, "Teléfono / Celular", ": " + val(entity.getTelefono()));
            agregarFila(document, "Dirección Domiciliaria", ": " + val(entity.getDireccion()));

            // Ubigeo
            agregarFila(document, "Departamento", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoId()));

            agregarEspacio(document);

            // --- III. DETALLE DEL CASO ---
            agregarSubtitulo(document, "III. DETALLE DEL CASO / CONSULTA:");

            agregarFila(document, "Tipo de Vulnerabilidad", ": " + val(entity.getTipoVulnerabilidad()));
            agregarFila(document, "Género", ": " + val(entity.getGenero()));
            agregarFila(document, "Lengua Materna", ": " + val(entity.getLenguaMaterna()));

            agregarLineaSeparadora(document); // Separador sutil interno

            agregarFila(document, "Materia / Tipo Caso", ": " + val(entity.getTipoCasoAtendido()));
            agregarFila(document, "N° Expediente (si aplica)", ": " + val(entity.getNumeroExpediente()));
            agregarFila(document, "Tipo de Violencia", ": " + val(entity.getTipoViolencia()));
            agregarFila(document, "Derivación (Institución)", ": " + val(entity.getDerivacionInstitucion()));

            // Reseña (Bloque texto grande)
            agregarBloqueTexto(document, "RESEÑA DEL CASO / MOTIVO DE CONSULTA:", entity.getResenaCaso());

            // --- IV. ANEXOS ---
            agregarEspacio(document);
            agregarSubtitulo(document, "IV. EVIDENCIAS Y ANEXOS:");

            Anchor linkAnexo = new Anchor("Ver Formato de Atención (PDF) - Haz clic aquí", FONT_LINK);
            linkAnexo.setReference(baseUrl + "/publico/v1/orientadoras/anexo/" + id);
            document.add(new Paragraph(linkAnexo));

            // Ajusta este link según tu controlador real de fotos OJ
            Anchor linkFoto = new Anchor("Ver Fotografía de la Actividad - Haz clic aquí", FONT_LINK);
            linkFoto.setReference(baseUrl + "/visor/oj/fotos/" + id);
            document.add(new Paragraph(linkFoto));

            agregarLineaSeparadora(document);

            // --- PIE Y FIRMA ---
            // Fecha impresión
            String fechaImp = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            agregarFila(document, "Fecha de impresión", ": " + fechaImp);
            agregarFila(document, "Registrado por", ": " + val(entity.getUsuarioRegistro()));

            document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE);

            Paragraph pLinea = new Paragraph("--------------------------------------------------", FONT_NORMAL_8);
            pLinea.setAlignment(Element.ALIGN_CENTER);
            document.add(pLinea);

            Paragraph pFirma = new Paragraph(val(entity.getUsuarioRegistro()) + "\nORIENTADORA JUDICIAL", FONT_BOLD_8);
            pFirma.setAlignment(Element.ALIGN_CENTER);
            document.add(pFirma);

            document.close();
            return out.toByteArray();
        }
    }

    // ============================================
    //              HELPERS VISUALES
    // ============================================

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
        // Si quieres subtítulo gris para el bloque:
        if (titulo != null) {
            PdfPTable tHead = new PdfPTable(1); tHead.setWidthPercentage(100);
            PdfPCell cHead = new PdfPCell(new Phrase(titulo, FONT_BOLD_8));
            cHead.setBorder(Rectangle.NO_BORDER);
            tHead.addCell(cHead);
            doc.add(tHead);
        }

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

    private void agregarLineaSeparadora(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
        doc.add(Chunk.NEWLINE);
    }

    private void agregarEspacio(Document doc) throws DocumentException {
        doc.add(new Paragraph(" ", FONT_NORMAL_7));
    }

    private String val(String s) { return s != null ? s : ""; }

    // Helpers Maestros
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

    // =================================================================
    //            CLASE INTERNA: CABECERA Y PIE DE PÁGINA FIX
    // =================================================================
    class HeaderFooterPageEvent extends PdfPageEventHelper {
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
                        new Phrase("ORIENTADORAS JUDICIALES", FONT_BOLD_10),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        pageTop - 90, 0);

                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("(INFORME DE ATENCIÓN)", FONT_BOLD_10),
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