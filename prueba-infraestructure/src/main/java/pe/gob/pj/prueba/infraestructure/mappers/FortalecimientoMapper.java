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
    // 1. FILTROS
    // =========================================================
    @Mapping(target = "search", source = "search")
    @Mapping(target = "distritoJudicialId", source = "distritoJudicialId")
    @Mapping(target = "tipoEvento", source = "tipoEvento")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    ListarFortalecimientoQuery toQuery(ListarFfcRequest request);

    // =========================================================
    // 2. REGISTRAR
    // =========================================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", source = "request.fechaInicio")
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "usuario", source = "peticion.usuario")
    @Mapping(target = "activo", ignore = true)
//    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")
    @Mapping(target = "participantes", source = "request.participantes")
    @Mapping(target = "tareasRealizadas", source = "request.tareasRealizadas")
    FortalecimientoCapacidades toDomainRegistrar(RegistrarFfcRequest request, PeticionServicios peticion);

    // =========================================================
    // 3. ACTUALIZAR
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "request.codigo")
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")
    @Mapping(target = "participantes", source = "request.participantes")
    @Mapping(target = "tareasRealizadas", source = "request.tareasRealizadas")
    FortalecimientoCapacidades toDomainActualizar(Long id, RegistrarFfcRequest request, PeticionServicios peticion);

    // =========================================================
    // 4. ENTITY <-> DOMAIN
    // =========================================================
    @Mapping(target = "participantes", source = "participantes")
    @Mapping(target = "tareasRealizadas", source = "tareasRealizadas")
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "fechaRegistroActividad", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)
    @Mapping(target = "participantes", ignore = true)
    @Mapping(target = "tareasRealizadas", ignore = true)
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    void updateEntityFromDomain(FortalecimientoCapacidades domain, @MappingTarget MovEventoFcEntity entity);

    // =========================================================
    // 5. HIJOS
    // =========================================================
    FortalecimientoCapacidades.DetalleParticipante toDomainPart(RegistrarFfcRequest.DetalleParticipantesRequest request);
    @Mapping(target = "eventoId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovEventoDetalleEntity toEntityPart(FortalecimientoCapacidades.DetalleParticipante domain);
    @InheritInverseConfiguration(name = "toEntityPart")
    @Mapping(target = "descripcionTipoParticipante", expression = "java(entity.getTipoParticipanteMaestro() != null ? entity.getTipoParticipanteMaestro().getDescripcion() : null)")
    FortalecimientoCapacidades.DetalleParticipante toDomainPart(MovEventoDetalleEntity entity);

    FortalecimientoCapacidades.DetalleTarea toDomainTarea(RegistrarFfcRequest.DetalleTareaRequest request);
    @Mapping(target = "eventoId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovEventoTareaEntity toEntityTarea(FortalecimientoCapacidades.DetalleTarea domain);
    @InheritInverseConfiguration(name = "toEntityTarea")
    @Mapping(target = "descripcion", expression = "java(entity.getTareaMaestra() != null ? entity.getTareaMaestra().getDescripcion() : null)")
    FortalecimientoCapacidades.DetalleTarea toDomainTarea(MovEventoTareaEntity entity);

    // =========================================================
    // 6. RESPUESTAS JSON (Domain -> Response)
    // =========================================================

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "distritoJudicialNombre", source = "distritoJudicialNombre")
    @Mapping(target = "tipoEvento", source = "tipoEvento")
    @Mapping(target = "nombreEvento", source = "nombreEvento")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "fechaRegistro", source = "fechaRegistro")
    FortalecimientoResponse toResponseListado(FortalecimientoCapacidades dominio);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "distritoJudicialNombre", source = "distritoJudicialNombre")
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    FortalecimientoResponse toResponseDetalle(FortalecimientoCapacidades dominio);

    // =========================================================
    // CÁLCULOS POST-MAPEO
    // =========================================================
    @AfterMapping
    default void calcularTotales(MovEventoFcEntity entity, @MappingTarget FortalecimientoCapacidades domain) {

        // A. PARTICIPANTES
        if (entity.getParticipantes() != null) {
            domain.setTotalParticipantesFem(entity.getParticipantes().stream()
                    .mapToInt(p -> p.getCantidadFemenino() != null ? p.getCantidadFemenino() : 0).sum());

            domain.setTotalParticipantesMas(entity.getParticipantes().stream()
                    .mapToInt(p -> p.getCantidadMasculino() != null ? p.getCantidadMasculino() : 0).sum());

            domain.setTotalParticipantesLgtbi(entity.getParticipantes().stream()
                    .mapToInt(p -> p.getCantidadLgtbiq() != null ? p.getCantidadLgtbiq() : 0).sum());
        } else {
            domain.setTotalParticipantesFem(0);
            domain.setTotalParticipantesMas(0);
            domain.setTotalParticipantesLgtbi(0);
        }

        // B. TAREAS
        if (entity.getTareasRealizadas() != null) {
            domain.setCantidadTareas(entity.getTareasRealizadas().size());
        } else {
            domain.setCantidadTareas(0);
        }
    }
}