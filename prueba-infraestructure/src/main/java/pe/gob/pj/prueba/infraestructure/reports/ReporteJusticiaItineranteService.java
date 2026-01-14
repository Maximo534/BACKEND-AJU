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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteJusticiaItineranteService {

    private final MovJusticiaItineranteRepository repository;

    // --- REPOSITORIOS MAESTROS PARA OBTENER DESCRIPCIONES ---
    private final MaeDistritoJudicialRepository repoDistritoJud;
    private final MaeEjeRepository repoEje;
    private final MaeDepartamentoRepository repoDepa;
    private final MaeProvinciaRepository repoProv;
    private final MaeDistritoRepository repoDist;
    private final MaeTamboRepository repoTambo;
    private final MaeMateriaRepository repoMateria;
    private final MaeTipoVulnerabilidadRepository repoVuln;

    @Value("${app.frontend.url:http://localhost:4200}") // Ajusta a tu URL real
    private String baseUrl;

    // --- FUENTES ESTILO PHP/FPDF ---
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_BOLD_10 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD_8 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_BOLD_7 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font FONT_NORMAL_8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_NORMAL_7 = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font FONT_LINK = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.UNDERLINE, Color.BLUE);
    private static final Color COLOR_CABECERA = new Color(240, 240, 240); // Gris claro similar al reporte

    @Transactional(readOnly = true)
    public byte[] generarFichaItinerante(String id) throws Exception {

        MovJusticiaItineranteEntity entity = repository.findById(id)
                .orElseThrow(() -> new Exception("Evento FJI no encontrado con ID: " + id));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new HeaderFooterPageEvent()); // Cabecera y Pie de página

            document.open();

            // --- TÍTULO CORTE ---
            String nombreCorte = obtenerNombreCorte(entity.getDistritoJudicialId());
            Paragraph pDistrito = new Paragraph("CORTE SUPERIOR DE JUSTICIA: " + nombreCorte, FONT_TITULO);
            pDistrito.setAlignment(Element.ALIGN_CENTER);
            document.add(pDistrito);

            // NÚMERO DE FICHA (Alineado a la derecha como en PHP)
            PdfPTable tableNum = new PdfPTable(2);
            tableNum.setWidthPercentage(100);
            tableNum.setWidths(new float[]{85, 15});
            PdfPCell cVacia = new PdfPCell(new Phrase("")); cVacia.setBorder(Rectangle.NO_BORDER);
            PdfPCell cNum = new PdfPCell(new Phrase("N°: " + entity.getId(), FONT_BOLD_8));
            cNum.setHorizontalAlignment(Element.ALIGN_CENTER);
            tableNum.addCell(cVacia); tableNum.addCell(cNum);
            document.add(tableNum);
            document.add(Chunk.NEWLINE);

            // --- DATOS GENERALES ---
            agregarFila(document, "Resolución Anual que Aprueba el Plan", ": N° " + val(entity.getResolucionPlanAnual()));
            agregarFila(document, "Resolución Administrativa que Aprueba el Plan", ": N° " + val(entity.getResolucionAdminPlan()));
            agregarFila(document, "Documento que Autoriza Actividad/Evento", ": N° " + val(entity.getDocumentoAutoriza()));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fInicio = entity.getFechaInicio() != null ? entity.getFechaInicio().format(fmt) : "";
            String fFin = entity.getFechaFin() != null ? entity.getFechaFin().format(fmt) : "";

            agregarFila(document, "Fecha de Inicio", ": " + fInicio);
            agregarFila(document, "Fecha de Finalización", ": " + fFin);

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

            agregarFila(document, "N° de Mesa de Partes Instaladas", ": " + num(entity.getNumMujeresIndigenas())); // Ojo: mapeo según tu entidad
            agregarFila(document, "N° de Servidores que Brindaron Atención", ": " + num(entity.getNumPersonasNoIdiomaNacional()));
            agregarFila(document, "N° de Jueces que Brindaron Atención", ": " + num(entity.getNumJovenesQuechuaAymara()));

            boolean esPais = "SI".equalsIgnoreCase(entity.getCodigoAdcPueblosIndigenas()) || "01".equals(entity.getCodigoAdcPueblosIndigenas());
            agregarFila(document, "¿La Actividad en Convenio con PAIS?", ": " + (esPais ? "SI" : "NO"));
            if (esPais) {
                agregarFila(document, "Si es sí, Mencione el Tambo", ": " + obtenerNombreTambo(entity.getTambo()));
            }

            boolean esLengua = "SI".equalsIgnoreCase(entity.getCodigoSaeLenguaNativa()) || "01".equals(entity.getCodigoSaeLenguaNativa());
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

            // --- TEXTOS LARGOS ---
            agregarBloqueTexto(document, "IV. DESCRIPCIÓN DE LA ACTIVIDAD REALIZADA:", entity.getDescripcionActividad());
            agregarBloqueTexto(document, "V. INSTITUCIONES ALIADAS:", entity.getInstitucionesAliadas());
            agregarBloqueTexto(document, "VI. OBSERVACIONES:", entity.getObservaciones());

            // --- VII. ACTIVIDAD OPERATIVA ---
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("VII. ACTIVIDAD OPERATIVA REALIZADA:", FONT_BOLD_8));
            if (entity.getTareasRealizadas() != null && !entity.getTareasRealizadas().isEmpty()) {
                // Lógica para agrupar Actividad -> Indicador -> Tarea
                List<MaeTareaEntity> tareas = entity.getTareasRealizadas().stream()
                        .map(MovJiTareasRealizadasEntity::getTareaMaestra).filter(Objects::nonNull).collect(Collectors.toList());

                // Actividades Únicas
                tareas.stream().map(t -> t.getIndicador().getActividad()).distinct()
                        .forEach(act -> {
                            try { agregarBloqueSimple(document, act.getId() + " " + act.getDescripcion()); } catch (DocumentException e) {}
                        });

                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("a) Indicadores de la Actividad Operativa:", FONT_NORMAL_8));
                tareas.stream().map(MaeTareaEntity::getIndicador).distinct()
                        .forEach(ind -> {
                            try { agregarBloqueSimple(document, "     " + ind.getId() + " " + ind.getDescripcion()); } catch (DocumentException e) {}
                        });

                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("b) Tareas Realizadas de la Actividad Operativa:", FONT_NORMAL_8));
                tareas.forEach(tar -> {
                    try { agregarBloqueSimple(document, "          " + tar.getId() + " " + tar.getDescripcion()); } catch (DocumentException e) {}
                });

            } else {
                document.add(Chunk.NEWLINE);
                agregarBloqueSimpleCentrado(document, "SIN REGISTROS");
            }

            // --- VIII. ANEXOS (LINKS) ---
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("VIII. ANEXOS:", FONT_BOLD_8));

            // Usamos Font Link azul para simular enlaces
            Anchor linkFicha = new Anchor("Ver formato de atención (Haz clic aquí)", FONT_LINK);
            linkFicha.setReference(baseUrl + "/descargar/anexo/" + id);
            document.add(new Paragraph(linkFicha));

            Anchor linkVideo = new Anchor("Ver videos (Haz clic aquí)", FONT_LINK);
            linkVideo.setReference(baseUrl + "/visor/videos/" + id);
            document.add(new Paragraph(linkVideo));

            Anchor linkFoto = new Anchor("Ver fotografías (Haz clic aquí)", FONT_LINK);
            linkFoto.setReference(baseUrl + "/visor/fotos/" + id);
            document.add(new Paragraph(linkFoto));

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------", FONT_NORMAL_8));

            // --- PIE Y FIRMA ---
            agregarFila(document, "Fecha de registro", ": " + (entity.getFechaRegistro() != null ? entity.getFechaRegistro().toString() : ""));
            agregarFila(document, "Registrado por", ": " + val(entity.getUsuarioRegistro()));

            document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE); document.add(Chunk.NEWLINE);

            // Bloque Firma (Diseño estático para reemplazo futuro)
            Paragraph pLinea = new Paragraph("--------------------------------------------------", FONT_NORMAL_8);
            pLinea.setAlignment(Element.ALIGN_CENTER);
            document.add(pLinea);

            // Aquí iría el nombre del usuario si tuvieras la entidad Usuario, por ahora estático
            Paragraph pFirma = new Paragraph("USUARIO RESPONSABLE\nCARGO - DEPENDENCIA", FONT_BOLD_8);
            pFirma.setAlignment(Element.ALIGN_CENTER);
            document.add(pFirma);

            document.close();
            return out.toByteArray();
        }
    }

    // ============================================
    //            TABLAS Y LOGICA
    // ============================================

    private void crearTablaBeneficiadasHorizontal(Document doc, List<MovJiPersonasBeneficiadasEntity> lista) throws DocumentException {
        // Estructura idéntica al PHP: 3 grandes grupos, total 10 columnas
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{11,11,11, 11,11,11, 11,11,11, 12});

        // Cabeceras Superiores
        agregarCeldaHeader(table, "NIÑOS Y ADOLESCENTES", 3);
        agregarCeldaHeader(table, "JÓVENES Y ADULTOS", 3);
        agregarCeldaHeader(table, "ADULTOS MAYORES", 3);

        PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL", FONT_BOLD_7));
        cTotal.setRowspan(3); cTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cTotal.setHorizontalAlignment(Element.ALIGN_CENTER); cTotal.setBackgroundColor(COLOR_CABECERA);
        table.addCell(cTotal);

        // Subcabeceras Rangos
        agregarCeldaHeader(table, "0-17 AÑOS", 3);
        agregarCeldaHeader(table, "18-59 AÑOS", 3);
        agregarCeldaHeader(table, "60+ AÑOS", 3);

        // Cabeceras Sexo
        for(int i=0; i<3; i++) {
            agregarCeldaSubHeader(table, "F");
            agregarCeldaSubHeader(table, "M");
            agregarCeldaSubHeader(table, "LGTBIQ");
        }

        // Datos
        if (lista == null || lista.isEmpty()) {
            agregarCeldaVacia(table, 10, "SIN REGISTROS");
        } else {
            // Mapeamos por codigoRango (asumiendo 01=Niños, 02=Adultos, 03=Mayores)
            Map<String, MovJiPersonasBeneficiadasEntity> map = lista.stream()
                    .collect(Collectors.toMap(MovJiPersonasBeneficiadasEntity::getCodigoRango, x -> x));

            String[] codigos = {"01", "02", "03"};
            int granTotal = 0;

            for (String cod : codigos) {
                MovJiPersonasBeneficiadasEntity item = map.get(cod);
                int f = item != null ? num(item.getCantFemenino()) : 0;
                int m = item != null ? num(item.getCantMasculino()) : 0;
                int l = item != null ? num(item.getCantLgtbiq()) : 0;

                agregarCeldaDato(table, String.valueOf(f));
                agregarCeldaDato(table, String.valueOf(m));
                agregarCeldaDato(table, String.valueOf(l));
                granTotal += (f + m + l);
            }
            // Columna Total Final
            agregarCeldaDatoBold(table, String.valueOf(granTotal));
        }
        doc.add(table);
    }

    private void crearTablaAtendidasMatriz(Document doc, List<MovJiPersonasAtendidasEntity> lista) throws DocumentException {
        // Matriz compleja: Vulnerabilidad vs 4 Rangos de edad
        PdfPTable table = new PdfPTable(14); // 1 (Nombre) + 4 rangos * 3 sexos (12) + 1 Total = 14 cols
        table.setWidthPercentage(100);
        float[] widths = new float[14];
        widths[0] = 20f; // Nombre Vuln
        for(int i=1; i<=12; i++) widths[i] = 5f; // Datos numericos
        widths[13] = 8f; // Total fila
        table.setWidths(widths);

        // Cabecera Principal
        PdfPCell cTitulo = new PdfPCell(new Phrase("TIPO DE VULNERABILIDAD", FONT_BOLD_7));
        cTitulo.setRowspan(3); cTitulo.setBackgroundColor(COLOR_CABECERA); cTitulo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cTitulo);

        agregarCeldaHeader(table, "NIÑOS Y ADOLESCENTES", 3);
        agregarCeldaHeader(table, "JÓVENES", 3);
        agregarCeldaHeader(table, "ADULTOS", 3);
        agregarCeldaHeader(table, "ADULTOS MAYORES", 3);

        PdfPCell cTotal = new PdfPCell(new Phrase("TOTAL", FONT_BOLD_7));
        cTotal.setRowspan(3); cTotal.setBackgroundColor(COLOR_CABECERA); cTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cTotal);

        // Subcabeceras Rango
        agregarCeldaHeader(table, "0-17 AÑOS", 3);
        agregarCeldaHeader(table, "18-29 AÑOS", 3);
        agregarCeldaHeader(table, "30-59 AÑOS", 3);
        agregarCeldaHeader(table, "60+ AÑOS", 3);

        // Subcabeceras Sexo
        for(int i=0; i<4; i++) {
            agregarCeldaSubHeader(table, "F");
            agregarCeldaSubHeader(table, "M");
            agregarCeldaSubHeader(table, "L");
        }

        if (lista == null || lista.isEmpty()) {
            agregarCeldaVacia(table, 14, "SIN REGISTROS");
        } else {
            // Agrupar por Vulnerabilidad
            Map<Integer, List<MovJiPersonasAtendidasEntity>> porVuln = lista.stream()
                    .collect(Collectors.groupingBy(MovJiPersonasAtendidasEntity::getTipoVulnerabilidadId));

            for (Map.Entry<Integer, List<MovJiPersonasAtendidasEntity>> entry : porVuln.entrySet()) {
                String nombreVuln = obtenerNombreVulnerabilidad(entry.getKey());
                agregarCeldaDato(table, nombreVuln);

                // Mapear por rangoEdad ("01", "02", "03", "04")
                Map<String, MovJiPersonasAtendidasEntity> porRango = entry.getValue().stream()
                        .collect(Collectors.toMap(MovJiPersonasAtendidasEntity::getRangoEdad, x->x));

                String[] rangos = {"01", "02", "03", "04"};
                int totalFila = 0;

                for (String r : rangos) {
                    MovJiPersonasAtendidasEntity data = porRango.get(r);
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

    private void crearTablaCasos(Document doc, List<MovJiCasosAtendidosEntity> lista) throws DocumentException {
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        // Cabeceras
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

            for (MovJiCasosAtendidosEntity item : lista) {
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
            // Fila Total General
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
    //            HELPERS & UTILS
    // ============================================

    private void agregarCeldaHeader(PdfPTable table, String text, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BOLD_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(COLOR_CABECERA);
        if (colspan > 1) cell.setColspan(colspan);
        table.addCell(cell);
    }

    private void agregarCeldaSubHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BOLD_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(COLOR_CABECERA);
        table.addCell(cell);
    }

    private void agregarCeldaDato(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_NORMAL_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void agregarCeldaDatoBold(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BOLD_7));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
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

    // Helpers Maestros (Implementación simple, agregar los demás repos según necesidad)
    private String obtenerNombreCorte(String id) { return repoDistritoJud.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreEje(String id) { return repoEje.findById(id).map(e -> e.getDescripcion()).orElse(id); }
    private String obtenerNombreDepa(String id) { return repoDepa.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreProv(String id) { return repoProv.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreDist(String id) { return repoDist.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreTambo(String id) { return repoTambo.findById(id).map(e -> e.getNombre()).orElse(id); }
    private String obtenerNombreMateria(Integer id) { return repoMateria.findById(id).map(e -> e.getDescripcion()).orElse(String.valueOf(id)); }
    private String obtenerNombreVulnerabilidad(Integer id) { return repoVuln.findById(id).map(e -> e.getDescripcion()).orElse(String.valueOf(id)); }

    private String val(String s) { return s != null ? s : ""; }
    private int num(Integer i) { return i != null ? i : 0; }

    // CLASE INTERNA PARA CABECERA Y PIE
    class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                // Intento cargar logo
                try {
                    Image logo = Image.getInstance(getClass().getResource("/images/ENCABEZADO.JPG"));
                    logo.scaleToFit(500, 50);
                    logo.setAbsolutePosition(30, document.getPageSize().getHeight() - 70);
                    writer.getDirectContent().addImage(logo);
                } catch (Exception e) {
                    // Si falla imagen, no rompe el reporte
                }
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