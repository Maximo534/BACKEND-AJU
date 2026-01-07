package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarFfcRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FortalecimientoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)

    // El Request trae "tareasRealizadas", el Dominio espera "tareas"
    @Mapping(target = "tareas", source = "tareasRealizadas")
    // El Request trae "participantesPorGenero", el Dominio espera "participantes"
    @Mapping(target = "participantes", source = "participantesPorGenero")
    FortalecimientoCapacidades toDomain(RegistrarFfcRequest request);

    //DOMAIN -> ENTITY ---
    MovEventoFcEntity toEntity(FortalecimientoCapacidades domain);

    //ENTITY -> DOMAIN---
    @InheritInverseConfiguration
    FortalecimientoCapacidades toDomain(MovEventoFcEntity entity);

    // --- MÉTODOS AUXILIARES PARA LAS LISTAS ---

    // Detalle Participantes: Request -> Domain
    FortalecimientoCapacidades.DetalleParticipante toDomainPart(RegistrarFfcRequest.DetalleParticipantesRequest request);

    // Detalle Tareas: Request -> Domain
    FortalecimientoCapacidades.DetalleTarea toDomainTarea(RegistrarFfcRequest.DetalleTareaRequest request);

    // Detalle Participantes: Domain -> Entity
    @Mapping(target = "eventoId", ignore = true)
    MovEventoDetalleEntity toEntityPart(FortalecimientoCapacidades.DetalleParticipante domain);

    @InheritInverseConfiguration
    FortalecimientoCapacidades.DetalleParticipante toDomainPart(MovEventoDetalleEntity entity);

    @Mapping(target = "eventoId", ignore = true)
    MovEventoTareaEntity toEntityTarea(FortalecimientoCapacidades.DetalleTarea domain);

    @InheritInverseConfiguration
    FortalecimientoCapacidades.DetalleTarea toDomainTarea(MovEventoTareaEntity entity);
}