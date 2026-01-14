package pe.gob.pj.prueba.infraestructure.reports;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.port.output.GenerarReportePort;

@Component
@RequiredArgsConstructor
public class GenerarReporteAdapter implements GenerarReportePort {

    private final ReporteJusticiaItineranteService servicioItinerante;
    private final ReporteFortalecimientoService servicioFortalecimiento;
    private final ReportePromocionService servicioPromocion;
    private final ReporteBuenaPracticaService servicioBP;
    private final ReporteJpeService servicioJpe;
    private final ReporteLljService servicioLlj;
    private final ReporteOrientadoraJudicialService servicioOJ;

    @Override
    public byte[] generarFichaItinerante(String idEvento) throws Exception {
        return servicioItinerante.generarFichaItinerante(idEvento);
    }

    @Override
    public byte[] generarFichaFortalecimiento(String idEvento) throws Exception {
        return servicioFortalecimiento.generarPdf(idEvento);
    }

    @Override
    public byte[] generarFichaPromocion(String idEvento) throws Exception {
        return servicioPromocion.generarPdf(idEvento);
    }

    @Override
    public byte[] generarFichaBuenaPractica(String id) throws Exception {
        return servicioBP.generarFichaBuenaPractica(id);
    }
    @Override
    public byte[] generarFichaJpe(String id) throws Exception {
        return servicioJpe.generarFichaJpe(id);
    }

    @Override
    public byte[] generarFichaLlj(String id) throws Exception {
        return servicioLlj.generarFichaPdf(id);
    }
    @Override
    public byte[] generarFichaOJ(String id) throws Exception {
        return servicioOJ.generarFichaPdf(id);
    }

}