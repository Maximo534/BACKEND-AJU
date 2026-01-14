package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovOrientadoraJudicialEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarOrientadoraRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.OrientadoraJudicialResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrientadoraJudicialMapper {

    // Request -> Domain (Incluye ID para updates)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    OrientadoraJudicial toDomain(RegistrarOrientadoraRequest request);

    // Domain -> Entity
    MovOrientadoraJudicialEntity toEntity(OrientadoraJudicial domain);

    // Entity -> Domain
    OrientadoraJudicial toDomain(MovOrientadoraJudicialEntity entity);

    // Update Entity (Protegemos campos de auditoría e ID)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    void updateEntityFromDomain(OrientadoraJudicial domain, @MappingTarget MovOrientadoraJudicialEntity entity);

    // Domain -> Response
    @Mapping(target = "nombrePersona", source = "nombreCompleto")
    @Mapping(target = "estado", constant = "REGISTRADO")
    @Mapping(target = "archivos", source = "archivosGuardados")
    OrientadoraJudicialResponse toResponse(OrientadoraJudicial domain);
}