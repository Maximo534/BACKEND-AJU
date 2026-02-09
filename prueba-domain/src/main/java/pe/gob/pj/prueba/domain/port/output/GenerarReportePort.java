package pe.gob.pj.prueba.domain.port.output;

import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;

import java.util.List;

public interface GenerarReportePort {
    byte[] generarFichaItinerante(Long idEvento) throws Exception;;
    byte[] generarFichaFortalecimiento(Long idEvento) throws Exception;
    byte[] generarFichaPromocion(Long idEvento) throws Exception;

    byte[] generarFichaBuenaPractica(String id) throws Exception;
    byte[] generarFichaJpe(String id) throws Exception;
    byte[] generarFichaLlj(String id) throws Exception;
    byte[] generarFichaOJ(String id) throws Exception;


    byte[] generarExcelListado(List<JusticiaItinerante> lista) throws Exception;
    byte[] generarExcelPromocion(List<PromocionCultura> lista) throws Exception;
    byte[] generarExcelFortalecimiento(List<FortalecimientoCapacidades> lista) throws Exception;
}