package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarBuenaPracticaQuery;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovBuenaPracticaEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarBuenaPracticaRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarBuenaPracticaRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.BuenaPracticaResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface BuenaPracticaMapper {

    // =========================================================
    // 1. QUERY (Request -> Domain Query)
    // =========================================================
    @Mapping(target = "search", source = "search")
    @Mapping(target = "distritoJudicialId", source = "distritoJudicialId")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    ListarBuenaPracticaQuery toQuery(ListarBuenaPracticaRequest request);

    // =========================================================
    // 2. REGISTRAR (Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)

    // -- Auditoría Técnica desde PeticionServicios --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    BuenaPractica toDomainRegistrar(RegistrarBuenaPracticaRequest request, PeticionServicios peticion);

    // =========================================================
    // 3. ACTUALIZAR (ID + Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", ignore = true)
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

    BuenaPractica toDomainActualizar(Long id, RegistrarBuenaPracticaRequest request, PeticionServicios peticion);

    // =========================================================
    // 4. PERSISTENCIA (Domain <-> Entity)
    // =========================================================
    // -- Mapeo Auditoría a Columnas BD --
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    @Mapping(target = "FAud", ignore = true)
    @Mapping(target = "BAud", ignore = true)
    @Mapping(target = "fechaRegistroActividad", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)
    MovBuenaPracticaEntity toEntity(BuenaPractica domain);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "usuario", source = "CAudId")
    @Mapping(target = "nombrePc", source = "CAudPc")
    @Mapping(target = "numeroIp", source = "CAudIp")
    @Mapping(target = "direccionMac", source = "CAudMcAddr")
    BuenaPractica toDomain(MovBuenaPracticaEntity entity);

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

    // Auditoría Update
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    void updateEntityFromDomain(BuenaPractica domain, @MappingTarget MovBuenaPracticaEntity entity);

    // =========================================================
    // 6. RESPUESTA (Domain -> Response)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    BuenaPracticaResponse toResponse(BuenaPractica domain);
}