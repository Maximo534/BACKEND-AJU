package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovOrientadoraJudicialEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarOrientadoraRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.OrientadoraJudicialResponse;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrientadoraJudicialMapper {

    OrientadoraJudicial toDomain(RegistrarOrientadoraRequest request);

    MovOrientadoraJudicialEntity toEntity(OrientadoraJudicial domain);

    OrientadoraJudicial toDomain(MovOrientadoraJudicialEntity entity);

    @Mapping(target = "nombrePersona", source = "nombreCompleto")
    @Mapping(target = "estado", constant = "REGISTRADO")
    OrientadoraJudicialResponse toResponse(OrientadoraJudicial domain);
}