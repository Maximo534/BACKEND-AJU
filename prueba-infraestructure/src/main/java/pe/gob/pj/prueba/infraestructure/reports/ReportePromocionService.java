package pe.gob.pj.prueba.infraestructure.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromocionCulturaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromCulturaDetalleEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromCulturaTareaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeActividadOperativaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeIndicadorEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeTareaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.MovPromocionCulturaRepository;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service("reportePromocionService")
@RequiredArgsConstructor
public class ReportePromocionService {

    private final MovPromocionCulturaRepository repository;

    // ✅ INYECCIÓN DE REPOSITORIOS PARA OBTENER NOMBRES
    private final MaeDistritoJudicialRepository repoDistritoJud;
    private final MaeEjeRepository repoEje;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String baseUrl;

    // --- FUENTES ---
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

        MovPromocionCulturaEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Evento APCJ no encontrado: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent());

            document.open();

            // --- TÍTULO ---
            Paragraph pTitulo = new Paragraph("ACCIONES DE PROMOCIÓN DE CULTURA JURÍDICA", FONT_TITULO);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitulo);

            // ✅ CORREGIDO: Nombre de Corte
            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pCorte = new Paragraph("DISTRITO JUDICIAL: " + nombreCorte, FONT_BOLD_10);
            pCorte.setAlignment(Element.ALIGN_CENTER);
            document.add(pCorte);

            // --- ID ---
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

            // --- DATOS GENERALES ---
            agregarFila(document, "Resolución Anual Plan", ": " + val(entity.getResolucionPlanAnual()));
            agregarFila(document, "Resolución Admin Plan", ": " + val(entity.getResolucionAdminPlan()));
            agregarFila(document, "Doc. Autoriza Evento", ": " + val(entity.getDocumentoAutoriza()));

            agregarFila(document, "Nombre Autoridad", ": " + val(entity.getNombreActividad()));
            agregarFila(document, "Tipo Doc. Autoridad", ": " + val(entity.getTipoActividad()));
            agregarFila(document, "Dato Autoridad", ": " + val(entity.getTipoActividadOtros()));

            agregarFila(document, "Fecha Inicio", ": " + entity.getFechaInicio());
            agregarFila(document, "Fecha Fin", ": " + entity.getFechaFin());

            // ✅ CORREGIDO: Nombre de Eje
            String nombreEje = obtenerNombreEje(entity.getEjeId());
            agregarFila(document, "Eje Temático", ": " + nombreEje);

            agregarFila(document, "Zona Intervención", ": " + val(entity.getZonaIntervencion()));
            agregarFila(document, "Modalidad", ": " + val(entity.getModalidadProyecto()));

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
            document.add(Chunk.NEWLINE);

            // --- I. DATOS DE LA ACTIVIDAD ---
            document.add(new Paragraph("I. DATOS DE LA ACTIVIDAD:", FONT_BOLD_8));

            agregarFila(document, "Lugar / Actividad", ": " + val(entity.getLugarActividad()));

            // ✅ CORREGIDO: Ubigeo con Nombres
            agregarFila(document, "Región", ": " + obtenerNombreDepa(entity.getDepartamentoId()));
            agregarFila(document, "Provincia", ": " + obtenerNombreProv(entity.getProvinciaId()));
            agregarFila(document, "Distrito", ": " + obtenerNombreDist(entity.getDistritoGeograficoId()));

            agregarFila(document, "Posición Solicitante", ": " + val(entity.getPublicoObjetivo()));
            agregarFila(document, "Desc. Posición Origen", ": " + val(entity.getPublicoObjetivoOtros()));

            agregarFila(document, "Sub-Área Intervención", ": " + val(entity.getSeDictoLenguaNativa()));
            agregarFila(document, "Lengua Nativa", ": " + val(entity.getLenguaNativaDesc()));

            agregarFila(document, "Cod. Prog. Presupuestal", ": " + val(entity.getParticiparonDiscapacitados()));
            String codProy = (entity.getNumeroDiscapacitados() != null) ? String.valueOf(entity.getNumeroDiscapacitados()) : "";
            agregarFila(document, "Cod. Prog. Proyecto", ": " + codProy);

            // El campo areaRiesgo en BD parece guardar el flag de Intérprete, según tu análisis PHP
            agregarFila(document, "¿Intérprete de Señas?", ": " + val(entity.getAreaRiesgo()));

            document.add(Chunk.NEWLINE);

            // --- II. PERSONAS BENEFICIADAS ---
            document.add(new Paragraph("II. PERSONAS BENEFICIADAS:", FONT_BOLD_8));
            document.add(Chunk.NEWLINE);

            crearTablaBeneficiarios(document, entity.getParticipantes());

            // --- III. DESCRIPCIÓN Y OBSERVACIONES ---
            agregarBloqueTexto(document, "III. DESCRIPCIÓN DE LA ACTIVIDAD:", entity.getDescripcionActividad());
            agregarBloqueTexto(document, "IV. RECURSOS UTILIZADOS:", entity.getInstitucionesAliadas());
            agregarBloqueTexto(document, "V. OBSERVACIONES:", entity.getObservacion());

            // --- VII. ACTIVIDAD OPERATIVA REALIZADA ---
            if (entity.getTareas() != null && !entity.getTareas().isEmpty()) {
                List<MaeTareaEntity> tareasRealizadas = entity.getTareas().stream()
                        .map(MovPromCulturaTareaEntity::getTareaMaestra)
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
                document.add(new Paragraph("VII. ACTIVIDAD OPERATIVA REALIZADA:", FONT_BOLD_8));
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

            // --- VIII. ANEXOS (CON SALTO DE LÍNEA) ---
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("VIII. ANEXOS:", FONT_BOLD_8));

            // 1. Enlace al PDF (Directo al Backend)
            Anchor linkFicha = new Anchor("Ver formato de atención (Haz clic aquí)", FONT_LINK);
            linkFicha.setReference(baseUrl + "/publico/v1/promocion-cultura/anexo/" + id);
            document.add(new Paragraph(linkFicha)); // ✅ Envuelto en Paragraph para salto de línea

            // 2. Enlace a Videos (Apunta al Frontend)
            Anchor linkVideos = new Anchor("Ver videos (Haz clic aquí)", FONT_LINK);
            // Cuando tengas frontend, será algo como: http://mi-web.com/visor-videos/{id}
            linkVideos.setReference(baseUrl + "/visor/videos/" + id);
            document.add(new Paragraph(linkVideos)); // ✅ Envuelto en Paragraph

            // 3. Enlace a Fotos (Apunta al Frontend)
            Anchor linkFotos = new Anchor("Ver fotografías (Haz clic aquí)", FONT_LINK);
            linkFotos.setReference(baseUrl + "/visor/fotos/" + id);
            document.add(new Paragraph(linkFotos)); // ✅ Envuelto en Paragraph

            // --- FOOTER REGISTRO ---
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));
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

    // --- MÉTODOS AUXILIARES TABLAS ---

    private void crearTablaBeneficiarios(Document doc, List<MovPromCulturaDetalleEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 10, 20, 20, 20});

        agregarCeldaEncabezado(table, "RANGO DESCRIPCIÓN");
        agregarCeldaEncabezado(table, "COD");
        agregarCeldaEncabezado(table, "FEMENINO");
        agregarCeldaEncabezado(table, "MASCULINO");
        agregarCeldaEncabezado(table, "LGTBIQ");

        int tF = 0, tM = 0, tL = 0;

        if (lista != null && !lista.isEmpty()) {
            for (MovPromCulturaDetalleEntity item : lista) {
                agregarCeldaDato(table, val(item.getDescripcionRango()));
                agregarCeldaDato(table, val(item.getCodigoRango()));
                agregarCeldaDato(table, String.valueOf(item.getCantidadFemenino()));
                agregarCeldaDato(table, String.valueOf(item.getCantidadMasculino()));
                agregarCeldaDato(table, String.valueOf(item.getCantidadLgtbiq()));

                tF += (item.getCantidadFemenino() == null ? 0 : item.getCantidadFemenino());
                tM += (item.getCantidadMasculino() == null ? 0 : item.getCantidadMasculino());
                tL += (item.getCantidadLgtbiq() == null ? 0 : item.getCantidadLgtbiq());
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
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(contenido, FONT_NORMAL_8));
        cell.setBorder(Rectangle.BOX);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        doc.add(table);
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
        PdfPCell cell = new PdfPCell(new Phrase("")); cell.setBorder(Rectangle.NO_BORDER); return cell;
    }
    private String val(String s) { return s != null ? s : ""; }

    class HeaderFooterPageEvent extends PdfPageEventHelper {
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                try {
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.JPG"));
                    logo.scaleToFit(500, 50); logo.setAbsolutePosition(30, document.getPageSize().getHeight() - 70);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {}
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("(INFORME)", FONT_BOLD_10),
                        (document.right() - document.left()) / 2 + document.leftMargin(), document.top() - 72, 0);
            } catch (Exception e) {}
        }
        public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Página " + writer.getPageNumber(), FONT_NORMAL_8),
                    (document.right() - document.left()) / 2 + document.leftMargin(), document.bottom() - 20, 0);
        }
    }
}