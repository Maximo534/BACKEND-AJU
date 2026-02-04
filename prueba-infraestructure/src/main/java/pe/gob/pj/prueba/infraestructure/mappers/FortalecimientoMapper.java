package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.FortalecimientoCapacidades;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarFortalecimientoQuery;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarFfcRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarFfcRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.FortalecimientoResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface FortalecimientoMapper {

    // =========================================================
    // 1. FILTROS (REST Request -> Domain Query)
    // =========================================================
    @Mapping(target = "search", source = "search")
    @Mapping(target = "distritoJudicialId", source = "distritoJudicialId")
    @Mapping(target = "tipoEvento", source = "tipoEvento")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    ListarFortalecimientoQuery toQuery(ListarFfcRequest request);

    // =========================================================
    // 2. REGISTRAR (Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)

    // -- Auditoría Técnica (Viene de PeticionServicios, NO del Request) --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    // -- Listas Hijas --
    @Mapping(target = "participantes", source = "request.participantes")
    @Mapping(target = "tareasRealizadas", source = "request.tareasRealizadas")
    FortalecimientoCapacidades toDomainRegistrar(RegistrarFfcRequest request, PeticionServicios peticion);

    // =========================================================
    // 3. ACTUALIZAR (ID + Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "request.codigo")
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)

    // -- Auditoría de Modificación --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    // -- Listas Hijas --
    @Mapping(target = "participantes", source = "request.participantes")
    @Mapping(target = "tareasRealizadas", source = "request.tareasRealizadas")
    FortalecimientoCapacidades toDomainActualizar(Long id, RegistrarFfcRequest request, PeticionServicios peticion);

    // =========================================================
    // 4. PERSISTENCIA (Domain <-> Entity)
    // =========================================================
    @Mapping(target = "participantes", source = "participantes")
    @Mapping(target = "tareasRealizadas", source = "tareasRealizadas")

    // -- Mapeo Auditoría a Columnas BD --
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    // Campos automáticos de BD (ignorarlos)
    @Mapping(target = "FAud", ignore = true)
    @Mapping(target = "BAud", ignore = true)
    @Mapping(target = "fechaRegistroActividad", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)
    MovEventoFcEntity toEntity(FortalecimientoCapacidades domain);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "usuario", source = "CAudId")
    @Mapping(target = "nombrePc", source = "CAudPc")
    @Mapping(target = "numeroIp", source = "CAudIp")
    @Mapping(target = "direccionMac", source = "CAudMcAddr")
    FortalecimientoCapacidades toDomain(MovEventoFcEntity entity);

    // =========================================================
    // 5. UPDATE PARCIAL (Entity Update)
    // =========================================================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "fechaRegistroActividad", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)

    // Las listas se manejan manualmente en el Adapter
    @Mapping(target = "participantes", ignore = true)
    @Mapping(target = "tareasRealizadas", ignore = true)

    // Auditoría Update
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    void updateEntityFromDomain(FortalecimientoCapacidades domain, @MappingTarget MovEventoFcEntity entity);

    // =========================================================
    // 6. MAPPINGS DE HIJOS (Listas)
    // =========================================================

    // --- Participantes ---
    FortalecimientoCapacidades.DetalleParticipante toDomainPart(RegistrarFfcRequest.DetalleParticipantesRequest request);

    @Mapping(target = "eventoId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovEventoDetalleEntity toEntityPart(FortalecimientoCapacidades.DetalleParticipante domain);

    @InheritInverseConfiguration(name = "toEntityPart")
    FortalecimientoCapacidades.DetalleParticipante toDomainPart(MovEventoDetalleEntity entity);

    // --- Tareas ---
    FortalecimientoCapacidades.DetalleTarea toDomainTarea(RegistrarFfcRequest.DetalleTareaRequest request);

    @Mapping(target = "eventoId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovEventoTareaEntity toEntityTarea(FortalecimientoCapacidades.DetalleTarea domain);

    @InheritInverseConfiguration(name = "toEntityTarea")
    @Mapping(target = "descripcion", expression = "java(entity.getTareaMaestra() != null ? entity.getTareaMaestra().getDescripcion() : null)")
    FortalecimientoCapacidades.DetalleTarea toDomainTarea(MovEventoTareaEntity entity);

    // =========================================================
    // 7. RESPUESTA (Domain -> Response)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    FortalecimientoResponse toResponse(FortalecimientoCapacidades dominio);
}