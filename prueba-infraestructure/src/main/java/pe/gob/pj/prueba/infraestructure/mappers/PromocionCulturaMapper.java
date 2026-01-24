package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarPromocionRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.PromocionCulturaResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromocionCulturaMapper {

    // --- REQUEST -> DOMAIN ---
    @Mapping(target = "id", source = "id")
    @Mapping(target = "personasBeneficiadas", source = "personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "tareasRealizadas")
    @Mapping(target = "modalidad", source = "modalidadProyecto")
    @Mapping(target = "lenguaNativaDesc", source = "lenguaNativa")
    @Mapping(target = "archivosGuardados", ignore = true)
    PromocionCultura toDomain(RegistrarPromocionRequest request);

    // --- DOMAIN -> ENTITY ---
    @Mapping(target = "personasBeneficiadas", source = "personasBeneficiadas")
    @Mapping(target = "tareas", source = "tareasRealizadas")
    @Mapping(target = "modalidadProyecto", source = "modalidad")
    @Mapping(target = "lenguaNativaDesc", source = "lenguaNativaDesc")
    MovPromocionCulturaEntity toEntity(PromocionCultura domain);

    // --- ENTITY -> DOMAIN ---
    @InheritInverseConfiguration(name = "toEntity")
    PromocionCultura toDomain(MovPromocionCulturaEntity entity);

    // ✅ UPDATE: Ignorar listas para manejo manual en el UseCase
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "personasBeneficiadas", ignore = true)
    @Mapping(target = "tareas", ignore = true)
    @Mapping(target = "modalidadProyecto", source = "modalidad")
    @Mapping(target = "lenguaNativaDesc", source = "lenguaNativaDesc")
    void updateEntityFromDomain(PromocionCultura domain, @MappingTarget MovPromocionCulturaEntity entity);

    @Mapping(target = "promocionCulturaId", ignore = true)
    MovPromCulturaDetalleEntity toEntityPart(PromocionCultura.DetalleBeneficiada domainPart);
    PromocionCultura.DetalleBeneficiada toDomainPart(MovPromCulturaDetalleEntity entityPart);

    @Mapping(target = "promocionCulturaId", ignore = true)
    MovPromCulturaTareaEntity toEntityTarea(PromocionCultura.DetalleTarea domainTarea);
    PromocionCultura.DetalleTarea toDomainTarea(MovPromCulturaTareaEntity entityTarea);

    @Mapping(target = "tipoActividad", source = "tipoActividad")
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    PromocionCulturaResponse toResponse(PromocionCultura dominio);
}