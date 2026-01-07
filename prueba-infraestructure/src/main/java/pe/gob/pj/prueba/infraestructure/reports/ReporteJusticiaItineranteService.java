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
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovJusticiaItineranteRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteJusticiaItineranteService {

    private final MovJusticiaItineranteRepository repository;

    // ✅ INYECCIÓN DE REPOSITORIOS MAESTROS
    private final MaeDistritoJudicialRepository repoDistritoJud;
    private final MaeEjeRepository repoEje;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;
    private final MaeTamboRepository repoTambo;
    private final MaeMateriaRepository repoMateria;
    private final MaeTipoVulnerabilidadRepository repoVuln;

    // ✅ INYECCIÓN DE URL BASE (Frontend/Backend Gateway)
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
    public byte[] generarFichaItinerante(String id) throws Exception {

        MovJusticiaItineranteEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Evento FJI no encontrado con ID: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // --- CABECERA ---
            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pDistrito = new Paragraph("CORTE SUPERIOR DE JUSTICIA: " + nombreCorte, FONT_TITULO);
            pDistrito.setAlignment(Element.ALIGN_CENTER);
            document.add(pDistrito);

            PdfPTable tableInfo = new PdfPTable(2);
            tableInfo.setWidthPercentage(100);
            tableInfo.setWidths(new float[]{85, 15});
            PdfPCell cellVacia = new PdfPCell(new Phrase("")); cellVacia.setBorder(Rectangle.NO_BORDER);
            PdfPCell cellNum = new PdfPCell(new Phrase(entity.getId(), FONT_BOLD_8));
            cellNum.setHorizontalAlignment(Element.ALIGN_CENTER); cellNum.setBorderWidth(1);
            tableInfo.addCell(cellVacia); tableInfo.addCell(cellNum);
            document.add(tableInfo);
            document.add(Chunk.NEWLINE);

            // --- DATOS GENERALES ---
            agregarFila(document, "Resolución Anual que Aprueba el Plan", ": N° " + val(entity.getResolucionPlanAnual()));
            agregarFila(document, "Resolución Administrativa que Aprueba el Plan", ": N° " + val(entity.getResolucionAdminPlan()));
            agregarFila(document, "Documento que Autoriza Actividad/Evento", ": N° " + val(entity.getDocumentoAutoriza()));
            agregarFila(document, "Fecha de Inicio", ": " + entity.getFechaInicio());
            agregarFila(document, "Fecha de Finalización", ": " + entity.getFechaFin());

            String nombreEje = obtenerNombreEje(entity.getEjeId());
            agregarFila(document, "Eje de Trabajo", ": " + nombreEje);

            agregarFila(document, "Público Objetivo", ": " + val(entity.getPublicoObjetivo()));

            if ("OTRO(ESPECIFICAR)".equalsIgnoreCase(entity.getPublicoObjetivo())) {
                agregarFila(document, "Observación", ": " + val(entity.getPublicoObjetivoDetalle()));
            }

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
            document.add(Chunk.NEWLINE);

            // --- I. LUGAR DE LA ACTIVIDAD ---
            document.add(new Paragraph("I. LUGAR DE LA ACTIVIDAD:", FONT_BOLD_8));

            agregarFila(document, "Anexo/Localidad/Institución", ": " + val(entity.getLugarActividad()));

            agregarFila(document, "Región", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoGeograficoId()));

            agregarFila(document, "N° de Mesa de Partes Instaladas", ": " + num(entity.getNumMujeresIndigenas()));
            agregarFila(document, "N° de Servidores que Brindaron Atención", ": " + num(entity.getNumPersonasNoIdiomaNacional()));
            agregarFila(document, "N° de Jueces que Brindaron Atención", ": " + num(entity.getNumJovenesQuechuaAymara()));

            boolean esPais = "SI".equalsIgnoreCase(entity.getCodigoAdcPueblosIndigenas());
            agregarFila(document, "¿La Actividad en Convenio con PAIS?", ": " + (esPais ? "SI" : "NO"));
            if (esPais) {
                String nombreTambo = obtenerNombreTambo(entity.getTambo());
                agregarFila(document, "Si es sí, Mencione el Tambo", ": " + nombreTambo);
            }

            boolean esLengua = "SI".equalsIgnoreCase(entity.getCodigoSaeLenguaNativa());
            agregarFila(document, "¿Se atendió en Lengua Nativa?", ": " + (esLengua ? "SI" : "NO"));
            if (esLengua) {
                agregarFila(document, "Si es sí, Mencione la Lengua Nativa", ": " + val(entity.getLenguaNativa()));
            }

            document.add(Chunk.NEWLINE);

            // --- II. PERSONAS BENEFICIADAS ---
            document.add(new Paragraph("II. PERSONAS BENEFICIADAS:", FONT_BOLD_8));
            document.add(new Paragraph("(*Número Aproximado de Asistentes a la Campaña/Feria)", FONT_NORMAL_8));
            document.add(Chunk.NEWLINE);

            crearTablaBeneficiadasHorizontal(document, entity.getPersonasBeneficiadas());

            document.add(Chunk.NEWLINE);

            // --- III. PERSONAS ATENDIDAS ---
            document.add(new Paragraph("III. PERSONAS ATENDIDAS:", FONT_BOLD_8));
            document.add(new Paragraph("*(Número de Personas Atendidas por el Poder Judicial)", FONT_NORMAL_8));
            document.add(new Paragraph("a) Por Tipo de Vulnerabilidad, Rango de Edad y Género:", FONT_NORMAL_8));
            document.add(Chunk.NEWLINE);

            crearTablaAtendidasMatriz(document, entity.getPersonasAtendidas());

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("b) Por Casos Atendidos:", FONT_NORMAL_8));
            document.add(Chunk.NEWLINE);

            crearTablaCasos(document, entity.getCasosAtendidos());

            // --- TEXTOS ---
            agregarBloqueTexto(document, "IV. DESCRIPCIÓN DE LA ACTIVIDAD REALIZADA:", entity.getDescripcionActividad());
            agregarBloqueTexto(document, "V. INSTITUCIONES ALIADAS:", entity.getInstitucionesAliadas());
            agregarBloqueTexto(document, "VI. OBSERVACIONES:", entity.getObservaciones());

            // --- VII. ACTIVIDAD OPERATIVA ---
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("VII. ACTIVIDAD OPERATIVA REALIZADA:", FONT_BOLD_8));

            if (entity.getTareasRealizadas() != null && !entity.getTareasRealizadas().isEmpty()) {
                List<MaeTareaEntity> tareas = entity.getTareasRealizadas().stream()
                        .map(MovJiTareasRealizadasEntity::getTareaMaestra)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                List<MaeActividadOperativaEntity> actividades = tareas.stream()
                        .map(t -> t.getIndicador().getActividad()).distinct().collect(Collectors.toList());
                for (MaeActividadOperativaEntity act : actividades)
                    agregarBloqueSimple(document, act.getId() + " " + act.getDescripcion());

                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("a) Indicadores de la Actividad Operativa:", FONT_NORMAL_8));
                List<MaeIndicadorEntity> indicadores = tareas.stream()
                        .map(MaeTareaEntity::getIndicador).distinct().collect(Collectors.toList());
                for (MaeIndicadorEntity ind : indicadores)
                    agregarBloqueSimple(document, "     " + ind.getId() + " " + ind.getDescripcion());

                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("b) Tareas Realizadas de la Actividad Operativa:", FONT_NORMAL_8));
                for (MaeTareaEntity tar : tareas)
                    agregarBloqueSimple(document, "          " + tar.getId() + " " + tar.getDescripcion());
            } else {
                document.add(Chunk.NEWLINE);
                agregarBloqueSimpleCentrado(document, "SIN REGISTROS");
            }

            // --- VIII. ANEXOS ---
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("VIII. ANEXOS:", FONT_BOLD_8));

            // 1. Enlace al PDF (Backend)
            Anchor linkFicha = new Anchor("Ver formato de atención (Haz clic aquí)", FONT_LINK);
            linkFicha.setReference(baseUrl + "/publico/v1/justicia-itinerante/anexo/" + id);
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

            // --- FOOTER / FIRMA ---
            agregarFila(document, "Fecha de registro", ": " + entity.getFechaRegistro());
            agregarFila(document, "Registrado por", ": " + entity.getUsuarioRegistro());

            document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE);
            Paragraph pLinea = new Paragraph("--------------------------------------------------", FONT_NORMAL_8);
            pLinea.setAlignment(Element.ALIGN_CENTER); document.add(pLinea);
            Paragraph pFirma = new Paragraph("JUAN PEREZ\nADMINISTRADOR DE SEDE", FONT_BOLD_8);
            pFirma.setAlignment(Element.ALIGN_CENTER); document.add(pFirma);

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

    private String obtenerNombreTambo(String idOrName) {
        if (idOrName == null) return "";
        if (idOrName.length() <= 6) {
            return repoTambo.findById(idOrName).map(e -> e.getNombre()).orElse(idOrName);
        }
        return idOrName;
    }

    private String obtenerNombreMateria(Integer id) {
        if (id == null) return "";
        return repoMateria.findById(id).map(e -> e.getDescripcion()).orElse(String.valueOf(id));
    }

    private String obtenerNombreVulnerabilidad(Integer id) {
        if (id == null) return "";
        return repoVuln.findById(id).map(e -> e.getDescripcion()).orElse(String.valueOf(id));
    }

    // ============================================
    //            MÉTODOS DE TABLAS
    // ============================================

    private void crearTablaBeneficiadasHorizontal(Document doc, List<MovJiPersonasBeneficiadasEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{11,11,11, 11,11,11, 11,11,11, 12});

        agregarCeldaEncabezado(table, "NIÑOS Y ADOLESCENTES", 3);
        agregarCeldaEncabezado(table, "JÓVENES Y ADULTOS", 3);
        agregarCeldaEncabezado(table, "ADULTOS MAYORES", 3);
        PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL", FONT_BOLD_7));
        cTotal.setRowspan(3); cTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cTotal.setHorizontalAlignment(Element.ALIGN_CENTER); cTotal.setBackgroundColor(COLOR_CABECERA);
        table.addCell(cTotal);

        agregarCeldaEncabezado(table, "0-17 AÑOS", 3);
        agregarCeldaEncabezado(table, "18-59 AÑOS", 3);
        agregarCeldaEncabezado(table, "60+ AÑOS", 3);

        for(int i=0; i<3; i++) { agregarCeldaSub(table, "F"); agregarCeldaSub(table, "M"); agregarCeldaSub(table, "LGTBIQ"); }

        if (lista == null || lista.isEmpty()) {
            PdfPCell cellVacia = new PdfPCell(new Phrase("SIN REGISTROS", FONT_NORMAL_7));
            cellVacia.setColspan(10); cellVacia.setHorizontalAlignment(Element.ALIGN_CENTER); cellVacia.setPadding(5);
            table.addCell(cellVacia);
        } else {
            Map<String, MovJiPersonasBeneficiadasEntity> map = lista.stream().collect(Collectors.toMap(MovJiPersonasBeneficiadasEntity::getCodigoRango, x -> x));
            String[] codigos = {"01", "02", "03"};
            int granTotal = 0;
            for (String cod : codigos) {
                MovJiPersonasBeneficiadasEntity item = map.get(cod);
                int f = num(item != null ? item.getCantFemenino() : 0);
                int m = num(item != null ? item.getCantMasculino() : 0);
                int l = num(item != null ? item.getCantLgtbiq() : 0);
                agregarCeldaDato(table, String.valueOf(f));
                agregarCeldaDato(table, String.valueOf(m));
                agregarCeldaDato(table, String.valueOf(l));
                granTotal += (f + m + l);
            }
            agregarCeldaDatoBold(table, String.valueOf(granTotal));
        }
        doc.add(table);
    }

    private void crearTablaAtendidasMatriz(Document doc, List<MovJiPersonasAtendidasEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(14);
        table.setWidthPercentage(100);
        float[] widths = new float[14];
        widths[0] = 20f; for(int i=1; i<=12; i++) widths[i] = 5f; widths[13] = 8f;
        table.setWidths(widths);

        PdfPCell cTitulo = new PdfPCell(new Phrase("TIPO DE VULNERABILIDAD", FONT_BOLD_7));
        cTitulo.setRowspan(3); cTitulo.setBackgroundColor(COLOR_CABECERA); table.addCell(cTitulo);
        agregarCeldaEncabezado(table, "NIÑOS Y ADOLESCENTES", 3);
        agregarCeldaEncabezado(table, "JÓVENES", 3);
        agregarCeldaEncabezado(table, "ADULTOS", 3);
        agregarCeldaEncabezado(table, "ADULTOS MAYORES", 3);
        PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL", FONT_BOLD_7));
        cTotal.setRowspan(3); cTotal.setBackgroundColor(COLOR_CABECERA); table.addCell(cTotal);

        agregarCeldaEncabezado(table, "0-17 AÑOS", 3);
        agregarCeldaEncabezado(table, "18-29 AÑOS", 3);
        agregarCeldaEncabezado(table, "30-59 AÑOS", 3);
        agregarCeldaEncabezado(table, "60+ AÑOS", 3);

        for(int i=0; i<4; i++) { agregarCeldaSub(table, "F"); agregarCeldaSub(table, "M"); agregarCeldaSub(table, "L"); }

        if (lista == null || lista.isEmpty()) {
            PdfPCell cellVacia = new PdfPCell(new Phrase("SIN REGISTROS", FONT_NORMAL_7));
            cellVacia.setColspan(14); cellVacia.setHorizontalAlignment(Element.ALIGN_CENTER); cellVacia.setPadding(5);
            table.addCell(cellVacia);
        } else {
            Map<Integer, List<MovJiPersonasAtendidasEntity>> agrupado = lista.stream().collect(Collectors.groupingBy(MovJiPersonasAtendidasEntity::getTipoVulnerabilidadId));
            for (Map.Entry<Integer, List<MovJiPersonasAtendidasEntity>> entry : agrupado.entrySet()) {
                String nombreVuln = obtenerNombreVulnerabilidad(entry.getKey());
                agregarCeldaDato(table, nombreVuln);

                Map<String, MovJiPersonasAtendidasEntity> map = entry.getValue().stream().collect(Collectors.toMap(MovJiPersonasAtendidasEntity::getRangoEdad, x->x));
                String[] rangos = {"01", "02", "03", "04"};
                int totalFila = 0;
                for (String r : rangos) {
                    MovJiPersonasAtendidasEntity e = map.get(r);
                    int f = num(e!=null ? e.getCantidadFemenino() : 0);
                    int m = num(e!=null ? e.getCantidadMasculino() : 0);
                    int l = num(e!=null ? e.getCantidadLgtbiq() : 0);
                    agregarCeldaDato(table, String.valueOf(f));
                    agregarCeldaDato(table, String.valueOf(m));
                    agregarCeldaDato(table, String.valueOf(l));
                    totalFila += (f+m+l);
                }
                agregarCeldaDatoBold(table, String.valueOf(totalFila));
            }
        }
        doc.add(table);
    }

    private void crearTablaCasos(Document doc, List<MovJiCasosAtendidosEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        agregarCeldaEncabezado(table, "MATERIA", 1);
        agregarCeldaSub(table, "Dem."); agregarCeldaSub(table, "Aud.");
        agregarCeldaSub(table, "Sent."); agregarCeldaSub(table, "Proc.");
        agregarCeldaSub(table, "Notif."); agregarCeldaSub(table, "Orien.");
        agregarCeldaEncabezado(table, "TOTAL", 1);

        if (lista == null || lista.isEmpty()) {
            PdfPCell cellVacia = new PdfPCell(new Phrase("SIN REGISTROS", FONT_NORMAL_7));
            cellVacia.setColspan(8); cellVacia.setHorizontalAlignment(Element.ALIGN_CENTER); cellVacia.setPadding(5);
            table.addCell(cellVacia);
        } else {
            for (MovJiCasosAtendidosEntity item : lista) {
                String nombreMateria = obtenerNombreMateria(item.getMateriaId());
                agregarCeldaDato(table, nombreMateria);

                int total = num(item.getCantidadDemandas()) + num(item.getCantidadAudiencias()) + num(item.getCantidadSentencias()) + num(item.getCantidadProcesos()) + num(item.getCantidadNotificaciones()) + num(item.getCantidadOrientaciones());
                agregarCeldaDato(table, String.valueOf(item.getCantidadDemandas()));
                agregarCeldaDato(table, String.valueOf(item.getCantidadAudiencias()));
                agregarCeldaDato(table, String.valueOf(item.getCantidadSentencias()));
                agregarCeldaDato(table, String.valueOf(item.getCantidadProcesos()));
                agregarCeldaDato(table, String.valueOf(item.getCantidadNotificaciones()));
                agregarCeldaDato(table, String.valueOf(item.getCantidadOrientaciones()));
                agregarCeldaDatoBold(table, String.valueOf(total));
            }
        }
        doc.add(table);
    }

    // --- Helpers (Igual que antes) ---
    private void agregarBloqueSimple(Document doc, String contenido) throws DocumentException {
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(val(contenido), FONT_NORMAL_8));
        cell.setBorder(Rectangle.BOX); table.addCell(cell); doc.add(table);
    }
    private void agregarBloqueSimpleCentrado(Document doc, String contenido) throws DocumentException {
        PdfPTable table = new PdfPTable(1); table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(contenido, FONT_NORMAL_8));
        cell.setBorder(Rectangle.BOX); cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell); doc.add(table);
    }
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
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        String texto = (contenido == null || contenido.trim().isEmpty()) ? "SIN REGISTROS" : contenido;
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_NORMAL_8));
        cell.setPadding(6);
        if(texto.equals("SIN REGISTROS")) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        doc.add(table);
    }
    private void agregarCeldaEncabezado(PdfPTable table, String texto, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_BOLD_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setColspan(colspan);
        cell.setBackgroundColor(COLOR_CABECERA);
        table.addCell(cell);
    }
    private void agregarCeldaSub(PdfPTable table, String texto) {
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
    private String val(String s) { return s != null ? s : ""; }
    private int num(Integer i) { return i != null ? i : 0; }

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
                        new Phrase("JUSTICIA ITINERANTE", FONT_BOLD_10),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.top() - 60, 0);
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                        new Phrase("(INFORME)", FONT_BOLD_10),
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