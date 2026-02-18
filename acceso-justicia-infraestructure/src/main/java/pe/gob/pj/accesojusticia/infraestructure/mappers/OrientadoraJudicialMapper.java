package pe.gob.pj.accesojusticia.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.domain.model.negocio.OrientadoraJudicial;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarOrientadoraQuery;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovOrientadoraJudicialEntity;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.ListarOrientadoraRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.RegistrarOrientadoraRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.OrientadoraJudicialResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface OrientadoraJudicialMapper {

    // =========================================================
    // 1. QUERY (Request -> Domain Query)
    // =========================================================
    ListarOrientadoraQuery toQuery(ListarOrientadoraRequest request);

    // =========================================================
    // 2. REGISTRAR (Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)

    // -- Auditoría Técnica desde PeticionServicios --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    OrientadoraJudicial toDomainRegistrar(RegistrarOrientadoraRequest request, PeticionServicios peticion);

    // =========================================================
    // 3. ACTUALIZAR (ID + Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)

    // -- Auditoría de Modificación --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    OrientadoraJudicial toDomainActualizar(Long id, RegistrarOrientadoraRequest request, PeticionServicios peticion);

    // =========================================================
    // 4. PERSISTENCIA (Domain <-> Entity)
    // =========================================================
    // -- Mapeo Auditoría a Columnas BD --
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")

    // Ignorar campos automáticos de BD
    @Mapping(target = "FAud", ignore = true)
    @Mapping(target = "BAud", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)
    MovOrientadoraJudicialEntity toEntity(OrientadoraJudicial domain);

    @InheritInverseConfiguration(name = "toEntity")
    // Mapeo inverso de auditoría para lectura
    @Mapping(target = "usuario", source = "CAudId")
    @Mapping(target = "nombrePc", source = "CAudPc")
    @Mapping(target = "numeroIp", source = "CAudIp")
    @Mapping(target = "direccionMac", source = "CAudMcAddr")
    OrientadoraJudicial toDomain(MovOrientadoraJudicialEntity entity);

    // =========================================================
    // 5. UPDATE PARCIAL (Entity Update)
    // =========================================================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)

    // Auditoría Update
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    void updateEntityFromDomain(OrientadoraJudicial domain, @MappingTarget MovOrientadoraJudicialEntity entity);

    // =========================================================
    // 6. RESPUESTA (Domain -> Response)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    @Mapping(target = "estado", constant = "REGISTRADO")
    OrientadoraJudicialResponse toResponse(OrientadoraJudicial domain);
}