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

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteJpeService {

    private final MovJpeCasoAtendidoRepository repository;

    // Repositorios Maestros
    private final MaeJuezPazEscolarRepository repoJuez;
    private final MaeDistritoJudicialRepository repoCorte;
    private final MaeInstitucionEducativaRepository repoColegio;
    private final MaeUgelRepository repoUgel;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String baseUrl;

    // Estilos
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, Color.BLUE);
    private static final Color COLOR_HEADER_BG = new Color(240, 240, 240);

    @Transactional(readOnly = true)
    public byte[] generarFichaJpe(String id) throws Exception {

        MovJpeCasoAtendidoEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Caso JPE no encontrado con ID: " + id));

        // ... (Lógica de Juez, Colegio y UGEL se mantiene igual) ...
        String nombreJuez = "";
        String gradoSeccionJuez = "";
        String nombreColegio = "";
        String nombreUgel = "";

        if(entity.getJuezEscolarId() != null) {
            MaeJuezPazEscolarEntity juez = repoJuez.findById(entity.getJuezEscolarId()).orElse(null);
            if(juez != null) {
                nombreJuez = juez.getNombres() + " " + juez.getApePaterno() + " " + juez.getApeMaterno();
                gradoSeccionJuez = juez.getGrado() + " - " + juez.getSeccion();
                nombreColegio = repoColegio.findById(juez.getInstitucionEducativaId())
                        .map(c -> c.getNombre()).orElse("-");
                String ugelId = repoColegio.findById(juez.getInstitucionEducativaId())
                        .map(c -> c.getUgelId()).orElse(null);
                if(ugelId != null) {
                    nombreUgel = repoUgel.findById(ugelId).map(u -> u.getNombre()).orElse("-");
                }
            }
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // Título Corte
            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pCorte = new Paragraph(nombreCorte, FONT_TITULO);
            pCorte.setAlignment(Element.ALIGN_CENTER);
            pCorte.setSpacingAfter(10);
            document.add(pCorte);

            // Tabla ID Caso
            PdfPTable tableId = new PdfPTable(2);
            tableId.setWidthPercentage(100);
            tableId.setWidths(new float[]{80, 20});
            PdfPCell cellEmpty = new PdfPCell(new Phrase("")); cellEmpty.setBorder(Rectangle.NO_BORDER);
            PdfPCell cellId = new PdfPCell(new Phrase("N° CASO: " + entity.getId(), FONT_BOLD_8));
            cellId.setHorizontalAlignment(Element.ALIGN_CENTER); cellId.setBorderWidth(1);
            tableId.addCell(cellEmpty); tableId.addCell(cellId);
            document.add(tableId);
            document.add(Chunk.NEWLINE);

            // I. DATOS GENERALES
            addSectionTitle(document, "I. DATOS GENERALES");
            agregarFila(document, "Fecha de Registro", ": " + entity.getFechaRegistro());
            agregarFila(document, "UGEL", ": " + nombreUgel);
            agregarFila(document, "Institución Educativa", ": " + nombreColegio);
            agregarFila(document, "Juez Escolar", ": " + nombreJuez);
            agregarFila(document, "Grado y Sección", ": " + gradoSeccionJuez);
            document.add(Chunk.NEWLINE);

            // II. LUGAR DE LA ACTIVIDAD
            addSectionTitle(document, "II. LUGAR DE LA ACTIVIDAD");

            // ✅ AQUÍ SE USAN LOS MÉTODOS CORREGIDOS CON TRIM()
            agregarFila(document, "Departamento", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoId()));
            agregarFila(document, "Lugar / Local", ": " + val(entity.getLugarActividad()));
            document.add(Chunk.NEWLINE);

            // III. ESTUDIANTES INVOLUCRADOS
            addSectionTitle(document, "III. ESTUDIANTES INVOLUCRADOS");
            PdfPTable tableEst = new PdfPTable(2);
            tableEst.setWidthPercentage(100);

            // Estudiante 1
            PdfPCell c1 = new PdfPCell(); c1.setBorder(Rectangle.NO_BORDER);
            c1.addElement(new Paragraph("ESTUDIANTE 1:", FONT_BOLD_8));
            c1.addElement(new Paragraph("Nombre: " + val(entity.getNombreEstudiante1()), FONT_NORMAL_8));
            c1.addElement(new Paragraph("DNI: " + val(entity.getDniEstudiante1()), FONT_NORMAL_8));
            c1.addElement(new Paragraph("Grado/Sec: " + val(entity.getGradoEstudiante1()) + " - " + val(entity.getSeccionEstudiante1()), FONT_NORMAL_8));

            // Estudiante 2
            PdfPCell c2 = new PdfPCell(); c2.setBorder(Rectangle.NO_BORDER);
            c2.addElement(new Paragraph("ESTUDIANTE 2:", FONT_BOLD_8));
            c2.addElement(new Paragraph("Nombre: " + val(entity.getNombreEstudiante2()), FONT_NORMAL_8));
            c2.addElement(new Paragraph("DNI: " + val(entity.getDniEstudiante2()), FONT_NORMAL_8));
            c2.addElement(new Paragraph("Grado/Sec: " + val(entity.getGradoEstudiante2()) + " - " + val(entity.getSeccionEstudiante2()), FONT_NORMAL_8));

            tableEst.addCell(c1); tableEst.addCell(c2);
            document.add(tableEst);
            document.add(Chunk.NEWLINE);

            // IV. HECHOS Y ACUERDOS
            agregarBloqueTexto(document, "IV. RESUMEN DE LOS HECHOS:", entity.getResumenHechos());
            agregarBloqueTexto(document, "V. ACUERDOS Y COMPROMISOS:", entity.getAcuerdos());

            // V. ANEXOS
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("VI. ANEXOS:", FONT_BOLD_8));
            Anchor linkActa = new Anchor("Ver acta digitalizada (Haz clic aquí)", FONT_LINK);
            linkActa.setReference(baseUrl + "/publico/v1/jueces-escolares/casos/" + id + "/acta");
            document.add(new Paragraph(linkActa));
            Anchor linkFotos = new Anchor("Ver fotografías (Haz clic aquí)", FONT_LINK);
            linkFotos.setReference(baseUrl + "/visor/fotos-jpe/" + id);
            document.add(new Paragraph(linkFotos));

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));

            // FOOTER
            agregarFila(document, "Fecha de registro", ": " + entity.getFechaRegistro());
            agregarFila(document, "Registrado por", ": " + val(entity.getUsuarioRegistro()));

            document.close();
            return out.toByteArray();
        }
    }

    // ============================================
    //        HELPERS CORREGIDOS CON TRIM()
    // ============================================

    private String obtenerNombreCorte(String id) {
        if (id == null) return "";
        // El trim() asegura que si viene "11 " lo convierta a "11" para buscarlo bien
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

    // ... Resto de helpers visuales (addSectionTitle, agregarFila, etc.) se mantienen igual ...
    private void addSectionTitle(Document doc, String title) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(title, FONT_BOLD_8));
        cell.setBackgroundColor(COLOR_HEADER_BG);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4f);
        table.addCell(cell);
        doc.add(table);
    }

    private void agregarFila(Document doc, String label, String value) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 70});
        PdfPCell c1 = new PdfPCell(new Phrase(label, FONT_NORMAL_8)); c1.setBorder(Rectangle.NO_BORDER);
        PdfPCell c2 = new PdfPCell(new Phrase(value, FONT_NORMAL_8)); c2.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1); table.addCell(c2);
        doc.add(table);
    }

    private void agregarBloqueTexto(Document doc, String titulo, String contenido) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(titulo, FONT_BOLD_8));
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        String texto = (contenido == null || contenido.trim().isEmpty()) ? "SIN REGISTROS" : contenido;
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_NORMAL_8));
        cell.setPadding(6);
        table.addCell(cell);
        doc.add(table);
    }

    private String val(String s) { return s != null ? s : ""; }

    class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                try {
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.JPG"));
                    logo.scaleToFit(500, 50);
                    logo.setAbsolutePosition(30, document.getPageSize().getHeight() - 70);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("JUECES DE PAZ ESCOLAR", FONT_BOLD_8),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.top() - 60, 0);
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("(INFORME)", FONT_BOLD_8),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.top() - 72, 0);
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