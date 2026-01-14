package pe.gob.pj.prueba.infraestructure.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.*;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovLlapanchikpaqJusticiaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
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

    @Value("${app.frontend.url:http://localhost:4200}")
    private String baseUrl;

    // Fuentes (Estilo PHP)
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_BOLD_7 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_NORMAL_7 = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, new Color(5, 64, 209));
    private static final Color COLOR_CABECERA = Color.WHITE; // El PHP no usa fondo gris, usa blanco simple o predeterminado.

    @Transactional(readOnly = true)
    public byte[] generarFichaPdf(String id) throws Exception {

        MovLlapanchikpaqJusticia entity = repository.findById(id)
                .orElseThrow(() -> new Exception("No existe registro LLJ con ID: " + id));

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

            // --- NÚMERO (Alineado Derecha) ---
            PdfPTable tableNum = new PdfPTable(2);
            tableNum.setWidthPercentage(100);
            tableNum.setWidths(new float[]{85, 15});

            PdfPCell cVacia = new PdfPCell(new Phrase(""));
            cVacia.setBorder(Rectangle.NO_BORDER);

            // Recuadro ID
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

            // --- DATOS GENERALES ---
            agregarFila(document, "Resolución Anual que Aprueba el Plan", ": N° " + val(entity.getResolucionPlanAnual()));
            agregarFila(document, "Resolución Administrativa que Aprueba el Plan", ": N° " + val(entity.getResolucionAdminPlan()));
            agregarFila(document, "Documento que Autoriza Actividad/Evento", ": N° " + val(entity.getDocumentoAutoriza()));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            agregarFila(document, "Fecha de Inicio", ": " + (entity.getFechaInicio() != null ? entity.getFechaInicio().format(fmt) : ""));

            agregarSeparador(document);

            // --- I. LUGAR ---
            document.add(new Paragraph("I. LUGAR DE LA ACTIVIDAD:", FONT_BOLD_8));
            agregarFila(document, "Anexo/Localidad/Institución", ": " + val(entity.getLugarActividad()));
            agregarFila(document, "Región", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoGeograficoId()));

            // Mapeo de campos numéricos según reporte PHP
            // nmpi -> numMujeresIndigenas
            agregarFila(document, "N° de Mesa de Partes Instaladas", ": " + num(entity.getNumMujeresIndigenas()));
            // nsqba -> numPersonasQuechuaAymara
            agregarFila(document, "N° de Servidores que Brindaron Atención", ": " + num(entity.getNumPersonasQuechuaAymara()));
            // njqba -> numJuecesQuechuaAymara
            agregarFila(document, "N° de Jueces que Brindaron Atención", ": " + num(entity.getNumJuecesQuechuaAymara()));

            agregarFila(document, "¿Se atendió en Lengua Nativa?", ": " + val(entity.getUsoLenguaNativa()));
            if ("SI".equalsIgnoreCase(entity.getUsoLenguaNativa())) {
                agregarFila(document, "Si es sí, Mencione la Lengua Nativa", ": " + val(entity.getLenguaNativaDesc()));
            }

            // --- II. PERSONAS BENEFICIADAS ---
            document.add(new Paragraph("II. PERSONAS BENEFICIADAS:", FONT_BOLD_8));
            document.add(new Paragraph("(*Número Aproximado de Asistentes a la Campaña/Feria)", FONT_NORMAL_8));
            document.add(Chunk.NEWLINE);
            crearTablaBeneficiadas(document, entity.getBeneficiadas());

            // --- III. PERSONAS ATENDIDAS ---
            document.add(new Paragraph("III. PERSONAS ATENDIDAS: ", FONT_BOLD_8));
            document.add(new Paragraph("*(Número de Personas Atendidas por el Poder Judicial, los Datos deben Concordar con el Formato de Atención FÍsico)", FONT_NORMAL_8));
            document.add(new Paragraph("a) Por Tipo de Vulnerabilidad, Rango de Edad y Género: ", FONT_NORMAL_8));
            document.add(Chunk.NEWLINE);
            crearMatrizAtendidas(document, entity.getAtendidas());
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("b) Por Casos Atendidos: ", FONT_NORMAL_8));
            document.add(Chunk.NEWLINE);
            crearTablaCasos(document, entity.getCasos());
            document.add(Chunk.NEWLINE);

            // --- TEXTOS ---
            document.add(new Paragraph("IV. DESCRIPCIÓN DE LA ACTIVIDAD REALIZADA (DETALLAR OBJETIVO Y FINALIDAD): ", FONT_BOLD_8));
            agregarBloqueTexto(document, entity.getDerivacion()); // Mapeado a descripción en BD? Revisar si es derivacion o el otro campo

            document.add(new Paragraph("V. INSTITUCIONES ALIADAS: ", FONT_BOLD_8));
            agregarBloqueTexto(document, entity.getImpactoActividad()); // Asumiendo mapeo (ia -> impacto)

            document.add(new Paragraph("VI. OBSERVACIONES: ", FONT_BOLD_8));
            agregarBloqueTexto(document, entity.getObservacion());

            // --- VII. ACTIVIDAD OPERATIVA ---
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("VII. ACTIVIDAD OPERATIVA REALIZADA: ", FONT_BOLD_8));

            if (entity.getTareas() != null && !entity.getTareas().isEmpty()) {
                List<MaeTareaEntity> tareas = entity.getTareas().stream()
                        .map(MovLljTareaRealizadasEntity::getTareaMaestra).filter(Objects::nonNull).collect(Collectors.toList());

                // Actividades
                tareas.stream().map(t -> t.getIndicador().getActividad()).distinct()
                        .forEach(a -> agregarItemLista(document, a.getId() + " " + a.getDescripcion()));

                document.add(new Paragraph("a) Indicadores de la Actividad Operativa: ", FONT_NORMAL_8));
                tareas.stream().map(MaeTareaEntity::getIndicador).distinct()
                        .forEach(i -> agregarItemLista(document, "     " + i.getId() + " " + i.getDescripcion()));

                document.add(new Paragraph("b) Tareas Realizadas de la Actividad Operativa: ", FONT_NORMAL_8));
                tareas.forEach(t -> agregarItemLista(document, "          " + t.getId() + " " + t.getDescripcion()));
            } else {
                agregarBloqueTexto(document, "SIN REGISTROS");
            }

            document.add(Chunk.NEWLINE);

            // --- VIII. ANEXOS ---
            document.add(new Paragraph("VIII. ANEXOS: ", FONT_BOLD_8));

            agregarLink(document, "Ver formato de atención (Haz clic aquí)", baseUrl + "/publico/v1/llapanchikpaq/anexo/" + id);
            agregarLink(document, "Ver videos (Haz clic aquí)", baseUrl + "/visor/llj/videos/" + id);
            agregarLink(document, "Ver fotografías (Haz clic aquí)", baseUrl + "/visor/llj/fotos/" + id);

            agregarSeparador(document);

            // --- PIE ---
            agregarFila(document, "Fecha de registro", ": " + (entity.getFechaRegistro() != null ? entity.getFechaRegistro() : ""));
            agregarFila(document, "Registrado por", ": " + val(entity.getUsuarioRegistro()));

            document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE);
            Paragraph pLinea = new Paragraph("------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8);
            pLinea.setAlignment(Element.ALIGN_CENTER);
            document.add(pLinea);

            // Nombre y Cargo (Mock - idealmente sacar de tabla de usuarios)
            Paragraph pFirma = new Paragraph(val(entity.getUsuarioRegistro()), FONT_BOLD_8);
            pFirma.setAlignment(Element.ALIGN_CENTER);
            document.add(pFirma);

            document.close();
            return out.toByteArray();
        }
    }

    // ==========================================
    //           LÓGICA TABLAS
    // ==========================================

    private void crearTablaBeneficiadas(Document doc, List<MovLljPersonasBeneficiadasEntity> lista) throws DocumentException {
        // Estructura: 3 Grupos (Niños, Jovenes, Adultos) + Total
        // Columnas: [F,M,L] x 3 + Total = 10 columnas
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{11,11,11, 11,11,11, 11,11,11, 12});

        // Cabecera 1
        agregarCeldaHeader(table, "NIÑOS Y ADOLESCENTES", 3);
        agregarCeldaHeader(table, "JÓVENES Y ADULTOS", 3);
        agregarCeldaHeader(table, "ADULTOS MAYORES", 3);
        PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL", FONT_BOLD_7));
        cTotal.setRowspan(3); cTotal.setVerticalAlignment(Element.ALIGN_MIDDLE); cTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cTotal);

        // Cabecera 2 (Rangos)
        agregarCeldaHeader(table, "0-17 AÑOS", 3);
        agregarCeldaHeader(table, "18-59 AÑOS", 3);
        agregarCeldaHeader(table, "60+ AÑOS", 3);

        // Cabecera 3 (Sexos)
        for(int i=0; i<3; i++) {
            agregarCeldaSubHeader(table, "F");
            agregarCeldaSubHeader(table, "M");
            agregarCeldaSubHeader(table, "LGTBIQ");
        }

        // Datos
        if (lista == null || lista.isEmpty()) {
            PdfPCell c = new PdfPCell(new Phrase("SIN REGISTROS", FONT_NORMAL_7));
            c.setColspan(10); c.setHorizontalAlignment(Element.ALIGN_CENTER); table.addCell(c);
        } else {
            // Mapear por codigoRango (01, 02, 03)
            Map<String, MovLljPersonasBeneficiadasEntity> map = lista.stream()
                    .collect(Collectors.toMap(e -> e.getCodigoRango().trim(), x -> x));

            String[] codigos = {"01", "02", "03"}; // Ajustar según BD real
            int granTotal = 0;

            for (String cod : codigos) {
                MovLljPersonasBeneficiadasEntity item = map.get(cod);
                int f = item != null ? num(item.getCantFemenino()) : 0;
                int m = item != null ? num(item.getCantMasculino()) : 0;
                int l = item != null ? num(item.getCantLgtbiq()) : 0;

                agregarCeldaDato(table, String.valueOf(f));
                agregarCeldaDato(table, String.valueOf(m));
                agregarCeldaDato(table, String.valueOf(l));
                granTotal += (f+m+l);
            }
            agregarCeldaDatoBold(table, String.valueOf(granTotal));
        }
        doc.add(table);
    }

    private void crearMatrizAtendidas(Document doc, List<MovLljPersonasAtendidasEntity> lista) throws DocumentException {
        // Matriz: Vulnerabilidad (Col 1) + 4 Rangos * 3 Sexos (12 cols) + Total (1 col) = 14 cols
        PdfPTable table = new PdfPTable(14);
        table.setWidthPercentage(100);
        float[] w = new float[14];
        w[0]=25f; // Nombre Vuln
        for(int i=1;i<=12;i++) w[i]=6f;
        w[13]=9f;
        table.setWidths(w);

        // Header 1
        PdfPCell cV = new PdfPCell(new Phrase("TIPO DE VULNERABILIDAD", FONT_BOLD_7));
        cV.setRowspan(3); cV.setVerticalAlignment(Element.ALIGN_MIDDLE); cV.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cV);

        agregarCeldaHeader(table, "NIÑOS Y ADOLESCENTES", 3);
        agregarCeldaHeader(table, "JÓVENES", 3);
        agregarCeldaHeader(table, "ADULTOS", 3);
        agregarCeldaHeader(table, "ADULTOS MAYORES", 3);

        PdfPCell cTot = new PdfPCell(new Phrase("TOTAL", FONT_BOLD_7));
        cTot.setRowspan(3); cTot.setVerticalAlignment(Element.ALIGN_MIDDLE); cTot.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cTot);

        // Header 2
        agregarCeldaHeader(table, "0-17 AÑOS", 3);
        agregarCeldaHeader(table, "18-29 AÑOS", 3);
        agregarCeldaHeader(table, "30-59 AÑOS", 3);
        agregarCeldaHeader(table, "60+ AÑOS", 3);

        // Header 3
        for(int i=0; i<4; i++) {
            agregarCeldaSubHeader(table, "F");
            agregarCeldaSubHeader(table, "M");
            agregarCeldaSubHeader(table, "L");
        }

        if (lista == null || lista.isEmpty()) {
            PdfPCell c = new PdfPCell(new Phrase("SIN REGISTROS", FONT_NORMAL_7));
            c.setColspan(14); c.setHorizontalAlignment(Element.ALIGN_CENTER); table.addCell(c);
        } else {
            // Agrupar por Vulnerabilidad
            Map<Integer, List<MovLljPersonasAtendidasEntity>> groups = lista.stream()
                    .collect(Collectors.groupingBy(MovLljPersonasAtendidasEntity::getTipoVulnerabilidadId));

            for (Map.Entry<Integer, List<MovLljPersonasAtendidasEntity>> entry : groups.entrySet()) {
                String nombreVuln = obtenerNombreVulnerabilidad(entry.getKey());
                agregarCeldaDato(table, nombreVuln, Element.ALIGN_LEFT);

                Map<String, MovLljPersonasAtendidasEntity> byRango = entry.getValue().stream()
                        .collect(Collectors.toMap(e -> e.getRangoEdad().trim(), x->x));

                String[] rangos = {"01", "02", "03", "04"}; // Ajustar según códigos BD
                int filaTotal = 0;

                for(String r : rangos) {
                    MovLljPersonasAtendidasEntity d = byRango.get(r);
                    int f = d!=null?num(d.getCantidadFemenino()):0;
                    int m = d!=null?num(d.getCantidadMasculino()):0;
                    int l = d!=null?num(d.getCantidadLgtbiq()):0;

                    agregarCeldaDato(table, String.valueOf(f));
                    agregarCeldaDato(table, String.valueOf(m));
                    agregarCeldaDato(table, String.valueOf(l));
                    filaTotal += (f+m+l);
                }
                agregarCeldaDatoBold(table, String.valueOf(filaTotal));
            }
        }
        doc.add(table);
    }

    private void crearTablaCasos(Document doc, List<MovLljCasosAtendidosEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);

        agregarCeldaHeader(table, "MATERIA", 1);
        agregarCeldaHeader(table, "TIPO DE SERVICIO", 6);
        agregarCeldaHeader(table, "TOTAL", 1);

        agregarCeldaSubHeader(table, ""); // Debajo de Materia
        agregarCeldaSubHeader(table, "Dem.");
        agregarCeldaSubHeader(table, "Aud.");
        agregarCeldaSubHeader(table, "Sent.");
        agregarCeldaSubHeader(table, "Proc.");
        agregarCeldaSubHeader(table, "Not.");
        agregarCeldaSubHeader(table, "Orie.");
        agregarCeldaSubHeader(table, ""); // Debajo de Total

        if (lista == null || lista.isEmpty()) {
            PdfPCell c = new PdfPCell(new Phrase("SIN REGISTROS", FONT_NORMAL_7));
            c.setColspan(8); c.setHorizontalAlignment(Element.ALIGN_CENTER); table.addCell(c);
        } else {
            int gt=0;
            for(MovLljCasosAtendidosEntity c : lista) {
                String mat = obtenerNombreMateria(c.getMateriaId());
                agregarCeldaDato(table, mat, Element.ALIGN_LEFT);

                int dem = num(c.getCantidadDemandas());
                int aud = num(c.getCantidadAudiencias());
                int sen = num(c.getCantidadSentencias());
                int pro = num(c.getCantidadProcesos());
                int not = num(c.getCantidadNotificaciones());
                int ori = num(c.getCantidadOrientaciones());
                int sub = dem+aud+sen+pro+not+ori; gt+=sub;

                agregarCeldaDato(table, String.valueOf(dem));
                agregarCeldaDato(table, String.valueOf(aud));
                agregarCeldaDato(table, String.valueOf(sen));
                agregarCeldaDato(table, String.valueOf(pro));
                agregarCeldaDato(table, String.valueOf(not));
                agregarCeldaDato(table, String.valueOf(ori));
                agregarCeldaDatoBold(table, String.valueOf(sub));
            }
            // Fila Total final (opcional, el PHP suma columna por columna pero esto basta)
        }
        doc.add(table);
    }

    // ==========================================
    //              HELPERS
    // ==========================================

    private void agregarFila(Document doc, String label, String value) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 65});
        PdfPCell c1 = new PdfPCell(new Phrase(label, FONT_NORMAL_8)); c1.setBorder(Rectangle.NO_BORDER);
        PdfPCell c2 = new PdfPCell(new Phrase(value, FONT_NORMAL_8)); c2.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1); table.addCell(c2);
        doc.add(table);
    }

    private void agregarCeldaHeader(PdfPTable t, String txt, int colspan) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_BOLD_7));
        if (colspan > 1) c.setColspan(colspan);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(c);
    }
    private void agregarCeldaSubHeader(PdfPTable t, String txt) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_BOLD_7));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(c);
    }
    private void agregarCeldaDato(PdfPTable t, String txt) {
        agregarCeldaDato(t, txt, Element.ALIGN_CENTER);
    }
    private void agregarCeldaDato(PdfPTable t, String txt, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_NORMAL_7));
        c.setHorizontalAlignment(align);
        t.addCell(c);
    }
    private void agregarCeldaDatoBold(PdfPTable t, String txt) {
        PdfPCell c = new PdfPCell(new Phrase(txt, FONT_BOLD_7));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(c);
    }

    private void agregarBloqueTexto(Document doc, String texto) throws DocumentException {
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
        PdfPCell c = new PdfPCell(new Phrase((texto!=null && !texto.isEmpty())?texto:" ", FONT_NORMAL_8));
        c.setBorder(Rectangle.BOX); c.setPadding(5);
        table.addCell(c); doc.add(table);
    }

    private void agregarItemLista(Document doc, String texto) {
        try {
            PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
            PdfPCell c = new PdfPCell(new Phrase(texto, FONT_NORMAL_8));
            c.setBorder(Rectangle.BOX); table.addCell(c); doc.add(table);
        } catch(Exception e){}
    }

    private void agregarLink(Document doc, String text, String url) throws DocumentException {
        Anchor anchor = new Anchor(text, FONT_LINK);
        anchor.setReference(url);
        doc.add(new Paragraph(anchor));
    }

    private void agregarSeparador(Document doc) throws DocumentException {
        Paragraph p = new Paragraph("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8);
        p.setSpacingBefore(2f); p.setSpacingAfter(2f);
        doc.add(p);
    }

    private String val(String s) { return s != null ? s : ""; }
    private int num(Integer i) { return i != null ? i : 0; }

    // Helpers Maestros
    private String obtenerNombreCorte(String id) { return repoCorte.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreDepa(String id) { return repoDepa.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreProv(String id) { return repoProv.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreDist(String id) { return repoDist.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreMateria(Integer id) { return repoMateria.findById(id).map(e -> e.getDescripcion()).orElse(String.valueOf(id)); }
    private String obtenerNombreVulnerabilidad(Integer id) { return repoVuln.findById(id).map(e -> e.getDescripcion()).orElse(String.valueOf(id)); }

    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                try {
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.JPG"));
                    logo.scaleToFit(300, 50);
                    logo.setAbsolutePosition(25, document.getPageSize().getHeight() - 50);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}

                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("FERIA LLAPANCHIKPAQ", FONT_BOLD_8),
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