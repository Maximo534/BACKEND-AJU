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
    // 1. QUERY
    // =========================================================
    @Mapping(target = "search", source = "search")
    @Mapping(target = "distritoJudicialId", source = "distritoJudicialId")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    ListarPromocionQuery toQuery(ListarPromocionRequest request);

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

    @Mapping(target = "personasBeneficiadas", source = "request.personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "request.tareasRealizadas")
    PromocionCultura toDomainRegistrar(RegistrarPromocionRequest request, PeticionServicios peticion);

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

    @Mapping(target = "personasBeneficiadas", source = "request.personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "request.tareasRealizadas")
    PromocionCultura toDomainActualizar(Long id, RegistrarPromocionRequest request, PeticionServicios peticion);

    // =========================================================
    // 4. ENTITY <-> DOMAIN
    // =========================================================
    @Mapping(target = "personasBeneficiadas", source = "personasBeneficiadas")
    @Mapping(target = "tareas", source = "tareasRealizadas")

    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "fechaRegistroActividad", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)

    @Mapping(target = "personasBeneficiadas", ignore = true)
    @Mapping(target = "tareas", ignore = true)

    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    void updateEntityFromDomain(PromocionCultura domain, @MappingTarget MovPromocionCulturaEntity entity);

    // =========================================================
    // 5. HIJOS
    // =========================================================

    // Personas Beneficiadas
    PromocionCultura.DetalleBeneficiada toDomainPB(RegistrarPromocionRequest.DetallePBRequest request);

    @Mapping(target = "promocionCulturaId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovPromCulturaDetalleEntity toEntityPB(PromocionCultura.DetalleBeneficiada domain);

    @InheritInverseConfiguration(name = "toEntityPB")
    PromocionCultura.DetalleBeneficiada toDomainPB(MovPromCulturaDetalleEntity entity);

    // Tareas
    PromocionCultura.DetalleTarea toDomainTarea(RegistrarPromocionRequest.DetalleTareaRequest request);

    @Mapping(target = "promocionCulturaId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovPromCulturaTareaEntity toEntityTarea(PromocionCultura.DetalleTarea domain);

    @InheritInverseConfiguration(name = "toEntityTarea")
    @Mapping(target = "descripcion", expression = "java(entity.getTareaMaestra() != null ? entity.getTareaMaestra().getDescripcion() : null)")
    PromocionCultura.DetalleTarea toDomainTarea(MovPromCulturaTareaEntity entity);

    // =========================================================
    // 6. RESPUESTAS JSON
    // =========================================================

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "distritoJudicialNombre", source = "distritoJudicialNombre")
    @Mapping(target = "nombreActividad", source = "nombreActividad")
    @Mapping(target = "tipoActividad", source = "tipoActividad")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "fechaRegistro", source = "fechaRegistro")
    PromocionCulturaResponse toResponseListado(PromocionCultura domain);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "distritoJudicialNombre", source = "distritoJudicialNombre")
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    PromocionCulturaResponse toResponseDetalle(PromocionCultura domain);

    // =========================================================
    // CÁLCULOS POST-MAPEO (IGUAL QUE EN ITINERANTE)
    // =========================================================
    @AfterMapping
    default void calcularTotales(MovPromocionCulturaEntity entity, @MappingTarget PromocionCultura domain) {

        // 1. TOTALES PARTICIPANTES
        if (entity.getPersonasBeneficiadas() != null) {
            domain.setTotalParticipantesFem(entity.getPersonasBeneficiadas().stream()
                    .mapToInt(p -> p.getCantidadFemenino() != null ? p.getCantidadFemenino() : 0).sum());

            domain.setTotalParticipantesMas(entity.getPersonasBeneficiadas().stream()
                    .mapToInt(p -> p.getCantidadMasculino() != null ? p.getCantidadMasculino() : 0).sum());

            domain.setTotalParticipantesLgtbi(entity.getPersonasBeneficiadas().stream()
                    .mapToInt(p -> p.getCantidadLgtbiq() != null ? p.getCantidadLgtbiq() : 0).sum());
        } else {
            domain.setTotalParticipantesFem(0);
            domain.setTotalParticipantesMas(0);
            domain.setTotalParticipantesLgtbi(0);
        }

        // 2. CANTIDAD TAREAS
        if (entity.getTareas() != null) {
            domain.setCantidadTareas(entity.getTareas().size());
        } else {
            domain.setCantidadTareas(0);
        }
    }
}