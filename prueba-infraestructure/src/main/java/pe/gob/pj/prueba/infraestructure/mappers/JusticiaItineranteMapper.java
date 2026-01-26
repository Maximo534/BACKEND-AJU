package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarFjiRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.JusticiaItineranteResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JusticiaItineranteMapper {

    // --- MAPPINGS DE ENTIDAD Y DOMINIO ---
    @Mapping(target = "personasAtendidas", source = "personasAtendidas")
    @Mapping(target = "casosAtendidos", source = "casosAtendidos")
    @Mapping(target = "personasBeneficiadas", source = "personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "tareasRealizadas")
    MovJusticiaItineranteEntity toEntity(JusticiaItinerante domain);

    @InheritInverseConfiguration(name = "toEntity")
    JusticiaItinerante toDomain(MovJusticiaItineranteEntity entity);

    // Mappings Hijos
    @Mapping(target = "cantidadFemenino", source = "cantFemenino")
    @Mapping(target = "cantidadMasculino", source = "cantMasculino")
    @Mapping(target = "cantidadLgtbiq", source = "cantLgtbiq")
    MovJiPersonasAtendidasEntity toEntityPA(JusticiaItinerante.DetalleAtendida d);
    @InheritInverseConfiguration
    JusticiaItinerante.DetalleAtendida toDomainPA(MovJiPersonasAtendidasEntity e);

    @Mapping(target = "cantidadDemandas", source = "numDemandas")
    @Mapping(target = "cantidadAudiencias", source = "numAudiencias")
    @Mapping(target = "cantidadSentencias", source = "numSentencias")
    @Mapping(target = "cantidadProcesos", source = "numProcesos")
    @Mapping(target = "cantidadNotificaciones", source = "numNotificaciones")
    @Mapping(target = "cantidadOrientaciones", source = "numOrientaciones")
    MovJiCasosAtendidosEntity toEntityPCA(JusticiaItinerante.DetalleCaso d);
    @InheritInverseConfiguration
    JusticiaItinerante.DetalleCaso toDomainPCA(MovJiCasosAtendidosEntity e);

    MovJiPersonasBeneficiadasEntity toEntityPB(JusticiaItinerante.DetalleBeneficiada d);
    @InheritInverseConfiguration
    JusticiaItinerante.DetalleBeneficiada toDomainPB(MovJiPersonasBeneficiadasEntity e);

    MovJiTareasRealizadasEntity toEntityTR(JusticiaItinerante.DetalleTarea d);
    @InheritInverseConfiguration
    @Mapping(target = "descripcion", expression = "java(e.getTareaMaestra() != null ? e.getTareaMaestra().getDescripcion() : null)")
    JusticiaItinerante.DetalleTarea toDomainTR(MovJiTareasRealizadasEntity e);

    // Update
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "personasAtendidas", ignore = true)
    @Mapping(target = "casosAtendidos", ignore = true)
    @Mapping(target = "personasBeneficiadas", ignore = true)
    @Mapping(target = "tareasRealizadas", ignore = true)
    void updateEntityFromDomain(JusticiaItinerante domain, @MappingTarget MovJusticiaItineranteEntity entity);

    // Request -> Domain (Igual)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "publicoObjetivoDetalle", source = "publicoObjetivoDetalle", defaultValue = "NINGUNO")
    @Mapping(target = "tambo", source = "tambo", defaultValue = "NINGUNO")
    @Mapping(target = "lenguaNativa", source = "lenguaNativa", defaultValue = "NINGUNO")
    @Mapping(target = "codigoAdcPueblosIndigenas", source = "codigoAdcPueblosIndigenas", defaultValue = "00")
    @Mapping(target = "codigoSaeLenguaNativa", source = "codigoSaeLenguaNativa", defaultValue = "00")
    @Mapping(target = "numMesasInstaladas", source = "numMesasInstaladas", defaultValue = "0")
    @Mapping(target = "numServidores", source = "numServidores", defaultValue = "0")
    @Mapping(target = "numJueces", source = "numJueces", defaultValue = "0")
    @Mapping(target = "descripcionActividad", source = "descripcionActividad", defaultValue = "")
    @Mapping(target = "institucionesAliadas", source = "institucionesAliadas", defaultValue = "")
    @Mapping(target = "observaciones", source = "observaciones", defaultValue = "")
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    JusticiaItinerante toDomain(RegistrarFjiRequest request);

    JusticiaItinerante.DetalleAtendida toDomainPA(RegistrarFjiRequest.DetallePARequest r);
    JusticiaItinerante.DetalleCaso toDomainPCA(RegistrarFjiRequest.DetallePCARequest r);
    JusticiaItinerante.DetalleBeneficiada toDomainPB(RegistrarFjiRequest.DetallePBRequest r);
    JusticiaItinerante.DetalleTarea toDomainTR(RegistrarFjiRequest.DetalleTRRequest r);
    // LISTADO 
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    JusticiaItineranteResponse toResponseListado(JusticiaItinerante dominio);

    // DETALLE
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    JusticiaItineranteResponse toResponseDetalle(JusticiaItinerante dominio);
}