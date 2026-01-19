package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarFjiRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.JusticiaItineranteResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JusticiaItineranteMapper {

    // 1. ENTITY <-> DOMAIN (Persistencia)
    @Mapping(target = "personasAtendidas", source = "personasAtendidas")
    @Mapping(target = "casosAtendidos", source = "casosAtendidos")
    @Mapping(target = "personasBeneficiadas", source = "personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "tareasRealizadas")
    MovJusticiaItineranteEntity toEntity(JusticiaItinerante domain);

    @InheritInverseConfiguration(name = "toEntity")
    JusticiaItinerante toDomain(MovJusticiaItineranteEntity entity);

    // --- Mappings Específicos para Listas Hijas (Entity <-> Domain) ---
    // MapStruct los usa automáticamente cuando convierte las listas

    // Personas Atendidas
    @Mapping(target = "cantidadFemenino", source = "cantFemenino")
    @Mapping(target = "cantidadMasculino", source = "cantMasculino")
    @Mapping(target = "cantidadLgtbiq", source = "cantLgtbiq")
    MovJiPersonasAtendidasEntity toEntityPA(JusticiaItinerante.DetalleAtendida d);
    @InheritInverseConfiguration
    JusticiaItinerante.DetalleAtendida toDomainPA(MovJiPersonasAtendidasEntity e);

    // Casos Atendidos
    @Mapping(target = "cantidadDemandas", source = "numDemandas")
    @Mapping(target = "cantidadAudiencias", source = "numAudiencias")
    @Mapping(target = "cantidadSentencias", source = "numSentencias")
    @Mapping(target = "cantidadProcesos", source = "numProcesos")
    @Mapping(target = "cantidadNotificaciones", source = "numNotificaciones")
    @Mapping(target = "cantidadOrientaciones", source = "numOrientaciones")
    MovJiCasosAtendidosEntity toEntityPCA(JusticiaItinerante.DetalleCaso d);
    @InheritInverseConfiguration
    JusticiaItinerante.DetalleCaso toDomainPCA(MovJiCasosAtendidosEntity e);

    // Personas Beneficiadas
    MovJiPersonasBeneficiadasEntity toEntityPB(JusticiaItinerante.DetalleBeneficiada d);
    @InheritInverseConfiguration
    JusticiaItinerante.DetalleBeneficiada toDomainPB(MovJiPersonasBeneficiadasEntity e);

    // Tareas Realizadas
    MovJiTareasRealizadasEntity toEntityTR(JusticiaItinerante.DetalleTarea d);
    @InheritInverseConfiguration
    JusticiaItinerante.DetalleTarea toDomainTR(MovJiTareasRealizadasEntity e);

    // 2. ACTUALIZACIÓN PARCIAL (Dominio -> Entity Existente)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)

    // Listas ignoradas (se manejan manual)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "personasAtendidas", ignore = true)
    @Mapping(target = "casosAtendidos", ignore = true)
    @Mapping(target = "personasBeneficiadas", ignore = true)
    @Mapping(target = "tareasRealizadas", ignore = true)
    void updateEntityFromDomain(JusticiaItinerante domain, @MappingTarget MovJusticiaItineranteEntity entity);

    // 3. REQUEST -> DOMAIN (Con Defaults)
    @Mapping(target = "id", source = "id")
    // DEFAULTS: Limpiamos los nulos antes de llegar al UseCase
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
    // Ignoramos campos de auditoría (se llenan en UseCase/Entity)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    JusticiaItinerante toDomain(RegistrarFjiRequest request);

    // Mappings para items del request
    JusticiaItinerante.DetalleAtendida toDomainPA(RegistrarFjiRequest.DetallePARequest r);
    JusticiaItinerante.DetalleCaso toDomainPCA(RegistrarFjiRequest.DetallePCARequest r);
    JusticiaItinerante.DetalleBeneficiada toDomainPB(RegistrarFjiRequest.DetallePBRequest r);
    JusticiaItinerante.DetalleTarea toDomainTR(RegistrarFjiRequest.DetalleTRRequest r);

    // 4. DOMAIN -> RESPONSE (Dos versiones)

    // LISTADO (Ligero: ignora listas pesadas)
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", ignore = true)
    @Mapping(target = "personasAtendidas", ignore = true)
    @Mapping(target = "casosAtendidos", ignore = true)
    @Mapping(target = "personasBeneficiadas", ignore = true)
    @Mapping(target = "tareasRealizadas", ignore = true)
    @Mapping(target = "observaciones", ignore = true)
    JusticiaItineranteResponse toResponseListado(JusticiaItinerante dominio);

    // DETALLE
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    JusticiaItineranteResponse toResponseDetalle(JusticiaItinerante dominio);
}