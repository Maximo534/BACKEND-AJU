package pe.gob.pj.prueba.infraestructure.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTareaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovEventoFcRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service("reporteFortalecimientoService")
@RequiredArgsConstructor
public class ReporteFortalecimientoService {

    private final MovEventoFcRepository repository;

    // Repositorios Maestros
    private final MaeDistritoJudicialRepository repoDistritoJud;
    private final MaeEjeRepository repoEje;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;
    private final MaeTipoParticipanteRepository repoTipoPart;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String baseUrl;

    // --- FUENTES & ESTILOS (Sincronizado con PHP) ---
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font FONT_BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_BOLD_7 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_NORMAL_7 = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, new Color(5, 64, 209));

    // COLORES
    private static final Color COLOR_FONDO_TITULO = new Color(232, 232, 232); // Gris subtítulos
    private static final Color COLOR_CABECERA_TABLA = new Color(240, 240, 240); // Gris tablas

    @Transactional(readOnly = true)
    public byte[] generarPdf(String id) throws Exception {

        MovEventoFcEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Evento FFC no encontrado: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Margen superior 120 para respetar la cabecera fija
            Document document = new Document(PageSize.A4, 30, 30, 120, 30);

            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // --- TÍTULO DISTRITO ---
            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pCorte = new Paragraph(nombreCorte.toUpperCase(), FONT_TITULO);
            pCorte.setAlignment(Element.ALIGN_CENTER);
            pCorte.setSpacingAfter(10);
            document.add(pCorte);

            // --- NÚMERO DE FICHA (Estilo Caja a la derecha) ---
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

            // --- DATOS GENERALES ---
            agregarFila(document, "Resolución Anual que Aprueba el Plan", ": N° " + val(entity.getResolucionPlanAnual()));
            agregarFila(document, "Resolución Administrativa que Aprueba el Plan", ": N° " + val(entity.getResolucionAdminPlan()));
            agregarFila(document, "Documento que Autoriza Actividad/Evento", ": N° " + val(entity.getDocumentoAutoriza()));
            agregarFila(document, "Tipo de Evento Académico", ": " + val(entity.getTipoEvento()));
            agregarFila(document, "Nombre del Evento", ": " + val(entity.getNombreEvento()));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            agregarFila(document, "Fecha de Inicio", ": " + (entity.getFechaInicio() != null ? entity.getFechaInicio().format(fmt) : ""));
            agregarFila(document, "Fecha de Finalización", ": " + (entity.getFechaFin() != null ? entity.getFechaFin().format(fmt) : ""));

            agregarFila(document, "Eje de Trabajo", ": " + obtenerNombreEje(entity.getEjeId()));
            agregarFila(document, "Modalidad", ": " + val(entity.getModalidad()));
            agregarFila(document, "Duración en Horas", ": " + (entity.getDuracionHoras() != null ? entity.getDuracionHoras() : "0"));
            agregarFila(document, "N° de sesiones", ": " + (entity.getNumeroSesiones() != null ? entity.getNumeroSesiones() : "0"));
            agregarFila(document, "Docente Expositor", ": " + val(entity.getDocenteExpositor()));
            agregarFila(document, "¿Participó Intérprete de Señas?", ": " + val(entity.getInterpreteSenias()));
            agregarFila(document, "Número de Personas con discapacidad Participantes", ": " + (entity.getNumeroDiscapacitados() != null ? entity.getNumeroDiscapacitados() : "0"));

            agregarFila(document, "¿Se Dictó en Lengua Nativa?", ": " + val(entity.getSeDictoLenguaNativa()));
            if ("SI".equalsIgnoreCase(entity.getSeDictoLenguaNativa())) {
                agregarFila(document, "Si es SI, Mencione Lengua Nativa", ": " + val(entity.getLenguaNativaDesc()));
            }

            agregarFila(document, "Público Objetivo", ": " + val(entity.getPublicoObjetivo()));
            if ("OTRO(ESPECIFICAR)".equalsIgnoreCase(entity.getPublicoObjetivo())) {
                agregarFila(document, "Observación", ": " + val(entity.getPublicoObjetivoDetalle()));
            }

            agregarLineaSeparadora(document);

            // --- I. LUGAR ---
            agregarSubtitulo(document, "I. LUGAR DE LA ACTIVIDAD (SOLO PRESENCIAL):");

            agregarFila(document, "Nombre de la Institución", ": " + val(entity.getNombreInstitucion()));
            agregarFila(document, "Región", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoGeograficoId()));

            agregarEspacio(document);

            // --- II. PARTICIPANTES (MATRIZ) ---
            agregarSubtitulo(document, "II. N° DE PARTICIPANTES POR RANGO DE EDAD Y GÉNERO:");
            agregarEspacio(document);

            crearMatrizParticipantes(document, entity.getParticipantes());

            agregarEspacio(document);

            // --- III. DESCRIPCIÓN ---
            agregarBloqueTexto(document, "III. DESCRIPCIÓN DE LA ACTIVIDAD REALIZADA (DETALLAR OBJETIVO, FINALIDAD):", entity.getDescripcionActividad());

            // --- IV. ALIADOS ---
            agregarBloqueTexto(document, "IV. INSTITUCIONES ALIADAS:", entity.getInstitucionesAliadas());

            // --- V. OBSERVACIONES ---
            agregarBloqueTexto(document, "V. OBSERVACIONES:", entity.getObservaciones());

            // --- VI. ACTIVIDAD OPERATIVA ---
            agregarEspacio(document);
            agregarSubtitulo(document, "VI. ACTIVIDAD OPERATIVA REALIZADA:");

            if (entity.getTareasRealizadas() != null && !entity.getTareasRealizadas().isEmpty()) {
                List<MaeTareaEntity> tareas = entity.getTareasRealizadas().stream()
                        .map(MovEventoTareaEntity::getTareaMaestra).filter(Objects::nonNull).collect(Collectors.toList());

                // Lista de Actividades
                tareas.stream().map(t -> t.getIndicador().getActividad()).distinct()
                        .forEach(a -> agregarBloqueSimple(document, a.getId() + " " + a.getDescripcion()));

                document.add(new Paragraph("a) Indicadores de la Actividad Operativa: ", FONT_NORMAL_8));
                tareas.stream().map(MaeTareaEntity::getIndicador).distinct()
                        .forEach(i -> agregarBloqueSimple(document, "     " + i.getId() + " " + i.getDescripcion()));

                document.add(new Paragraph("b) Tareas Realizadas de la Actividad Operativa: ", FONT_NORMAL_8));
                tareas.forEach(t -> agregarBloqueSimple(document, "          " + t.getId() + " " + t.getDescripcion()));
            } else {
                agregarEspacio(document);
                agregarBloqueSimpleCentrado(document, "SIN REGISTROS");
            }

            // --- VIII. ANEXOS (Salta de VI a VIII como en PHP) ---
            agregarEspacio(document);
            agregarSubtitulo(document, "VIII. ANEXOS:");

            // Links con estilo azul
            Anchor linkFicha = new Anchor("Ver formato de atención (Haz clic aquí)", FONT_LINK);
            linkFicha.setReference(baseUrl + "/descargar/anexo/" + id);
            document.add(new Paragraph(linkFicha));

            Anchor linkVideo = new Anchor("Ver videos (Haz clic aquí)", FONT_LINK);
            linkVideo.setReference(baseUrl + "/visor/ffc/videos/" + id);
            document.add(new Paragraph(linkVideo));

            Anchor linkFoto = new Anchor("Ver fotografías (Haz clic aquí)", FONT_LINK);
            linkFoto.setReference(baseUrl + "/visor/ffc/fotos/" + id);
            document.add(new Paragraph(linkFoto));

            agregarLineaSeparadora(document);

            // --- PIE ---
            // PHP: Fecha de Registro del Evento: ... Registrado por: ...
            // Todo en una fila o dos columnas
            PdfPTable tPie = new PdfPTable(2);
            tPie.setWidthPercentage(100);
            PdfPCell cFec = new PdfPCell(new Phrase("Fecha de Registro del Evento: " + (entity.getFechaRegistro() != null ? entity.getFechaRegistro() : ""), FONT_NORMAL_8));
            cFec.setBorder(Rectangle.NO_BORDER);
            tPie.addCell(cFec);

            PdfPCell cUsu = new PdfPCell(new Phrase("Registrado por: " + val(entity.getUsuarioRegistro()), FONT_NORMAL_8));
            cUsu.setBorder(Rectangle.NO_BORDER);
            tPie.addCell(cUsu);
            document.add(tPie);

            // Línea de Firma
            document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE);
            Paragraph pLinea = new Paragraph("------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8);
            pLinea.setAlignment(Element.ALIGN_CENTER);
            document.add(pLinea);

            // Nombre del usuario centrado (Firma)
            Paragraph pFirma = new Paragraph(val(entity.getUsuarioRegistro()), FONT_BOLD_8);
            pFirma.setAlignment(Element.ALIGN_CENTER);
            document.add(pFirma);

            document.close();
            return out.toByteArray();
        }
    }

    // ==========================================
    //           LÓGICA MATRIZ (TABLA)
    // ==========================================
    private void crearMatrizParticipantes(Document doc, List<MovEventoDetalleEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(14);
        table.setWidthPercentage(100);
        // Anchos aproximados
        float[] widths = new float[14];
        widths[0] = 30f; // Nombre Tipo
        for(int i=1; i<=12; i++) widths[i] = 7f; // Datos
        widths[13] = 10f; // Total
        table.setWidths(widths);

        // --- FILA 1: Rangos ---
        PdfPCell cVacia = new PdfPCell(new Phrase("")); cVacia.setBorder(Rectangle.NO_BORDER);
        table.addCell(cVacia); // Celda sobre "Tipo" vacía

        String[] rangos = {"0-17 AÑOS", "18-29 AÑOS", "30-59 AÑOS", "60+ AÑOS"};
        for (String r : rangos) {
            PdfPCell cRango = new PdfPCell(new Phrase(r, FONT_BOLD_7));
            cRango.setColspan(3);
            cRango.setHorizontalAlignment(Element.ALIGN_CENTER);
            cRango.setBackgroundColor(COLOR_CABECERA_TABLA);
            cRango.setPadding(3f);
            table.addCell(cRango);
        }
        table.addCell(cVacia); // Celda sobre Total vacía

        // --- FILA 2: Cabeceras Columnas ---
        agregarCeldaHeader(table, "Tipo de participante");
        for(int i=0; i<4; i++) {
            agregarCeldaSubHeader(table, "F");
            agregarCeldaSubHeader(table, "M");
            agregarCeldaSubHeader(table, "LGTBIQ");
        }
        agregarCeldaHeader(table, "TOTAL");

        // --- DATOS ---
        if (lista == null || lista.isEmpty()) {
            PdfPCell c = new PdfPCell(new Phrase("SIN REGISTROS", FONT_NORMAL_7));
            c.setColspan(14); c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPadding(5f);
            table.addCell(c);
        } else {
            Map<Integer, List<MovEventoDetalleEntity>> agrupado = lista.stream()
                    .collect(Collectors.groupingBy(MovEventoDetalleEntity::getTipoParticipanteId));

            for (Map.Entry<Integer, List<MovEventoDetalleEntity>> entry : agrupado.entrySet()) {
                String nombreTipo = obtenerNombreTipoParticipante(entry.getKey());
                agregarCeldaDato(table, nombreTipo, Element.ALIGN_LEFT);

                Map<String, MovEventoDetalleEntity> porRango = entry.getValue().stream()
                        .collect(Collectors.toMap(MovEventoDetalleEntity::getRangoEdad, x -> x, (p1, p2) -> p1));

                String[] codigosRango = {"01", "02", "03", "04"}; // Ajustar a tus códigos de BD
                int totalFila = 0;

                for (String cod : codigosRango) {
                    MovEventoDetalleEntity d = porRango.get(cod);
                    int f = d != null ? num(d.getCantidadFemenino()) : 0;
                    int m = d != null ? num(d.getCantidadMasculino()) : 0;
                    int l = d != null ? num(d.getCantidadLgtbiq()) : 0;

                    agregarCeldaDato(table, String.valueOf(f), Element.ALIGN_CENTER);
                    agregarCeldaDato(table, String.valueOf(m), Element.ALIGN_CENTER);
                    agregarCeldaDato(table, String.valueOf(l), Element.ALIGN_CENTER);
                    totalFila += (f + m + l);
                }
                // Total Fila
                PdfPCell cTotal = new PdfPCell(new Phrase(String.valueOf(totalFila), FONT_BOLD_7));
                cTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
                cTotal.setPadding(3f);
                table.addCell(cTotal);
            }
        }
        doc.add(table);
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
        PdfPCell c = new PdfPCell(new Phrase(texto, FONT_NORMAL_8));
        c.setBorder(Rectangle.BOX);
        c.setPadding(5f);
        if(texto.equals("SIN REGISTROS")) c.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c);
        doc.add(table);
    }

    private void agregarBloqueSimple(Document doc, String contenido) {
        try {
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            PdfPCell c = new PdfPCell(new Phrase(val(contenido), FONT_NORMAL_8));
            c.setBorder(Rectangle.BOX);
            c.setPadding(3f);
            table.addCell(c);
            doc.add(table);
        } catch (Exception e) {}
    }

    private void agregarBloqueSimpleCentrado(Document doc, String contenido) throws DocumentException {
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(contenido, FONT_NORMAL_8));
        cell.setBorder(Rectangle.BOX); cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3f);
        table.addCell(cell); doc.add(table);
    }

    private void agregarSeparador(Document doc) throws DocumentException {
        agregarLineaSeparadora(doc);
    }

    private void agregarLineaSeparadora(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
        doc.add(Chunk.NEWLINE);
    }

    private void agregarEspacio(Document doc) throws DocumentException {
        doc.add(new Paragraph(" ", FONT_NORMAL_7));
    }

    private void agregarCeldaHeader(PdfPTable t, String txt) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_BOLD_7));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setBackgroundColor(COLOR_CABECERA_TABLA);
        c.setPadding(3f);
        t.addCell(c);
    }

    private void agregarCeldaSubHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BOLD_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(COLOR_CABECERA_TABLA);
        cell.setPadding(3f);
        table.addCell(cell);
    }

    private void agregarCeldaDato(PdfPTable t, String txt, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_NORMAL_7));
        c.setHorizontalAlignment(align);
        c.setPadding(3f);
        t.addCell(c);
    }

    // ==========================================
    //              HELPERS DATA
    // ==========================================
    private String val(String s) { return s != null ? s : ""; }
    private int num(Integer i) { return i != null ? i : 0; }

    private String obtenerNombreCorte(String id) { return repoDistritoJud.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreEje(String id) { return repoEje.findById(id).map(e -> e.getDescripcion()).orElse(id); }
    private String obtenerNombreDepa(String id) { return repoDepa.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreProv(String id) { return repoProv.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreDist(String id) { return repoDist.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreTipoParticipante(Integer id) { return repoTipoPart.findById(id).map(e -> e.getDescripcion()).orElse(String.valueOf(id)); }

    // ==========================================
    //           HEADER & FOOTER EVENT
    // ==========================================
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
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
                        new Phrase("FORTALECIMIENTO DE CAPACIDADES", FONT_BOLD_10),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        pageTop - 90, 0);

                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("(INFORME)", FONT_BOLD_10),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        pageTop - 105, 0);

            } catch (Exception e) {}
        }

        public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase("Página " + writer.getPageNumber(), FONT_NORMAL_8), // PHP usa I (Italic) 8
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 20, 0);
        }
    }
}