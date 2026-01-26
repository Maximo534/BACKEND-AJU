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
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovLlapanchikpaqJusticiaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service("reporteLljService")
@RequiredArgsConstructor
public class ReporteLljService {

    private final MovLlapanchikpaqJusticiaRepository repository;

    // Repositorios Maestros
    private final MaeDistritoJudicialRepository repoCorte;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;
    private final MaeMateriaRepository repoMateria;
    private final MaeTipoVulnerabilidadRepository repoVuln;
    private final MaeEjeRepository repoEje; // Si se usa, aunque LLJ a veces no lo tiene explícito en BD según script

    @Value("${app.frontend.url:http://localhost:4200}")
    private String baseUrl;

    // --- FUENTES & ESTILOS ---
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font FONT_BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_BOLD_7 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_NORMAL_7 = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, new Color(5, 64, 209));

    // COLORES
    private static final Color COLOR_FONDO_TITULO = new Color(232, 232, 232);
    private static final Color COLOR_CABECERA_TABLA = new Color(240, 240, 240);

    @Transactional(readOnly = true)
    public byte[] generarFichaLlj(String id) throws Exception {

        MovLlapanchikpaqJusticiaEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Evento LLJ no encontrado: " + id));

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

            // --- NÚMERO DE FICHA (Caja derecha) ---
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

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fInicio = entity.getFechaInicio() != null ? entity.getFechaInicio().format(fmt) : "";
            agregarFila(document, "Fecha de Inicio", ": " + fInicio);

            // --- I. LUGAR DE LA ACTIVIDAD ---
            agregarLineaSeparadora(document);
            agregarSubtitulo(document, "I. LUGAR DE LA ACTIVIDAD:");

            agregarFila(document, "Anexo/Localidad/Institución", ": " + val(entity.getLugarActividad()));
            agregarFila(document, "Región", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoGeograficoId()));

            agregarFila(document, "N° de Mesa de Partes Instaladas", ": " + num(entity.getNumMesasInstaladas())); // Ajustar nombre según entidad real
            // Nota: Si en tu entidad LLJ no tienes numMesasInstaladas, comenta estas líneas.
            // Según DB2.sql: MovLlapanchikpaqJusticiaEntity tiene n_num_muj_indg, n_per_quech_aymar, n_juez_quech_aymar
            // Ajusta aquí los campos específicos de LLJ:

            agregarFila(document, "N° de Mesa de Partes Instaladas", ": " + num(entity.getNumMesasInstaladas()));
            agregarFila(document, "N° de Servidores que Brindaron Atención", ": " + num(entity.getNumServidores()));
            agregarFila(document, "N° de Jueces que Brindaron Atención", ": " + num(entity.getNumJueces()));

            boolean esLengua = "SI".equalsIgnoreCase(entity.getUsoLenguaNativa()) || "01".equals(entity.getUsoLenguaNativa());
            agregarFila(document, "¿Se atendió en Lengua Nativa?", ": " + (esLengua ? "SI" : "NO"));
            if (esLengua) {
                agregarFila(document, "Si es sí, Mencione la Lengua Nativa", ": " + val(entity.getUsoLenguaNativa()));
            }

            agregarEspacio(document);

            // --- II. PERSONAS BENEFICIADAS ---
            agregarSubtitulo(document, "II. PERSONAS BENEFICIADAS:");
            document.add(new Paragraph("(*Número Aproximado de Asistentes)", FONT_NORMAL_8));
            agregarEspacio(document);

            crearTablaBeneficiadasHorizontal(document, entity.getBeneficiadas()); // Ajustar getter
            agregarEspacio(document);

            // --- III. PERSONAS ATENDIDAS ---
            agregarSubtitulo(document, "III. PERSONAS ATENDIDAS:");
            document.add(new Paragraph("a) Por Tipo de Vulnerabilidad, Rango de Edad y Género:", FONT_NORMAL_8));
            agregarEspacio(document);

            crearTablaAtendidasMatriz(document, entity.getAtendidas()); // Ajustar getter

            agregarEspacio(document);
            document.add(new Paragraph("b) Por Casos Atendidos:", FONT_NORMAL_8));
            agregarEspacio(document);

            crearTablaCasos(document, entity.getCasos()); // Ajustar getter

            // --- TEXTOS ---
            agregarBloqueTexto(document, "IV. DERIVACIÓN DE CASOS:", entity.getDerivacion());
            agregarBloqueTexto(document, "V. IMPACTO DE LA ACTIVIDAD:", entity.getDescripcionActividad());
            agregarBloqueTexto(document, "VI. OBSERVACIONES:", entity.getObservacion());

            // --- VII. ACTIVIDAD OPERATIVA ---
            agregarEspacio(document);
            agregarSubtitulo(document, "VII. ACTIVIDAD OPERATIVA REALIZADA:");

            if (entity.getTareas() != null && !entity.getTareas().isEmpty()) {
                List<MaeTareaEntity> tareas = entity.getTareas().stream()
                        .map(MovLljTareaRealizadasEntity::getTareaMaestra).filter(Objects::nonNull).collect(Collectors.toList());

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
            linkVideo.setReference(baseUrl + "/visor/llj/videos/" + id);
            document.add(new Paragraph(linkVideo));

            Anchor linkFoto = new Anchor("Ver fotografías (Haz clic aquí)", FONT_LINK);
            linkFoto.setReference(baseUrl + "/visor/llj/fotos/" + id);
            document.add(new Paragraph(linkFoto));

            agregarLineaSeparadora(document);

            // --- PIE ---
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

    // ============================================
    //            TABLAS Y LÓGICA (Con Estilos)
    // ============================================

    private void crearTablaBeneficiadasHorizontal(Document doc, List<MovLljPersonasBeneficiadasEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{11,11,11, 11,11,11, 11,11,11, 12});

        agregarCeldaHeader(table, "NIÑOS Y ADOLESCENTES", 3);
        agregarCeldaHeader(table, "JÓVENES Y ADULTOS", 3);
        agregarCeldaHeader(table, "ADULTOS MAYORES", 3);

        PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL", FONT_BOLD_7));
        cTotal.setRowspan(3); cTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cTotal.setHorizontalAlignment(Element.ALIGN_CENTER); cTotal.setBackgroundColor(COLOR_CABECERA_TABLA);
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
            Map<String, MovLljPersonasBeneficiadasEntity> map = lista.stream()
                    .collect(Collectors.toMap(MovLljPersonasBeneficiadasEntity::getCodigoRango, x -> x));

            String[] codigos = {"01", "02", "03"};
            int granTotal = 0;

            for (String cod : codigos) {
                MovLljPersonasBeneficiadasEntity item = map.get(cod);
                int f = item != null ? num(item.getCantFemenino()) : 0;
                int m = item != null ? num(item.getCantMasculino()) : 0;
                int l = item != null ? num(item.getCantLgtbiq()) : 0;

                agregarCeldaDato(table, String.valueOf(f));
                agregarCeldaDato(table, String.valueOf(m));
                agregarCeldaDato(table, String.valueOf(l));
                granTotal += (f + m + l);
            }
            agregarCeldaDatoBold(table, String.valueOf(granTotal));
        }
        doc.add(table);
    }

    private void crearTablaAtendidasMatriz(Document doc, List<MovLljPersonasAtendidasEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(14);
        table.setWidthPercentage(100);
        float[] widths = new float[14];
        widths[0] = 20f;
        for(int i=1; i<=12; i++) widths[i] = 5f;
        widths[13] = 8f;
        table.setWidths(widths);

        PdfPCell cTitulo = new PdfPCell(new Phrase("TIPO DE VULNERABILIDAD", FONT_BOLD_7));
        cTitulo.setRowspan(3); cTitulo.setBackgroundColor(COLOR_CABECERA_TABLA); cTitulo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cTitulo);

        agregarCeldaHeader(table, "NIÑOS Y ADOLESCENTES", 3);
        agregarCeldaHeader(table, "JÓVENES", 3);
        agregarCeldaHeader(table, "ADULTOS", 3);
        agregarCeldaHeader(table, "ADULTOS MAYORES", 3);

        PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL", FONT_BOLD_7));
        cTotal.setRowspan(3); cTotal.setBackgroundColor(COLOR_CABECERA_TABLA); cTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cTotal);

        agregarCeldaHeader(table, "0-17 AÑOS", 3);
        agregarCeldaHeader(table, "18-29 AÑOS", 3);
        agregarCeldaHeader(table, "30-59 AÑOS", 3);
        agregarCeldaHeader(table, "60+ AÑOS", 3);

        for(int i=0; i<4; i++) {
            agregarCeldaSubHeader(table, "F");
            agregarCeldaSubHeader(table, "M");
            agregarCeldaSubHeader(table, "L");
        }

        if (lista == null || lista.isEmpty()) {
            agregarCeldaVacia(table, 14, "SIN REGISTROS");
        } else {
            Map<Integer, List<MovLljPersonasAtendidasEntity>> porVuln = lista.stream()
                    .collect(Collectors.groupingBy(MovLljPersonasAtendidasEntity::getTipoVulnerabilidadId));

            for (Map.Entry<Integer, List<MovLljPersonasAtendidasEntity>> entry : porVuln.entrySet()) {
                String nombreVuln = obtenerNombreVulnerabilidad(entry.getKey());
                agregarCeldaDato(table, nombreVuln);

                Map<String, MovLljPersonasAtendidasEntity> porRango = entry.getValue().stream()
                        .collect(Collectors.toMap(MovLljPersonasAtendidasEntity::getRangoEdad, x->x));

                String[] rangos = {"01", "02", "03", "04"};
                int totalFila = 0;

                for (String r : rangos) {
                    MovLljPersonasAtendidasEntity data = porRango.get(r);
                    int f = data != null ? num(data.getCantidadFemenino()) : 0;
                    int m = data != null ? num(data.getCantidadMasculino()) : 0;
                    int l = data != null ? num(data.getCantidadLgtbiq()) : 0;

                    agregarCeldaDato(table, String.valueOf(f));
                    agregarCeldaDato(table, String.valueOf(m));
                    agregarCeldaDato(table, String.valueOf(l));
                    totalFila += (f + m + l);
                }
                agregarCeldaDatoBold(table, String.valueOf(totalFila));
            }
        }
        doc.add(table);
    }

    private void crearTablaCasos(Document doc, List<MovLljCasosAtendidosEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);

        agregarCeldaHeader(table, "MATERIA", 1);
        agregarCeldaSubHeader(table, "Dem.");
        agregarCeldaSubHeader(table, "Aud.");
        agregarCeldaSubHeader(table, "Sent.");
        agregarCeldaSubHeader(table, "Proc.");
        agregarCeldaSubHeader(table, "Notif.");
        agregarCeldaSubHeader(table, "Orien.");
        agregarCeldaHeader(table, "TOTAL", 1);

        if (lista == null || lista.isEmpty()) {
            agregarCeldaVacia(table, 8, "SIN REGISTROS");
        } else {
            int tDem=0, tAud=0, tSen=0, tPro=0, tNot=0, tOri=0, tGen=0;

            for (MovLljCasosAtendidosEntity item : lista) {
                String materia = obtenerNombreMateria(item.getMateriaId());
                agregarCeldaDato(table, materia);

                int dem = num(item.getCantidadDemandas()); tDem+=dem;
                int aud = num(item.getCantidadAudiencias()); tAud+=aud;
                int sen = num(item.getCantidadSentencias()); tSen+=sen;
                int pro = num(item.getCantidadProcesos()); tPro+=pro;
                int not = num(item.getCantidadNotificaciones()); tNot+=not;
                int ori = num(item.getCantidadOrientaciones()); tOri+=ori;
                int totalFila = dem+aud+sen+pro+not+ori; tGen+=totalFila;

                agregarCeldaDato(table, String.valueOf(dem));
                agregarCeldaDato(table, String.valueOf(aud));
                agregarCeldaDato(table, String.valueOf(sen));
                agregarCeldaDato(table, String.valueOf(pro));
                agregarCeldaDato(table, String.valueOf(not));
                agregarCeldaDato(table, String.valueOf(ori));
                agregarCeldaDatoBold(table, String.valueOf(totalFila));
            }
            agregarCeldaDatoBold(table, "TOTAL");
            agregarCeldaDatoBold(table, String.valueOf(tDem));
            agregarCeldaDatoBold(table, String.valueOf(tAud));
            agregarCeldaDatoBold(table, String.valueOf(tSen));
            agregarCeldaDatoBold(table, String.valueOf(tPro));
            agregarCeldaDatoBold(table, String.valueOf(tNot));
            agregarCeldaDatoBold(table, String.valueOf(tOri));
            agregarCeldaDatoBold(table, String.valueOf(tGen));
        }
        doc.add(table);
    }

    // ============================================
    //            HELPERS VISUALES (Estandarizados)
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

    private void agregarBloqueSimple(Document doc, String contenido) throws DocumentException {
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(val(contenido), FONT_NORMAL_8));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(3f);
        table.addCell(cell); doc.add(table);
    }

    private void agregarBloqueSimpleCentrado(Document doc, String contenido) throws DocumentException {
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(contenido, FONT_NORMAL_8));
        cell.setBorder(Rectangle.BOX); cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3f);
        table.addCell(cell); doc.add(table);
    }

    private void agregarLineaSeparadora(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
        doc.add(Chunk.NEWLINE);
    }

    private void agregarEspacio(Document doc) throws DocumentException {
        doc.add(new Paragraph(" ", FONT_NORMAL_7));
    }

    // Helpers
    private String obtenerNombreCorte(String id) { return repoCorte.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreDepa(String id) { return repoDepa.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreProv(String id) { return repoProv.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreDist(String id) { return repoDist.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreMateria(Integer id) { return repoMateria.findById(id).map(e -> e.getDescripcion()).orElse(String.valueOf(id)); }
    private String obtenerNombreVulnerabilidad(Integer id) { return repoVuln.findById(id).map(e -> e.getDescripcion()).orElse(String.valueOf(id)); }
    private String val(String s) { return s != null ? s : ""; }
    private int num(Integer i) { return i != null ? i : 0; }

    // =================================================================
    //            CLASE INTERNA: CABECERA Y PIE DE PÁGINA FIX
    // =================================================================
    class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                float pageTop = document.getPageSize().getHeight();
                try {
                    // Carga la imagen de recursos
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.jpg"));
                    logo.scaleToFit(500, 50);
                    // Posición Fija
                    logo.setAbsolutePosition(30, pageTop - 70);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}

                // Texto Cabecera
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("FERIA LLAPANCHIKPAQ", FONT_BOLD_10),
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