package pe.gob.pj.prueba.domain.port.output;

public interface GenerarReportePort {
    byte[] generarFichaItinerante(Long idEvento) throws Exception;;
    byte[] generarFichaFortalecimiento(Long idEvento) throws Exception;
    byte[] generarFichaPromocion(String idEvento) throws Exception;

    byte[] generarFichaBuenaPractica(String id) throws Exception;
    byte[] generarFichaJpe(String id) throws Exception;
    byte[] generarFichaLlj(String id) throws Exception;
    byte[] generarFichaOJ(String id) throws Exception;
}