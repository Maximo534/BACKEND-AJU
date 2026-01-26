package pe.gob.pj.prueba.infraestructure.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromCulturaDetalleEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromCulturaTareaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromocionCulturaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTareaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovPromocionCulturaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service("reportePromocionService")
@RequiredArgsConstructor
public class ReportePromocionService {

    private final MovPromocionCulturaRepository repository;
    private final MaeDistritoJudicialRepository repoDistritoJud;
    private final MaeEjeRepository repoEje;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String baseUrl;

    // --- FUENTES ---
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

    // ✅ CORRECCIÓN: Agregada la fuente que faltaba
    private static final Font FONT_BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_BOLD_7 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_NORMAL_7 = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, new Color(5, 64, 209));

    // COLORES EXACTOS DEL PHP
    private static final Color COLOR_FONDO_TITULO = new Color(232, 232, 232); // El gris del PHP
    private static final Color COLOR_CABECERA_TABLA = new Color(240, 240, 240); // Gris claro para tablas

    @Transactional(readOnly = true)
    public byte[] generarPdf(String id) throws Exception {

        MovPromocionCulturaEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Evento APCJ no encontrado: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Margen superior 120 para que la cabecera no se cruce
            Document document = new Document(PageSize.A4, 30, 30, 120, 30);

            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // --- TÍTULO CORTE (Centrado) ---
            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pCorte = new Paragraph(nombreCorte.toUpperCase(), FONT_TITULO);
            pCorte.setAlignment(Element.ALIGN_CENTER);
            pCorte.setSpacingAfter(10); // Espacio después del título
            document.add(pCorte);

            // --- NÚMERO DE FICHA (Alineado a la derecha, estilo caja) ---
            // Simula: [ N°: ] [ 000123 ]
            PdfPTable tableNum = new PdfPTable(3);
            tableNum.setWidthPercentage(100);
            tableNum.setWidths(new float[]{80, 5, 15}); // Empuja todo a la derecha

            PdfPCell cVacia = new PdfPCell(new Phrase(""));
            cVacia.setBorder(Rectangle.NO_BORDER);

            PdfPCell cLabelNum = new PdfPCell(new Phrase("N°:", FONT_BOLD_8));
            cLabelNum.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cLabelNum.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cLabelNum.setBorder(Rectangle.NO_BORDER); // La "N" fuera del cuadro

            PdfPCell cValNum = new PdfPCell(new Phrase(entity.getId(), FONT_NORMAL_8));
            cValNum.setHorizontalAlignment(Element.ALIGN_CENTER);
            cValNum.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cValNum.setBorder(Rectangle.BOX); // El número dentro del cuadro
            cValNum.setPadding(3);

            tableNum.addCell(cVacia);
            tableNum.addCell(cLabelNum);
            tableNum.addCell(cValNum);
            document.add(tableNum);

            agregarEspacio(document);

            // --- DATOS GENERALES (Sin subtítulo, directo como en PHP) ---
            agregarFila(document, "Resolución Anual que Aprueba el Plan", ": N° " + val(entity.getResolucionPlanAnual()));
            agregarFila(document, "Resolución Administrativa que Aprueba el Plan", ": N° " + val(entity.getResolucionAdminPlan()));
            agregarFila(document, "Documento que Autoriza Actividad/Evento", ": N° " + val(entity.getDocumentoAutoriza()));
            agregarEspacio(document); // Separación visual

            agregarFila(document, "Nombre de la Actividad/Servicio", ": " + val(entity.getNombreActividad()));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fInicio = entity.getFechaInicio() != null ? entity.getFechaInicio().format(fmt) : "";
            String fFin = entity.getFechaFin() != null ? entity.getFechaFin().format(fmt) : "";

            agregarFila(document, "Fecha de Inicio", ": " + fInicio);
            agregarFila(document, "Fecha de Finalización", ": " + fFin);

            agregarFila(document, "Tipo de Actividad", ": " + val(entity.getTipoActividad()));
            if ("OTRO(ESPECIFICAR)".equalsIgnoreCase(entity.getTipoActividad())) {
                agregarFila(document, "Observación", ": " + val(entity.getTipoActividadOtros()));
            }

            agregarFila(document, "Detallar Zona de Intervención", ": " + val(entity.getZonaIntervencion()));
            agregarFila(document, "Modalidad", ": " + val(entity.getModalidadProyecto()));

            agregarFila(document, "Público Objetivo", ": " + val(entity.getPublicoObjetivo()));
            if ("OTRO(ESPECIFICAR)".equalsIgnoreCase(entity.getPublicoObjetivo())) {
                agregarFila(document, "Observación", ": " + val(entity.getPublicoObjetivoOtros()));
            }

            agregarLineaSeparadora(document);

            // --- I. LUGAR DE LA ACTIVIDAD ---
            // Usamos el estilo Gris del PHP
            agregarSubtitulo(document, "I. LUGAR DE LA ACTIVIDAD:");

            agregarFila(document, "Anexo/Localidad/Institución", ": " + val(entity.getLugarActividad()));
            agregarFila(document, "Región", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoGeograficoId()));

            String nombreEje = obtenerNombreEje(entity.getEjeId());
            agregarFila(document, "Eje de Trabajo", ": " + val(entity.getEjeId()) + " " + nombreEje);

            boolean esLengua = "SI".equalsIgnoreCase(entity.getSeDictoLenguaNativa());
            agregarFila(document, "¿Se atendió en Lengua Nativa?", ": " + (esLengua ? "SI" : "NO"));
            if (esLengua) {
                agregarFila(document, "Si es sí, Mencione la Lengua Nativa", ": " + val(entity.getLenguaNativaDesc()));
            }

            boolean esDiscap = "SI".equalsIgnoreCase(entity.getParticiparonDiscapacitados());
            agregarFila(document, "¿Participaron Personas con Discapacidad?", ": " + (esDiscap ? "SI" : "NO"));
            if (esDiscap) {
                agregarFila(document, "Si es SÍ, Mencione el Número", ": " + num(entity.getNumeroDiscapacitados()));
            }

            // Nota: En el PHP 'areaRiesgo' se usaba para Interprete en FFC, aquí lo mapeaste igual.
            agregarFila(document, "¿La Actividad Requirió Interprete?", ": " + val(entity.getAreaRiesgo()));

            agregarEspacio(document);

            // --- II. PERSONAS BENEFICIADAS ---
            agregarSubtitulo(document, "II. PERSONAS BENEFICIADAS:");
            document.add(new Paragraph("Número Aproximado de Asistentes a la Actividad", FONT_NORMAL_8));
            agregarEspacio(document);

            crearTablaBeneficiarios(document, entity.getPersonasBeneficiadas());

            // --- TEXTOS GRANDES ---
            agregarBloqueTexto(document, "III. DESCRIPCIÓN DE LA ACTIVIDAD REALIZADA:", entity.getDescripcionActividad());
            agregarBloqueTexto(document, "IV. INSTITUCIONES ALIADAS:", entity.getInstitucionesAliadas());
            agregarBloqueTexto(document, "V. OBSERVACIONES:", entity.getObservacion());

            // --- VII. ACTIVIDAD OPERATIVA ---
            agregarEspacio(document);
            agregarSubtitulo(document, "VII. ACTIVIDAD OPERATIVA REALIZADA:");

            if (entity.getTareas() != null && !entity.getTareas().isEmpty()) {
                // ... lógica de tareas igual ...
                List<MaeTareaEntity> tareas = entity.getTareas().stream()
                        .map(MovPromCulturaTareaEntity::getTareaMaestra)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                tareas.stream().map(t -> t.getIndicador().getActividad()).distinct()
                        .forEach(act -> {
                            try { agregarBloqueSimple(document, act.getId() + " " + act.getDescripcion()); } catch (Exception e){}
                        });

                document.add(new Paragraph("a) Indicadores de la Actividad Operativa:", FONT_NORMAL_8));
                tareas.stream().map(MaeTareaEntity::getIndicador).distinct()
                        .forEach(ind -> {
                            try { agregarBloqueSimple(document, "     " + ind.getId() + " " + ind.getDescripcion()); } catch (Exception e){}
                        });

                document.add(new Paragraph("b) Tareas Realizadas de la Actividad Operativa:", FONT_NORMAL_8));
                tareas.forEach(t -> {
                    try { agregarBloqueSimple(document, "          " + t.getId() + " " + t.getDescripcion()); } catch (Exception e){}
                });
            } else {
                agregarEspacio(document);
                agregarBloqueSimpleCentrado(document, "SIN REGISTROS");
            }

            // --- VIII. ANEXOS ---
            agregarEspacio(document);
            agregarSubtitulo(document, "VIII. ANEXOS:");

            Anchor linkFicha = new Anchor("Ver formato de atención (Haz clic aquí)", FONT_LINK);
            linkFicha.setReference(baseUrl + "/descargar/anexo/" + id);
            document.add(new Paragraph(linkFicha));

            Anchor linkVideo = new Anchor("Ver videos (Haz clic aquí)", FONT_LINK);
            linkVideo.setReference(baseUrl + "/visor/videos/" + id);
            document.add(new Paragraph(linkVideo));

            Anchor linkFoto = new Anchor("Ver fotografías (Haz clic aquí)", FONT_LINK);
            linkFoto.setReference(baseUrl + "/visor/fotos/" + id);
            document.add(new Paragraph(linkFoto));

            agregarLineaSeparadora(document);

            // --- PIE DE REPORTE ---
            agregarFila(document, "Fecha de registro", ": " + (entity.getFechaRegistro() != null ? entity.getFechaRegistro().toString() : ""));
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

    // =================================================================
    //            ESTILOS Y HELPERS VISUALES
    // =================================================================

    // 1. SUBTÍTULO CON FONDO GRIS (Estilo PHP)
    private void agregarSubtitulo(Document doc, String texto) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_BOLD_8));
        cell.setBackgroundColor(COLOR_FONDO_TITULO); // Gris
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(4f); // Espacio interno
        table.addCell(cell);
        doc.add(table);
    }

    // 2. FILA DE DATOS CON PADDING (Para que no se vea pegado)
    private void agregarFila(Document doc, String label, String value) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 65});

        PdfPCell c1 = new PdfPCell(new Phrase(label, FONT_NORMAL_8));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(3f); // Aire vertical

        PdfPCell c2 = new PdfPCell(new Phrase(value, FONT_NORMAL_8));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(3f); // Aire vertical

        table.addCell(c1); table.addCell(c2);
        doc.add(table);
    }

    private void agregarEspacio(Document doc) throws DocumentException {
        doc.add(new Paragraph(" ", FONT_NORMAL_7)); // Pequeño salto
    }

    private void agregarLineaSeparadora(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
        doc.add(Chunk.NEWLINE);
    }

    private void agregarBloqueTexto(Document doc, String titulo, String contenido) throws DocumentException {
        agregarEspacio(doc);
        agregarSubtitulo(doc, titulo); // Reutilizamos el estilo gris

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        String texto = (contenido == null || contenido.trim().isEmpty()) ? "SIN REGISTROS" : contenido;
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_NORMAL_8));
        cell.setPadding(6);
        cell.setBorder(Rectangle.BOX); // Marco alrededor del texto
        if(texto.equals("SIN REGISTROS")) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        doc.add(table);
    }

    private void agregarBloqueSimple(Document doc, String contenido) throws DocumentException {
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(val(contenido), FONT_NORMAL_8));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(3f); // Aire
        table.addCell(cell); doc.add(table);
    }

    private void agregarBloqueSimpleCentrado(Document doc, String contenido) throws DocumentException {
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(contenido, FONT_NORMAL_8));
        cell.setBorder(Rectangle.BOX); cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3f); // Aire
        table.addCell(cell); doc.add(table);
    }

    // Helpers Maestros
    private String obtenerNombreCorte(String id) { return repoDistritoJud.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreEje(String id) { return repoEje.findById(id).map(e -> e.getDescripcion()).orElse(id); }
    private String obtenerNombreDepa(String id) { return repoDepa.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreProv(String id) { return repoProv.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreDist(String id) { return repoDist.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String val(String s) { return s != null ? s : ""; }
    private int num(Integer i) { return i != null ? i : 0; }

    // =================================================================
    //            CLASE INTERNA: CABECERA Y PIE DE PÁGINA FIX
    // =================================================================
    class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                // Obtenemos la altura total de la página para calcular posiciones fijas
                float pageTop = document.getPageSize().getHeight();

                // 1. IMAGEN (LOGO)
                try {
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.jpg"));
                    logo.scaleToFit(500, 50);
                    // Posición fija: 30 px desde la izq, y 70 px desde el borde superior
                    logo.setAbsolutePosition(30, pageTop - 70);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}

                // 2. TEXTOS DE CABECERA
                // Posición fija: 90 px desde el borde superior
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("ACCIONES DE PROMOCIÓN DE CULTURA JURÍDICA", FONT_BOLD_10),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        pageTop - 90, 0);

                // Posición fija: 105 px desde el borde superior
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

    // ============================================
    //            TABLA BENEFICIARIOS
    // ============================================
    private void crearTablaBeneficiarios(Document doc, List<MovPromCulturaDetalleEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{11,11,11, 11,11,11, 11,11,11, 12});

        agregarCeldaHeader(table, "NIÑOS Y ADOLESCENTES", 3);
        agregarCeldaHeader(table, "JÓVENES Y ADULTOS", 3);
        agregarCeldaHeader(table, "ADULTOS MAYORES", 3);

        PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL", FONT_BOLD_7));
        cTotal.setRowspan(3);
        cTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
        cTotal.setBackgroundColor(COLOR_CABECERA_TABLA);
        table.addCell(cTotal);

        agregarCeldaHeader(table, "0-17 AÑOS", 3);
        agregarCeldaHeader(table, "18-59 AÑOS", 3);
        agregarCeldaHeader(table, "60+ AÑOS", 3);

        for(int i=0; i<3; i++) {
            agregarCeldaSubHeader(table, "F");
            agregarCeldaSubHeader(table, "M");
            agregarCeldaSubHeader(table, "LGTBIQ");
        }

        if (lista == null || lista.isEmpty()) {
            agregarCeldaVacia(table, 10, "SIN REGISTROS");
        } else {
            Map<String, MovPromCulturaDetalleEntity> map = lista.stream()
                    .collect(Collectors.toMap(MovPromCulturaDetalleEntity::getCodigoRango, x -> x));

            String[] codigos = {"01", "02", "03"};
            int granTotal = 0;

            for (String cod : codigos) {
                MovPromCulturaDetalleEntity item = map.get(cod);
                int f = item != null ? num(item.getCantidadFemenino()) : 0;
                int m = item != null ? num(item.getCantidadMasculino()) : 0;
                int l = item != null ? num(item.getCantidadLgtbiq()) : 0;

                agregarCeldaDato(table, String.valueOf(f));
                agregarCeldaDato(table, String.valueOf(m));
                agregarCeldaDato(table, String.valueOf(l));
                granTotal += (f + m + l);
            }
            agregarCeldaDatoBold(table, String.valueOf(granTotal));
        }
        doc.add(table);
    }

    private void agregarCeldaHeader(PdfPTable table, String text, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BOLD_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(COLOR_CABECERA_TABLA);
        cell.setPadding(3f);
        if (colspan > 1) cell.setColspan(colspan);
        table.addCell(cell);
    }

    private void agregarCeldaSubHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BOLD_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(COLOR_CABECERA_TABLA);
        cell.setPadding(3f);
        table.addCell(cell);
    }

    private void agregarCeldaDato(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_NORMAL_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3f);
        table.addCell(cell);
    }

    private void agregarCeldaDatoBold(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BOLD_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3f);
        table.addCell(cell);
    }

    private void agregarCeldaVacia(PdfPTable table, int colspan, String msj) {
        PdfPCell cell = new PdfPCell(new Phrase(msj, FONT_NORMAL_7));
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
}