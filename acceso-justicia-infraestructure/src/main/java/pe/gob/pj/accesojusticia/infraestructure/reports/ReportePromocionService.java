package pe.gob.pj.accesojusticia.infraestructure.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovPromCulturaDetalleEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovPromCulturaTareaEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovPromocionCulturaEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovUsuarioEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeTareaEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.MovPromocionCulturaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.MovUsuarioRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters.*;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters.*;

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
    private final MovUsuarioRepository usuarioRepo;

    // --- REPOSITORIOS MAESTROS ---
    private final MaeDistritoJudicialRepository repoDistritoJud;
    private final MaeEjeRepository repoEje;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String baseUrl;

    // --- FUENTES Y ESTILOS ---
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font FONT_BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_BOLD_7 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_NORMAL_7 = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, new Color(5, 64, 209));

    private static final Color COLOR_FONDO_TITULO = new Color(232, 232, 232);
    private static final Color COLOR_CABECERA_TABLA = new Color(240, 240, 240);

    @Transactional(readOnly = true)
    public byte[] generarPdf(Long id) throws Exception {

        MovPromocionCulturaEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Evento Promoción Cultura no encontrado: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 30, 30, 120, 30);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // --- TÍTULO CORTE ---
            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pCorte = new Paragraph(nombreCorte != null ? nombreCorte.toUpperCase() : "", FONT_TITULO);
            pCorte.setAlignment(Element.ALIGN_CENTER);
            pCorte.setSpacingAfter(10);
            document.add(pCorte);

            // --- NÚMERO DE FICHA ---
            PdfPTable tableNum = new PdfPTable(3);
            tableNum.setWidthPercentage(100);
            tableNum.setWidths(new float[]{80, 5, 15});

            PdfPCell cVacia = new PdfPCell(new Phrase(""));
            cVacia.setBorder(Rectangle.NO_BORDER);

            PdfPCell cLabelNum = new PdfPCell(new Phrase("N°:", FONT_BOLD_8));
            cLabelNum.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cLabelNum.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cLabelNum.setBorder(Rectangle.NO_BORDER);

            PdfPCell cValNum = new PdfPCell(new Phrase(val(entity.getCodigo()), FONT_NORMAL_8));
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

            agregarFila(document, "Nombre de la Actividad", ": " + val(entity.getNombreActividad()));
            agregarFila(document, "Tipo de Actividad", ": " + val(entity.getTipoActividad()));
            if ("OTROS".equalsIgnoreCase(entity.getTipoActividad())) {
                agregarFila(document, "Especifique Otros", ": " + val(entity.getTipoActividadOtros()));
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fIni = entity.getFechaInicio() != null ? entity.getFechaInicio().format(fmt) : "";
            String fFin = entity.getFechaFin() != null ? entity.getFechaFin().format(fmt) : "";

            agregarFila(document, "Fecha de Inicio", ": " + fIni);
            agregarFila(document, "Fecha de Finalización", ": " + fFin);

            agregarFila(document, "Eje de Trabajo", ": " + obtenerNombreEje(entity.getEjeId()));
            agregarFila(document, "Modalidad del Proyecto", ": " + val(entity.getModalidadProyecto()));
            agregarFila(document, "Zona de Intervención", ": " + val(entity.getZonaIntervencion()));

            agregarFila(document, "Público Objetivo", ": " + val(entity.getPublicoObjetivo()));
            if ("OTRO(ESPECIFICAR)".equalsIgnoreCase(entity.getPublicoObjetivo())) {
                agregarFila(document, "Especifique Otros", ": " + val(entity.getPublicoObjetivoOtros()));
            }

            agregarFila(document, "¿Se dictó en Lengua Nativa?", ": " + val(entity.getSeDictoLenguaNativa()));
            if ("SI".equalsIgnoreCase(entity.getSeDictoLenguaNativa())) {
                agregarFila(document, "Lengua Nativa", ": " + val(entity.getLenguaNativa()));
            }

            agregarFila(document, "¿Participaron personas con discapacidad?", ": " + val(entity.getParticiparonDiscapacitados()));
            if ("SI".equalsIgnoreCase(entity.getParticiparonDiscapacitados())) {
                agregarFila(document, "Cantidad Discapacitados", ": " + num(entity.getNumeroDiscapacitados()));
            }

            agregarLineaSeparadora(document);

            // --- I. LUGAR ---
            agregarSubtitulo(document, "I. LUGAR DE LA ACTIVIDAD:");
            agregarFila(document, "Lugar Específico", ": " + val(entity.getLugarActividad()));
            agregarFila(document, "Región", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoGeograficoId())); // Corregido getter

            agregarEspacio(document);

            // --- II. PERSONAS BENEFICIADAS (MATRIZ HORIZONTAL) ---
            agregarSubtitulo(document, "II. PERSONAS BENEFICIADAS:");
            document.add(new Paragraph("(*Número Aproximado de Asistentes)", FONT_NORMAL_8));
            agregarEspacio(document);

            crearTablaBeneficiadasHorizontal(document, entity.getPersonasBeneficiadas());

            agregarEspacio(document);

            // --- III. DESCRIPCIÓN ---
            agregarBloqueTexto(document, "III. DESCRIPCIÓN DE LA ACTIVIDAD REALIZADA:", entity.getDescripcionActividad());

            // --- IV. RECURSOS / ALIADOS ---
            agregarBloqueTexto(document, "IV. RECURSOS UTILIZADOS / INSTITUCIONES ALIADAS:", entity.getInstitucionesAliadas());

            // --- V. OBSERVACIONES ---
            agregarBloqueTexto(document, "V. OBSERVACIONES:", entity.getObservacion());

            // --- VI. ACTIVIDAD OPERATIVA ---
            agregarEspacio(document);
            agregarSubtitulo(document, "VI. ACTIVIDAD OPERATIVA REALIZADA:");

            if (entity.getTareas() != null && !entity.getTareas().isEmpty()) {
                List<MaeTareaEntity> tareas = entity.getTareas().stream()
                        .map(MovPromCulturaTareaEntity::getTareaMaestra).filter(Objects::nonNull).collect(Collectors.toList());

                // Indicadores
                document.add(new Paragraph("a) Indicadores de la Actividad Operativa:", FONT_NORMAL_8));
                tareas.stream().map(MaeTareaEntity::getIndicador).distinct()
                        .forEach(ind -> {
                            try { agregarBloqueSimple(document, "     " + ind.getId() + " " + ind.getDescripcion()); } catch (DocumentException e) {}
                        });

                // Tareas
                document.add(new Paragraph("b) Tareas Realizadas de la Actividad Operativa:", FONT_NORMAL_8));
                tareas.forEach(tar -> {
                    try { agregarBloqueSimple(document, "          " + tar.getId() + " " + tar.getDescripcion()); } catch (DocumentException e) {}
                });
            } else {
                agregarEspacio(document);
                agregarBloqueSimpleCentrado(document, "SIN REGISTROS");
            }

            // --- VII. ANEXOS ---
            agregarEspacio(document);
            agregarSubtitulo(document, "VII. ANEXOS:");

            Anchor linkFicha = new Anchor("Ver formato de atención (Haz clic aquí)", FONT_LINK);
            linkFicha.setReference(baseUrl + "/descargar/anexo/" + id);
            document.add(new Paragraph(linkFicha));

            Anchor linkVideo = new Anchor("Ver videos (Haz clic aquí)", FONT_LINK);
            linkVideo.setReference(baseUrl + "/visor/pc/videos/" + id);
            document.add(new Paragraph(linkVideo));

            Anchor linkFoto = new Anchor("Ver fotografías (Haz clic aquí)", FONT_LINK);
            linkFoto.setReference(baseUrl + "/visor/pc/fotos/" + id);
            document.add(new Paragraph(linkFoto));

            agregarLineaSeparadora(document);

            // --- PIE Y FIRMA ---
            String fReg = entity.getFRegistro() != null ? entity.getFRegistro().format(fmt) : "";
            String uReg = "No identificado";
            if (entity.getUsuarioRegistroId() != null) {
                uReg = usuarioRepo.findById(entity.getUsuarioRegistroId().intValue())
                        .map(MovUsuarioEntity::getNombreCompleto)
                        .orElse("Usuario no encontrado");
            }

            PdfPTable tPie = new PdfPTable(2);
            tPie.setWidthPercentage(100);
            PdfPCell cFec = new PdfPCell(new Phrase("Fecha de Registro: " + fReg, FONT_NORMAL_8)); cFec.setBorder(Rectangle.NO_BORDER);
            PdfPCell cUsu = new PdfPCell(new Phrase("Registrado por: " + uReg, FONT_NORMAL_8)); cUsu.setBorder(Rectangle.NO_BORDER);
            tPie.addCell(cFec); tPie.addCell(cUsu);
            document.add(tPie);

            document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE);

            Paragraph pLinea = new Paragraph("--------------------------------------------------", FONT_NORMAL_8);
            pLinea.setAlignment(Element.ALIGN_CENTER);
            document.add(pLinea);

            Paragraph pFirma = new Paragraph(uReg + "\nUSUARIO RESPONSABLE", FONT_BOLD_8);
            pFirma.setAlignment(Element.ALIGN_CENTER);
            document.add(pFirma);

            document.close();
            return out.toByteArray();
        }
    }

    // ==========================================
    //           TABLAS Y LÓGICA
    // ==========================================
    private void crearTablaBeneficiadasHorizontal(Document doc, List<MovPromCulturaDetalleEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        // Anchos proporcionales
        table.setWidths(new float[]{11,11,11, 11,11,11, 11,11,11, 12});

        // Fila 1: Grupos
        agregarCeldaHeader(table, "NIÑOS Y ADOLESCENTES", 3);
        agregarCeldaHeader(table, "JÓVENES Y ADULTOS", 3);
        agregarCeldaHeader(table, "ADULTOS MAYORES", 3);

        PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL", FONT_BOLD_7));
        cTotal.setRowspan(3); cTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
        cTotal.setBackgroundColor(COLOR_CABECERA_TABLA);
        table.addCell(cTotal);

        // Fila 2: Rangos
        agregarCeldaHeader(table, "0-17 AÑOS", 3);
        agregarCeldaHeader(table, "18-59 AÑOS", 3);
        agregarCeldaHeader(table, "60+ AÑOS", 3);

        // Fila 3: Sexo
        for(int i=0; i<3; i++) {
            agregarCeldaSubHeader(table, "F");
            agregarCeldaSubHeader(table, "M");
            agregarCeldaSubHeader(table, "LGTBIQ");
        }

        // Datos
        if (lista == null || lista.isEmpty()) {
            agregarCeldaVacia(table, 10, "SIN REGISTROS");
        } else {
            // Mapeamos por Código de Rango (01, 02, 03)
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
        PdfPCell c1 = new PdfPCell(new Phrase(label, FONT_NORMAL_8)); c1.setBorder(Rectangle.NO_BORDER); c1.setPadding(3f);
        PdfPCell c2 = new PdfPCell(new Phrase(value, FONT_NORMAL_8)); c2.setBorder(Rectangle.NO_BORDER); c2.setPadding(3f);
        table.addCell(c1); table.addCell(c2);
        doc.add(table);
    }

    private void agregarBloqueTexto(Document doc, String titulo, String contenido) throws DocumentException {
        agregarEspacio(doc);
        agregarSubtitulo(doc, titulo);
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
        String txt = (contenido == null || contenido.isBlank()) ? "SIN REGISTROS" : contenido;
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_NORMAL_8)); c.setBorder(Rectangle.BOX); c.setPadding(5f);
        if(txt.equals("SIN REGISTROS")) c.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c);
        doc.add(table);
    }

    private void agregarBloqueSimple(Document doc, String contenido) throws DocumentException {
        PdfPTable t = new PdfPTable(1); t.setWidthPercentage(100);
        PdfPCell c = new PdfPCell(new Phrase(val(contenido), FONT_NORMAL_8)); c.setBorder(Rectangle.BOX); c.setPadding(3f);
        t.addCell(c); doc.add(t);
    }

    private void agregarBloqueSimpleCentrado(Document doc, String contenido) throws DocumentException {
        PdfPTable t = new PdfPTable(1); t.setWidthPercentage(100);
        PdfPCell c = new PdfPCell(new Phrase(contenido, FONT_NORMAL_8)); c.setBorder(Rectangle.BOX); c.setHorizontalAlignment(Element.ALIGN_CENTER); c.setPadding(3f);
        t.addCell(c); doc.add(t);
    }

    private void agregarLineaSeparadora(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
        doc.add(Chunk.NEWLINE);
    }

    private void agregarEspacio(Document doc) throws DocumentException {
        doc.add(new Paragraph(" ", FONT_NORMAL_7));
    }

    // Helpers Tablas
    private void agregarCeldaHeader(PdfPTable t, String txt, int colspan) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_BOLD_7)); c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE); c.setBackgroundColor(COLOR_CABECERA_TABLA); c.setPadding(3f);
        if(colspan>1) c.setColspan(colspan);
        t.addCell(c);
    }
    private void agregarCeldaSubHeader(PdfPTable t, String txt) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_BOLD_7)); c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBackgroundColor(COLOR_CABECERA_TABLA); c.setPadding(3f);
        t.addCell(c);
    }
    private void agregarCeldaDato(PdfPTable t, String txt) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_NORMAL_7)); c.setHorizontalAlignment(Element.ALIGN_CENTER); c.setPadding(3f);
        t.addCell(c);
    }
    private void agregarCeldaDatoBold(PdfPTable t, String txt) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_BOLD_7)); c.setHorizontalAlignment(Element.ALIGN_CENTER); c.setPadding(3f);
        t.addCell(c);
    }
    private void agregarCeldaVacia(PdfPTable t, int colspan, String txt) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_NORMAL_7)); c.setColspan(colspan); c.setHorizontalAlignment(Element.ALIGN_CENTER); c.setPadding(5f);
        t.addCell(c);
    }

    // Helpers Data
    private String val(String s) { return s != null ? s : ""; }
    private int num(Integer i) { return i != null ? i : 0; }

    // Helpers Maestras (LONG)
    private String obtenerNombreCorte(Long id) { return repoDistritoJud.findById(id).map(e -> e.getNombre()).orElse(String.valueOf(id)); }
    private String obtenerNombreEje(Long id) { return repoEje.findById(id).map(e -> e.getDescripcion()).orElse(String.valueOf(id)); }
    private String obtenerNombreDepa(Long id) { return repoDepa.findById(id).map(e -> e.getNombre()).orElse(String.valueOf(id)); }
    private String obtenerNombreProv(Long id) { return repoProv.findById(id).map(e -> e.getNombre()).orElse(String.valueOf(id)); }
    private String obtenerNombreDist(Long id) { return repoDist.findById(id).map(e -> e.getNombre()).orElse(String.valueOf(id)); }

    // --- HEADER FOOTER ---
    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                float pageTop = document.getPageSize().getHeight();
                try {
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.jpg"));
                    logo.scaleToFit(500, 50);
                    logo.setAbsolutePosition(30, pageTop - 70);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}

                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("PROMOCIÓN DE LA CULTURA JURÍDICA", FONT_BOLD_10),
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
                    new Phrase("Página " + writer.getPageNumber(), FONT_NORMAL_8),
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 20, 0);
        }
    }
}