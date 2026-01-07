package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarLlapanchikpaqRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.LlapanchikpaqResponse;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LlapanchikpaqMapper {

    // Request -> Dominio
    LlapanchikpaqJusticia toDomain(RegistrarLlapanchikpaqRequest request);

    // Dominio -> Entity (Principal)
    MovLlapanchikpaqJusticia toEntity(LlapanchikpaqJusticia domain);

    // Domain -> Response
    @Mapping(target = "estado", constant = "REGISTRADO")
    LlapanchikpaqResponse toResponse(LlapanchikpaqJusticia domain);

    // --- MAPPERS PARA DETALLES (Dominio -> Entity) ---
    // MapStruct detecta los nombres iguales y mapea automáticamente.

    MovLljPersonasBeneficiadasEntity mapBeneficiada(LlapanchikpaqJusticia.DetalleBeneficiada detalle);

    MovLljPersonasAtendidasEntity mapAtendida(LlapanchikpaqJusticia.DetalleAtendida detalle);

    MovLljCasosAtendidosEntity mapCaso(LlapanchikpaqJusticia.DetalleCaso detalle);

    MovLljTareaRealizadasEntity mapTarea(LlapanchikpaqJusticia.DetalleTarea detalle);
}