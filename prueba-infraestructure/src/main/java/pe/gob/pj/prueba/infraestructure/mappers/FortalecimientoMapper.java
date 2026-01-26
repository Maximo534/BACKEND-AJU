package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarFfcRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.FortalecimientoResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FortalecimientoMapper {

    // REQUEST -> DOMAIN
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    FortalecimientoCapacidades toDomain(RegistrarFfcRequest request);

    // DOMAIN -> ENTITY
    MovEventoFcEntity toEntity(FortalecimientoCapacidades domain);

    // ENTITY -> DOMAIN
    @InheritInverseConfiguration(name = "toEntity")
    FortalecimientoCapacidades toDomain(MovEventoFcEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "participantes", ignore = true)
    @Mapping(target = "tareasRealizadas", ignore = true)
    void updateEntityFromDomain(FortalecimientoCapacidades domain, @MappingTarget MovEventoFcEntity entity);

    // --- MÉTODOS AUXILIARES ---
    FortalecimientoCapacidades.DetalleParticipante toDomainPart(RegistrarFfcRequest.DetalleParticipantesRequest request);
    FortalecimientoCapacidades.DetalleTarea toDomainTarea(RegistrarFfcRequest.DetalleTareaRequest request);

    @Mapping(target = "eventoId", ignore = true)
    MovEventoDetalleEntity toEntityPart(FortalecimientoCapacidades.DetalleParticipante domain);

    @InheritInverseConfiguration
    FortalecimientoCapacidades.DetalleParticipante toDomainPart(MovEventoDetalleEntity entity);

    @Mapping(target = "eventoId", ignore = true)
    MovEventoTareaEntity toEntityTarea(FortalecimientoCapacidades.DetalleTarea domain);

    @InheritInverseConfiguration
    FortalecimientoCapacidades.DetalleTarea toDomainTarea(MovEventoTareaEntity entity);

    // DOMAIN -> RESPONSE
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    FortalecimientoResponse toResponse(FortalecimientoCapacidades dominio);
}