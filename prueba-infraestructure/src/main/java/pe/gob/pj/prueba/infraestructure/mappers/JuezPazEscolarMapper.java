package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarJuezEscolarQuery;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeJuezPazEscolarEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarJuezEscolarRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarJuezRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.JuezPazEscolarResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface JuezPazEscolarMapper {

    // =========================================================
    // 1. QUERY (Request -> Domain Query)
    // =========================================================
    ListarJuezEscolarQuery toQuery(ListarJuezEscolarRequest request);

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

    JuezPazEscolar toDomainRegistrar(RegistrarJuezRequest request, PeticionServicios peticion);

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

    JuezPazEscolar toDomainActualizar(Long id, RegistrarJuezRequest request, PeticionServicios peticion);

    // =========================================================
    // 4. PERSISTENCIA (Domain <-> Entity)
    // =========================================================
    @Mapping(target = "institucionEducativa", ignore = true)

    // -- Mapeo Auditoría a Columnas BD --
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")

    // Ignorar campos automáticos de BD
    @Mapping(target = "FAud", ignore = true)
    @Mapping(target = "BAud", ignore = true)
    @Mapping(target = "fechaRegistroActividad", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)
    MaeJuezPazEscolarEntity toEntity(JuezPazEscolar domain);

    @InheritInverseConfiguration(name = "toEntity")
    // Mapeo inverso de auditoría
    @Mapping(target = "usuario", source = "CAudId")
    @Mapping(target = "nombrePc", source = "CAudPc")
    @Mapping(target = "numeroIp", source = "CAudIp")
    @Mapping(target = "direccionMac", source = "CAudMcAddr")
    JuezPazEscolar toDomain(MaeJuezPazEscolarEntity entity);

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
    @Mapping(target = "institucionEducativa", ignore = true)

    // Auditoría Update
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    void updateEntityFromDomain(JuezPazEscolar domain, @MappingTarget MaeJuezPazEscolarEntity entity);

    // =========================================================
    // 6. RESPUESTA (Domain -> Response)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    @Mapping(target = "nombreCompleto", expression = "java(domain.getNombres() + ' ' + domain.getApePaterno() + ' ' + domain.getApeMaterno())")
    JuezPazEscolarResponse toResponse(JuezPazEscolar domain);
}