package pe.gob.pj.accesojusticia.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarJpeCasosQuery;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovJpeCasoAtendidoEntity;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.ListarJpeCasosRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.RegistrarCasoRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.JpeCasoAtendidoResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface JusticiaPazMapper {

    // =========================================================
    // 1. QUERY (Request -> Domain Query)
    // =========================================================
    ListarJpeCasosQuery toQuery(ListarJpeCasosRequest request);

    // =========================================================
    // 2. REGISTRAR (Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "fechaRegistroCaso", source = "request.fechaRegistro")
    @Mapping(target = "archivosGuardados", ignore = true)

    // -- Auditoría Técnica desde PeticionServicios --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    JpeCasoAtendido toDomainRegistrar(RegistrarCasoRequest request, PeticionServicios peticion);

    // =========================================================
    // 3. ACTUALIZAR (ID + Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "fechaRegistroCaso", source = "request.fechaRegistro")
    @Mapping(target = "archivosGuardados", ignore = true)

    // -- Auditoría de Modificación --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    JpeCasoAtendido toDomainActualizar(Long id, RegistrarCasoRequest request, PeticionServicios peticion);

    // =========================================================
    // 4. PERSISTENCIA (Domain <-> Entity)
    // =========================================================
    @Mapping(target = "juezEscolar", ignore = true)

    // -- Mapeo Auditoría a Columnas BD --
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")

    // Ignorar campos automáticos de BD
    @Mapping(target = "FAud", ignore = true)
    @Mapping(target = "BAud", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)
    MovJpeCasoAtendidoEntity toEntity(JpeCasoAtendido domain);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "usuario", source = "CAudId")
    @Mapping(target = "nombrePc", source = "CAudPc")
    @Mapping(target = "numeroIp", source = "CAudIp")
    @Mapping(target = "direccionMac", source = "CAudMcAddr")
    JpeCasoAtendido toDomain(MovJpeCasoAtendidoEntity entity);

    // =========================================================
    // 5. UPDATE PARCIAL (Entity Update)
    // =========================================================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)
    @Mapping(target = "juezEscolar", ignore = true)

    // Auditoría Update
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    void updateEntityFromDomain(JpeCasoAtendido domain, @MappingTarget MovJpeCasoAtendidoEntity entity);

    // =========================================================
    // 6. RESPUESTA (Domain -> Response)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "fechaRegistro", source = "fechaRegistroCaso")
    @Mapping(target = "archivos", source = "archivosGuardados")
    @Mapping(target = "estado", constant = "REGISTRADO")
    JpeCasoAtendidoResponse toResponse(JpeCasoAtendido domain);
}