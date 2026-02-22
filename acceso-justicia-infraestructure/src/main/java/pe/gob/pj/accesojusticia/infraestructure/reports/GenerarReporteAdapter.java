package pe.gob.pj.accesojusticia.infraestructure.reports;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.gob.pj.accesojusticia.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.accesojusticia.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.accesojusticia.domain.model.negocio.PromocionCultura;
import pe.gob.pj.accesojusticia.domain.port.output.GenerarReportePort;

import java.util.List;

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
    private final ReporteJusticiaItineranteExcelService servicioItineranteExcel;
    private final ReportePromocionCulturaExcelService servicioPromocionExcel;
    private final ReporteFortalecimientoExcelService servicioFortalecimientoExcel;

    @Override
    public byte[] generarFichaItinerante(Long idEvento) throws Exception {
        return servicioItinerante.generarFichaItinerante(idEvento);
    }

    @Override
    public byte[] generarFichaFortalecimiento(Long idEvento) throws Exception {
        return servicioFortalecimiento.generarPdf(idEvento);
    }

    @Override
    public byte[] generarFichaPromocion(Long idEvento) throws Exception {
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
        return servicioLlj.generarFichaLlj(id);
    }
    @Override
    public byte[] generarFichaOJ(String id) throws Exception {
        return servicioOJ.generarFichaPdf(id);
    }



    @Override
    public byte[] generarExcelListado(List<JusticiaItinerante> lista) throws Exception {
        return servicioItineranteExcel.generarExcelListado(lista);
    }

    @Override
    public byte[] generarExcelPromocion(List<PromocionCultura> lista) throws Exception {
        return servicioPromocionExcel.generarExcel(lista);
    }

    @Override
    public byte[] generarExcelFortalecimiento(List<FortalecimientoCapacidades> lista) throws Exception {
        return servicioFortalecimientoExcel.generarExcel(lista);
    }
}