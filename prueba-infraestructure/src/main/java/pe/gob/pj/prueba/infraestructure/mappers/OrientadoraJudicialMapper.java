package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovOrientadoraJudicialEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarOrientadoraRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.OrientadoraJudicialResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrientadoraJudicialMapper {

    // Request -> Domain
    @Mapping(target = "archivosGuardados", ignore = true) // No viene en el body JSON/Form
    @Mapping(target = "usuarioRegistro", ignore = true)   // Se setea en el UseCase
    OrientadoraJudicial toDomain(RegistrarOrientadoraRequest request);

    // Domain -> Entity
    MovOrientadoraJudicialEntity toEntity(OrientadoraJudicial domain);

    // Entity -> Domain
    OrientadoraJudicial toDomain(MovOrientadoraJudicialEntity entity);

    // Update Entity (Protegemos ID y Usuario)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    void updateEntityFromDomain(OrientadoraJudicial domain, @MappingTarget MovOrientadoraJudicialEntity entity);

    // Domain -> Response
    // Asignamos una constante a 'estado' si no existe en la tabla nueva
    @Mapping(target = "estado", constant = "REGISTRADO")
    @Mapping(target = "archivos", source = "archivosGuardados")
    OrientadoraJudicialResponse toResponse(OrientadoraJudicial domain);
}