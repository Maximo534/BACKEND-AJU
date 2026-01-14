package pe.gob.pj.prueba.infraestructure.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeActividadOperativaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeIndicadorEntity;
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

    // Fuentes Réplica PHP (Arial -> Helvetica en PDF)
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_BOLD_7 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_NORMAL_7 = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, new Color(5, 64, 209));
    private static final Color COLOR_BLANCO = Color.WHITE;

    @Transactional(readOnly = true)
    public byte[] generarPdf(String id) throws Exception {

        MovEventoFcEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Evento FFC no encontrado: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // --- TÍTULO DISTRITO ---
            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pCorte = new Paragraph(nombreCorte, FONT_TITULO); // PHP: Arial 12 Bold Centered
            pCorte.setAlignment(Element.ALIGN_CENTER);
            pCorte.setSpacingAfter(5f);
            document.add(pCorte);

            // --- NÚMERO (Alineado derecha) ---
            PdfPTable tableNum = new PdfPTable(2);
            tableNum.setWidthPercentage(100);
            tableNum.setWidths(new float[]{85, 15});
            PdfPCell cVacia = new PdfPCell(new Phrase("")); cVacia.setBorder(Rectangle.NO_BORDER);

            // Recuadro con el ID
            PdfPCell cNum = new PdfPCell(new Phrase(entity.getId(), FONT_BOLD_8));
            cNum.setHorizontalAlignment(Element.ALIGN_CENTER);
            cNum.setBorder(Rectangle.BOX);

            // Etiqueta "N°: "
            PdfPTable tRight = new PdfPTable(2);
            tRight.setWidths(new float[]{30, 70});
            PdfPCell cLbl = new PdfPCell(new Phrase("N°: ", FONT_BOLD_8)); cLbl.setBorder(Rectangle.NO_BORDER); cLbl.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tRight.addCell(cLbl);
            tRight.addCell(cNum);

            PdfPCell cWrapper = new PdfPCell(tRight); cWrapper.setBorder(Rectangle.NO_BORDER);

            tableNum.addCell(cVacia);
            tableNum.addCell(cWrapper);
            document.add(tableNum);

            // --- DATOS GENERALES ---
            // PHP imprime etiquetas directas
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

            agregarSeparador(document);

            // --- I. LUGAR ---
            document.add(new Paragraph("I. LUGAR DE LA ACTIVIDAD (SOLO PRESENCIAL): ", FONT_BOLD_8));
            agregarFila(document, "Nombre de la Institución", ": " + val(entity.getNombreInstitucion()));
            agregarFila(document, "Región", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoGeograficoId()));

            document.add(Chunk.NEWLINE);

            // --- II. PARTICIPANTES (MATRIZ) ---
            document.add(new Paragraph("II. N° DE PARTICIPANTES POR RANGO DE EDAD Y GÉNERO: ", FONT_BOLD_8));
            document.add(Chunk.NEWLINE);
            crearMatrizParticipantes(document, entity.getParticipantes());
            document.add(Chunk.NEWLINE);

            // --- III. DESCRIPCIÓN ---
            document.add(new Paragraph("III. DESCRIPCIÓN DE LA ACTIVIDAD REALIZADA (DETALLAR OBJETIVO,FINALIDAD) ", FONT_BOLD_8));
            agregarBloqueTexto(document, entity.getDescripcionActividad());

            // --- IV. ALIADOS ---
            document.add(new Paragraph("IV. INSTITUCIONES ALIADAS: ", FONT_BOLD_8));
            agregarBloqueTexto(document, entity.getInstitucionesAliadas());

            // --- V. OBSERVACIONES ---
            document.add(new Paragraph("V. OBSERVACIONES: ", FONT_BOLD_8));
            agregarBloqueTexto(document, entity.getObservaciones());

            // --- VI. ACTIVIDAD OPERATIVA ---
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("VI. ACTIVIDAD OPERATIVA REALIZADA: ", FONT_BOLD_8));

            if (entity.getTareas() != null && !entity.getTareas().isEmpty()) {
                List<MaeTareaEntity> tareas = entity.getTareas().stream()
                        .map(MovEventoTareaEntity::getTareaMaestra).filter(Objects::nonNull).collect(Collectors.toList());

                // Lista de Actividades
                tareas.stream().map(t -> t.getIndicador().getActividad()).distinct()
                        .forEach(a -> agregarItemLista(document, a.getId() + " " + a.getDescripcion()));

                // a) Indicadores
                document.add(new Paragraph("a) Indicadores de la Actividad Operativa: ", FONT_NORMAL_8));
                tareas.stream().map(MaeTareaEntity::getIndicador).distinct()
                        .forEach(i -> agregarItemLista(document, "     " + i.getId() + " " + i.getDescripcion()));

                // b) Tareas
                document.add(new Paragraph("b) Tareas Realizadas de la Actividad Operativa: ", FONT_NORMAL_8));
                tareas.forEach(t -> agregarItemLista(document, "          " + t.getId() + " " + t.getDescripcion()));
            } else {
                agregarBloqueTexto(document, "SIN REGISTROS");
            }

            document.add(Chunk.NEWLINE);

            // --- VIII. ANEXOS (Salta de VI a VIII como en PHP) ---
            document.add(new Paragraph("VIII. ANEXOS: ", FONT_BOLD_8));

            // Links
            agregarLink(document, "Ver formato de atención (Haz clic aquí)", baseUrl + "/publico/v1/fortalecimiento/anexo/" + id);
            agregarLink(document, "Ver videos (Haz clic aquí)", baseUrl + "/visor/ffc/videos/" + id);
            agregarLink(document, "Ver fotografías (Haz clic aquí)", baseUrl + "/visor/ffc/fotos/" + id);

            agregarSeparador(document);

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
        // La tabla tiene: Columna Tipo (Ancha) + 4 Rangos * 3 Cols (12) + Total (1) = 14 Columnas
        // PHP headers: 0-17, 18-29, 30-59, 60+

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
        table.addCell(cVacia); // Celda sobre "Tipo" vacía en esta fila

        String[] rangos = {"0-17 AÑOS", "18-29 AÑOS", "30-59 AÑOS", "60+ AÑOS"};
        for (String r : rangos) {
            PdfPCell cRango = new PdfPCell(new Phrase(r, FONT_BOLD_7));
            cRango.setColspan(3);
            cRango.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cRango);
        }
        table.addCell(cVacia); // Celda sobre Total vacía

        // --- FILA 2: Cabeceras Columnas ---
        agregarCeldaHeader(table, "Tipo de participante");
        for(int i=0; i<4; i++) {
            agregarCeldaHeader(table, "F");
            agregarCeldaHeader(table, "M");
            agregarCeldaHeader(table, "LGTBIQ");
        }
        agregarCeldaHeader(table, "TOTAL");

        // --- DATOS ---
        if (lista == null || lista.isEmpty()) {
            PdfPCell c = new PdfPCell(new Phrase("SIN REGISTROS", FONT_NORMAL_7));
            c.setColspan(14); c.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c);
        } else {
            // Agrupar por Tipo de Participante
            Map<Integer, List<MovEventoDetalleEntity>> agrupado = lista.stream()
                    .collect(Collectors.groupingBy(MovEventoDetalleEntity::getTipoParticipanteId));

            int granTotal = 0;

            for (Map.Entry<Integer, List<MovEventoDetalleEntity>> entry : agrupado.entrySet()) {
                String nombreTipo = obtenerNombreTipoParticipante(entry.getKey());
                agregarCeldaDato(table, nombreTipo, Element.ALIGN_LEFT);

                // Mapear por rangoEdad ("01", "02", "03", "04")
                // Asumo que en BD guardas códigos "01", "02"... o textos.
                // Ajustar mapeo según tu data real. Aquí asumo mapeo directo o lógica de negocio.
                // Usaré un mapa temporal para buscar rápido.
                Map<String, MovEventoDetalleEntity> porRango = entry.getValue().stream()
                        .collect(Collectors.toMap(MovEventoDetalleEntity::getRangoEdad, x -> x, (p1, p2) -> p1));

                String[] codigosRango = {"01", "02", "03", "04"}; // Ajustar a tus códigos reales
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
                table.addCell(cTotal);
                granTotal += totalFila;
            }

            // Fila Final (Total General) si se desea, el PHP pone el total general abajo a la derecha
            // Aquí lo dejaremos limpio.
        }
        doc.add(table);
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
        PdfPCell c = new PdfPCell(new Phrase((texto != null && !texto.isBlank()) ? texto : " ", FONT_NORMAL_8));
        c.setBorder(Rectangle.BOX);
        c.setPadding(5f);
        table.addCell(c);
        doc.add(table);
    }

    private void agregarItemLista(Document doc, String texto) {
        try {
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            PdfPCell c = new PdfPCell(new Phrase(texto, FONT_NORMAL_8));
            c.setBorder(Rectangle.NO_BORDER); // PHP usa MultiCell con borde 1, aquí lista simple
            c.setBorder(Rectangle.BOX); // Replica el borde de PHP para las tareas
            table.addCell(c);
            doc.add(table);
        } catch (Exception e) {}
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

    private void agregarCeldaHeader(PdfPTable t, String txt) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_NORMAL_7)); // PHP no usa bold en headers de tabla participante
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(c);
    }

    private void agregarCeldaDato(PdfPTable t, String txt, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_NORMAL_7));
        c.setHorizontalAlignment(align);
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
                // Logo
                try {
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.JPG"));
                    logo.scaleToFit(300, 50);
                    logo.setAbsolutePosition(25, document.getPageSize().getHeight() - 50);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}

                // Títulos Cabecera
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("FORTALECIMIENTO DE CAPACIDADES", FONT_BOLD_8),
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
                    new Phrase("Página " + writer.getPageNumber(), FONT_NORMAL_7), // PHP usa I (Italic) 8
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 15, 0);
        }
    }
}