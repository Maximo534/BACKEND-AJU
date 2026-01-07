package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import pe.gob.pj.prueba.domain.model.negocio.PromocionCultura;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromocionCulturaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromCulturaDetalleEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovPromCulturaTareaEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarPromocionRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromocionCulturaMapper {


    @Mapping(target = "participantes", source = "participantesPorGenero")
    @Mapping(target = "tareas", source = "tareasRealizadas")
    @Mapping(target = "modalidadProyecto", source = "modalidad")
    @Mapping(target = "lenguaNativaDesc", source = "lenguaNativaDesc")
    MovPromocionCulturaEntity toEntity(PromocionCultura domain);

    @InheritInverseConfiguration(name = "toEntity")
    PromocionCultura toDomain(MovPromocionCulturaEntity entity);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "participantesPorGenero", ignore = true)
    @Mapping(target = "tareasRealizadas", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    PromocionCultura toDomainResumen(MovPromocionCulturaEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "participantesPorGenero", source = "participantesPorGenero")
    @Mapping(target = "tareasRealizadas", source = "tareasRealizadas")
    @Mapping(target = "modalidad", source = "modalidadProyecto")
    @Mapping(target = "lenguaNativaDesc", source = "lenguaNativa")
    PromocionCultura toDomain(RegistrarPromocionRequest request);

    @Mapping(target = "promocionCulturaId", ignore = true)
    @Mapping(target = "descripcionRango", source = "descripcionRango")
    @Mapping(target = "codigoRango", source = "codigoRango")
    @Mapping(target = "cantidadFemenino", source = "cantidadFemenino")
    @Mapping(target = "cantidadMasculino", source = "cantidadMasculino")
    @Mapping(target = "cantidadLgtbiq", source = "cantidadLgtbiq")
    MovPromCulturaDetalleEntity toEntityPart(PromocionCultura.DetalleParticipante domainPart);

    PromocionCultura.DetalleParticipante toDomainPart(MovPromCulturaDetalleEntity entityPart);
    PromocionCultura.DetalleParticipante toDomainPart(RegistrarPromocionRequest.DetalleParticipantesRequest requestPart);


    @Mapping(target = "promocionCulturaId", ignore = true)
    @Mapping(target = "tareaId", source = "tareaId")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    MovPromCulturaTareaEntity toEntityTarea(PromocionCultura.DetalleTarea domainTarea);

    PromocionCultura.DetalleTarea toDomainTarea(MovPromCulturaTareaEntity entityTarea);
    PromocionCultura.DetalleTarea toDomainTarea(RegistrarPromocionRequest.DetalleTareaRequest requestTarea);
}