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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service("reporteFortalecimientoService")
@RequiredArgsConstructor
public class ReporteFortalecimientoService {

    private final MovEventoFcRepository repository;

    // ✅ INYECCIÓN DE REPOSITORIOS
    private final MaeDistritoJudicialRepository repoDistritoJud;
    private final MaeEjeRepository repoEje;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;
    private final MaeTipoParticipanteRepository repoTipoPart;

    // ✅ INYECCIÓN DE URL BASE
    @Value("${app.frontend.url:http://localhost:8080}")
    private String baseUrl;

    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_BOLD_7 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_NORMAL_7 = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, Color.BLUE);
    private static final Color COLOR_CABECERA = new Color(240, 240, 240);

    @Transactional(readOnly = true)
    public byte[] generarPdf(String id) throws Exception {

        MovEventoFcEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Evento FFC no encontrado: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            Paragraph pTitulo = new Paragraph("FORTALECIMIENTO DE CAPACIDADES", FONT_TITULO);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pCorte = new Paragraph("DISTRITO JUDICIAL: " + nombreCorte, FONT_BOLD_10);
            pCorte.setAlignment(Element.ALIGN_CENTER);
            document.add(pCorte);

            PdfPTable tableInfo = new PdfPTable(2);
            tableInfo.setWidthPercentage(100);
            tableInfo.setWidths(new float[]{85, 15});
            tableInfo.addCell(crearCeldaVacia());

            PdfPCell cellNum = new PdfPCell(new Phrase(entity.getId(), FONT_BOLD_8));
            cellNum.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNum.setBorderWidth(1);
            tableInfo.addCell(cellNum);
            document.add(tableInfo);

            document.add(Chunk.NEWLINE);

            agregarFila(document, "Resolución Anual Plan", ": " + val(entity.getResolucionPlanAnual()));
            agregarFila(document, "Resolución Admin Plan", ": " + val(entity.getResolucionAdminPlan()));
            agregarFila(document, "Doc. Autoriza Evento", ": " + val(entity.getDocumentoAutoriza()));

            agregarFila(document, "Tipo de Evento", ": " + val(entity.getTipoEvento()));
            agregarFila(document, "Nombre del Evento", ": " + val(entity.getNombreEvento()));

            agregarFila(document, "Fecha Inicio", ": " + entity.getFechaInicio());
            agregarFila(document, "Fecha Fin", ": " + entity.getFechaFin());

            String nombreEje = obtenerNombreEje(entity.getEjeId());
            agregarFila(document, "Eje Temático", ": " + nombreEje);

            agregarFila(document, "Modalidad", ": " + val(entity.getModalidad()));

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("I. DATOS DE LA ACTIVIDAD:", FONT_BOLD_8));

            agregarFila(document, "Lugar / Institución", ": " + val(entity.getNombreInstitucion()));

            agregarFila(document, "Región", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoGeograficoId()));

            agregarFila(document, "Público Objetivo", ": " + val(entity.getPublicoObjetivo()));
            if(entity.getPublicoObjetivoDetalle() != null && !entity.getPublicoObjetivoDetalle().equals("NINGUNO")){
                agregarFila(document, "Detalle Público", ": " + entity.getPublicoObjetivoDetalle());
            }

            agregarFila(document, "Docente/Expositor", ": " + val(entity.getDocenteExpositor()));

            String duracion = (entity.getDuracionHoras() != null) ? String.valueOf(entity.getDuracionHoras()) : "0";
            agregarFila(document, "Duración (Horas)", ": " + duracion);

            String sesiones = (entity.getNumeroSesiones() != null) ? String.valueOf(entity.getNumeroSesiones()) : "0";
            agregarFila(document, "N° Sesiones", ": " + sesiones);

            agregarFila(document, "Cuenta con Intérprete", ": " + val(entity.getInterpreteSenias()));

            String discap = (entity.getNumeroDiscapacitados() != null) ? String.valueOf(entity.getNumeroDiscapacitados()) : "0";
            agregarFila(document, "Part. con Discapacidad", ": " + discap);

            agregarFila(document, "¿Lengua Nativa?", ": " + val(entity.getSeDictoLenguaNativa()));
            if("SI".equals(entity.getSeDictoLenguaNativa())) {
                agregarFila(document, "Desc. Lengua Nativa", ": " + val(entity.getLenguaNativaDesc()));
            }

            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("II. DETALLE DE PARTICIPANTES:", FONT_BOLD_8));
            document.add(Chunk.NEWLINE);

            crearTablaParticipantes(document, entity.getParticipantes());

            agregarBloqueTexto(document, "III. DESCRIPCIÓN DE LA ACTIVIDAD:", entity.getDescripcionActividad());
            agregarBloqueTexto(document, "IV. INSTITUCIONES ALIADAS:", entity.getInstitucionesAliadas());
            agregarBloqueTexto(document, "V. OBSERVACIONES:", entity.getObservaciones());

            if (entity.getTareas() != null && !entity.getTareas().isEmpty()) {
                List<MaeTareaEntity> tareasRealizadas = entity.getTareas().stream()
                        .map(MovEventoTareaEntity::getTareaMaestra)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                List<MaeActividadOperativaEntity> actividadesUnicas = tareasRealizadas.stream()
                        .map(t -> t.getIndicador().getActividad())
                        .distinct()
                        .collect(Collectors.toList());

                List<MaeIndicadorEntity> indicadoresUnicos = tareasRealizadas.stream()
                        .map(MaeTareaEntity::getIndicador)
                        .distinct()
                        .collect(Collectors.toList());

                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("VI. ACTIVIDAD OPERATIVA REALIZADA:", FONT_BOLD_8));
                document.add(Chunk.NEWLINE);

                for (MaeActividadOperativaEntity act : actividadesUnicas) {
                    agregarBloqueSimple(document, act.getId() + " " + act.getDescripcion());
                }

                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("a) Indicadores de la Actividad Operativa:", FONT_NORMAL_8));
                for (MaeIndicadorEntity ind : indicadoresUnicos) {
                    agregarBloqueSimple(document, "     " + ind.getId() + " " + ind.getDescripcion());
                }

                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("b) Tareas Realizadas de la Actividad Operativa:", FONT_NORMAL_8));
                for (MaeTareaEntity tar : tareasRealizadas) {
                    agregarBloqueSimple(document, "          " + tar.getId() + " " + tar.getDescripcion());
                }
            } else {
                document.add(Chunk.NEWLINE);
                agregarBloqueSimpleCentrado(document, "SIN REGISTROS");
            }

            // --- VII. ANEXOS (CON SALTO DE LÍNEA) ---
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("VII. ANEXOS:", FONT_BOLD_8));

            // 1. Enlace al PDF (Backend)
            Anchor linkFicha = new Anchor("Ver formato de atención (Haz clic aquí)", FONT_LINK);
            linkFicha.setReference(baseUrl + "/publico/v1/fortalecimiento/anexo/" + id);
            document.add(new Paragraph(linkFicha)); // ✅ Salto de línea

            // 2. Enlace a Videos (Frontend Visor)
            Anchor linkVideos = new Anchor("Ver videos (Haz clic aquí)", FONT_LINK);
            linkVideos.setReference(baseUrl + "/visor/videos/" + id);
            document.add(new Paragraph(linkVideos)); // ✅ Salto de línea

            // 3. Enlace a Fotos (Frontend Visor)
            Anchor linkFotos = new Anchor("Ver fotografías (Haz clic aquí)", FONT_LINK);
            linkFotos.setReference(baseUrl + "/visor/fotos/" + id);
            document.add(new Paragraph(linkFotos)); // ✅ Salto de línea

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
            agregarFila(document, "Fecha de registro", ": " + entity.getFechaRegistro());
            agregarFila(document, "Registrado por", ": " + entity.getUsuarioRegistro());

            document.close();
            return out.toByteArray();
        }
    }

    // ============================================
    //        HELPERS DE BÚSQUEDA DE NOMBRES
    // ============================================

    private String obtenerNombreCorte(String id) {
        if (id == null) return "";
        return repoDistritoJud.findById(id).map(e -> e.getNombre()).orElse(id);
    }

    private String obtenerNombreEje(String id) {
        if (id == null) return "";
        return repoEje.findById(id).map(e -> e.getDescripcion()).orElse(id);
    }

    private String obtenerNombreDepa(String id) {
        if (id == null) return "";
        return repoDepa.findById(id).map(e -> e.getNombre()).orElse(id);
    }

    private String obtenerNombreProv(String id) {
        if (id == null) return "";
        return repoProv.findById(id).map(e -> e.getNombre()).orElse(id);
    }

    private String obtenerNombreDist(String id) {
        if (id == null) return "";
        return repoDist.findById(id).map(e -> e.getNombre()).orElse(id);
    }

    private String obtenerNombreTipoParticipante(Integer id) {
        if (id == null) return "";
        return repoTipoPart.findById(id).map(e -> e.getDescripcion()).orElse(String.valueOf(id));
    }

    // ============================================
    //            MÉTODOS DE TABLAS
    // ============================================

    private void crearTablaParticipantes(Document doc, List<MovEventoDetalleEntity> detalles) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 25, 15, 15, 15});

        agregarCeldaEncabezado(table, "TIPO PARTICIPANTE");
        agregarCeldaEncabezado(table, "RANGO / GRUPO");
        agregarCeldaEncabezado(table, "FEMENINO");
        agregarCeldaEncabezado(table, "MASCULINO");
        agregarCeldaEncabezado(table, "LGTBIQ");

        int tF = 0, tM = 0, tL = 0;

        if (detalles != null && !detalles.isEmpty()) {
            for (MovEventoDetalleEntity d : detalles) {
                String nombreTipo = obtenerNombreTipoParticipante(d.getTipoParticipanteId());
                agregarCeldaDato(table, nombreTipo);

                agregarCeldaDato(table, val(d.getRangoEdad()));
                agregarCeldaDato(table, String.valueOf(d.getCantidadFemenino()));
                agregarCeldaDato(table, String.valueOf(d.getCantidadMasculino()));
                agregarCeldaDato(table, String.valueOf(d.getCantidadLgtbiq()));

                tF += (d.getCantidadFemenino() == null ? 0 : d.getCantidadFemenino());
                tM += (d.getCantidadMasculino() == null ? 0 : d.getCantidadMasculino());
                tL += (d.getCantidadLgtbiq() == null ? 0 : d.getCantidadLgtbiq());
            }
        } else {
            PdfPCell cellVacia = new PdfPCell(new Phrase("SIN REGISTROS", FONT_NORMAL_7));
            cellVacia.setColspan(5);
            cellVacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cellVacia);
        }

        PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL", FONT_BOLD_7));
        cTotal.setColspan(2);
        cTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cTotal.setBackgroundColor(COLOR_CABECERA);
        table.addCell(cTotal);

        agregarCeldaDatoBold(table, String.valueOf(tF));
        agregarCeldaDatoBold(table, String.valueOf(tM));
        agregarCeldaDatoBold(table, String.valueOf(tL));

        doc.add(table);
    }

    // --- Helpers Genéricos ---

    private void agregarFila(Document doc, String label, String value) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 65});
        PdfPCell c1 = new PdfPCell(new Phrase(label, FONT_NORMAL_8)); c1.setBorder(Rectangle.NO_BORDER);
        PdfPCell c2 = new PdfPCell(new Phrase(value, FONT_NORMAL_8)); c2.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1); table.addCell(c2);
        doc.add(table);
    }

    private void agregarBloqueTexto(Document doc, String titulo, String contenido) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(titulo, FONT_BOLD_8));
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(val(contenido), FONT_NORMAL_8)); cell.setPadding(6);
        table.addCell(cell); doc.add(table);
    }

    private void agregarBloqueSimple(Document doc, String contenido) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(val(contenido), FONT_NORMAL_8));
        cell.setBorder(Rectangle.BOX);
        table.addCell(cell);
        doc.add(table);
    }

    private void agregarBloqueSimpleCentrado(Document doc, String contenido) throws DocumentException {
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(contenido, FONT_NORMAL_8));
        cell.setBorder(Rectangle.BOX); cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell); doc.add(table);
    }

    private void agregarCeldaEncabezado(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_BOLD_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(COLOR_CABECERA);
        table.addCell(cell);
    }

    private void agregarCeldaDato(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_NORMAL_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void agregarCeldaDatoBold(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_BOLD_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private PdfPCell crearCeldaVacia() {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private String val(String s) { return s != null ? s : ""; }

    class HeaderFooterPageEvent extends PdfPageEventHelper {
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                try {
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.JPG"));
                    logo.scaleToFit(500, 50);
                    logo.setAbsolutePosition(30, document.getPageSize().getHeight() - 70);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}

                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("(INFORME)", FONT_BOLD_10),
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