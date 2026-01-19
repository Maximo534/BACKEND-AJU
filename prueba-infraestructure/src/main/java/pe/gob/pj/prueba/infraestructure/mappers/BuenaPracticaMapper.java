package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovBuenaPracticaEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarBuenaPracticaRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.BuenaPracticaResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BuenaPracticaMapper {

    // 1. REQUEST -> DOMAIN
    @Mapping(target = "id", source = "id")
    @Mapping(target = "archivosGuardados", ignore = true)
    BuenaPractica toDomain(RegistrarBuenaPracticaRequest request);

    // 2. DOMAIN <-> ENTITY
    MovBuenaPracticaEntity toEntity(BuenaPractica domain);

    @InheritInverseConfiguration(name = "toEntity")
    BuenaPractica toDomain(MovBuenaPracticaEntity entity);

    // 3.(Domain -> Entity)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    void updateEntityFromDomain(BuenaPractica domain, @MappingTarget MovBuenaPracticaEntity entity);

    // 4. DOMAIN -> RESPONSE
    //Aquí mapeamos la lista de archivos para que salga en el JSON
    @Mapping(target = "archivos", source = "archivosGuardados")
    BuenaPracticaResponse toResponse(BuenaPractica domain);
}