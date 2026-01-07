package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import pe.gob.pj.prueba.domain.model.negocio.*;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarFjiRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JusticiaItineranteMapper {


    @Mapping(target = "personasAtendidas", source = "personasAtendidas")
    @Mapping(target = "casosAtendidos", source = "casosAtendidos")
    @Mapping(target = "personasBeneficiadas", source = "personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "tareasRealizadas")
    MovJusticiaItineranteEntity toEntity(JusticiaItinerante domain);

    @InheritInverseConfiguration
    JusticiaItinerante toDomain(MovJusticiaItineranteEntity entity);

    // --- PERSONAS ATENDIDAS (PA) ---
    @Mapping(target = "cantidadFemenino", source = "cantFemenino")
    @Mapping(target = "cantidadMasculino", source = "cantMasculino")
    @Mapping(target = "cantidadLgtbiq", source = "cantLgtbiq")
    MovJiPersonasAtendidasEntity toEntityPA(DetallePersonasAtendidas domain);

    @InheritInverseConfiguration
    DetallePersonasAtendidas toDomainPA(MovJiPersonasAtendidasEntity entity);

    // ---  CASOS ATENDIDOS (PCA) ---

    @Mapping(target = "cantidadDemandas", source = "numDemandas")
    @Mapping(target = "cantidadAudiencias", source = "numAudiencias")
    @Mapping(target = "cantidadSentencias", source = "numSentencias")
    @Mapping(target = "cantidadProcesos", source = "numProcesos")
    @Mapping(target = "cantidadNotificaciones", source = "numNotificaciones")
    @Mapping(target = "cantidadOrientaciones", source = "numOrientaciones")
    MovJiCasosAtendidosEntity toEntityPCA(DetalleCasosAtendidos domain);

    @InheritInverseConfiguration
    DetalleCasosAtendidos toDomainPCA(MovJiCasosAtendidosEntity entity);

    MovJiPersonasBeneficiadasEntity toEntityPB(DetallePersonasBeneficiadas domain);

    @InheritInverseConfiguration
    DetallePersonasBeneficiadas toDomainPB(MovJiPersonasBeneficiadasEntity entity);

    MovJiTareasRealizadasEntity toEntityTR(DetalleTareaRealizada domain);

    @InheritInverseConfiguration
    DetalleTareaRealizada toDomainTR(MovJiTareasRealizadasEntity entity);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "personasAtendidas", source = "personasAtendidas")
    @Mapping(target = "casosAtendidos", source = "casosAtendidos")
    @Mapping(target = "personasBeneficiadas", source = "personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "tareasRealizadas")
    JusticiaItinerante toDomain(RegistrarFjiRequest request);

    DetallePersonasAtendidas toDomainPA(RegistrarFjiRequest.DetallePARequest request);
    DetalleCasosAtendidos toDomainPCA(RegistrarFjiRequest.DetallePCARequest request);
    DetallePersonasBeneficiadas toDomainPB(RegistrarFjiRequest.DetallePBRequest request);
    DetalleTareaRealizada toDomainTR(RegistrarFjiRequest.DetalleTRRequest request);
}