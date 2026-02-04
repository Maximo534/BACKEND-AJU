package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarPromocionQuery;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarPromocionRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarPromocionRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.PromocionCulturaResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface PromocionCulturaMapper {

    // =========================================================
    // 1. QUERY (Request -> Domain Query)
    // =========================================================
    @Mapping(target = "search", source = "search")
    @Mapping(target = "distritoJudicialId", source = "distritoJudicialId")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    ListarPromocionQuery toQuery(ListarPromocionRequest request);

    // =========================================================
    // 2. REGISTRAR (Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)

    // -- Mapeo de Nombres Diferentes (Request vs Domain) --
    @Mapping(target = "modalidad", source = "request.modalidadProyecto")
    @Mapping(target = "lenguaNativaDesc", source = "request.lenguaNativa")

    // -- Auditoría desde PeticionServicios --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    // -- Listas Hijas --
    @Mapping(target = "personasBeneficiadas", source = "request.personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "request.tareasRealizadas")
    PromocionCultura toDomainRegistrar(RegistrarPromocionRequest request, PeticionServicios peticion);

    // =========================================================
    // 3. ACTUALIZAR (ID + Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)

    // -- Mapeo de Nombres Diferentes --
    @Mapping(target = "modalidad", source = "request.modalidadProyecto")
    @Mapping(target = "lenguaNativaDesc", source = "request.lenguaNativa")

    // -- Auditoría de Modificación --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    // -- Listas Hijas --
    @Mapping(target = "personasBeneficiadas", source = "request.personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "request.tareasRealizadas")
    PromocionCultura toDomainActualizar(Long id, RegistrarPromocionRequest request, PeticionServicios peticion);

    // =========================================================
    // 4. PERSISTENCIA (Domain <-> Entity)
    // =========================================================
    @Mapping(target = "personasBeneficiadas", source = "personasBeneficiadas")
    @Mapping(target = "tareas", source = "tareasRealizadas")

    // Mapeo inverso de campos de negocio
    @Mapping(target = "modalidadProyecto", source = "modalidad")
    @Mapping(target = "lenguaNativaDesc", source = "lenguaNativaDesc")

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
    MovPromocionCulturaEntity toEntity(PromocionCultura domain);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "usuario", source = "CAudId")
    @Mapping(target = "nombrePc", source = "CAudPc")
    @Mapping(target = "numeroIp", source = "CAudIp")
    @Mapping(target = "direccionMac", source = "CAudMcAddr")
    PromocionCultura toDomain(MovPromocionCulturaEntity entity);

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

    @Mapping(target = "personasBeneficiadas", ignore = true)
    @Mapping(target = "tareas", ignore = true)

    // Campos de negocio específicos
    @Mapping(target = "modalidadProyecto", source = "modalidad")
    @Mapping(target = "lenguaNativaDesc", source = "lenguaNativaDesc")

    // Auditoría Update
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    void updateEntityFromDomain(PromocionCultura domain, @MappingTarget MovPromocionCulturaEntity entity);

    // =========================================================
    // 6. MAPPINGS DE HIJOS (Listas)
    // =========================================================

    // --- Personas Beneficiadas ---
    PromocionCultura.DetalleBeneficiada toDomainPB(RegistrarPromocionRequest.DetallePBRequest request);

    @Mapping(target = "promocionCulturaId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovPromCulturaDetalleEntity toEntityPB(PromocionCultura.DetalleBeneficiada domain);

    @InheritInverseConfiguration(name = "toEntityPB")
    PromocionCultura.DetalleBeneficiada toDomainPB(MovPromCulturaDetalleEntity entity);

    // --- Tareas ---
    PromocionCultura.DetalleTarea toDomainTarea(RegistrarPromocionRequest.DetalleTareaRequest request);

    @Mapping(target = "promocionCulturaId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovPromCulturaTareaEntity toEntityTarea(PromocionCultura.DetalleTarea domain);

    @InheritInverseConfiguration(name = "toEntityTarea")
    @Mapping(target = "descripcion", expression = "java(entity.getTareaMaestra() != null ? entity.getTareaMaestra().getDescripcion() : null)")
    PromocionCultura.DetalleTarea toDomainTarea(MovPromCulturaTareaEntity entity);

    // =========================================================
    // 7. RESPUESTA (Domain -> Response)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    PromocionCulturaResponse toResponse(PromocionCultura domain);
}