package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.JusticiaItinerante;
import pe.gob.pj.prueba.domain.model.negocio.query.ListarJusticiaItineranteQuery;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarItineranteRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarFjiRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.JusticiaItineranteResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface JusticiaItineranteMapper {

    // =========================================================
    // 1. FILTROS (REST Request -> Domain Query)
    // =========================================================
    @Mapping(target = "search", source = "search")
    @Mapping(target = "distritoJudicialId", source = "distritoJudicialId")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    ListarJusticiaItineranteQuery toQuery(ListarItineranteRequest request);


    // =========================================================
    // 2. REGISTRAR (Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "fechaRegistroActividad", source = "request.fechaInicio")
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "usuario", source = "peticion.usuario")
    // -- Auditoría desde PeticionServicios --
//    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    // -- Valores por defecto (Negocio) --
    @Mapping(target = "publicoObjetivoDetalle", source = "request.publicoObjetivoDetalle", defaultValue = "NINGUNO")
    @Mapping(target = "tambo", source = "request.tambo", defaultValue = "NINGUNO")
    @Mapping(target = "lenguaNativa", source = "request.lenguaNativa", defaultValue = "NINGUNO")
    @Mapping(target = "codigoAdcPueblosIndigenas", source = "request.codigoAdcPueblosIndigenas", defaultValue = "00")
    @Mapping(target = "codigoSaeLenguaNativa", source = "request.codigoSaeLenguaNativa", defaultValue = "00")
    @Mapping(target = "numMesasInstaladas", source = "request.numMesasInstaladas", defaultValue = "0")
    @Mapping(target = "numServidores", source = "request.numServidores", defaultValue = "0")
    @Mapping(target = "numJueces", source = "request.numJueces", defaultValue = "0")

    // -- Listas Hijas --
    @Mapping(target = "personasAtendidas", source = "request.personasAtendidas")
    @Mapping(target = "casosAtendidos", source = "request.casosAtendidos")
    @Mapping(target = "personasBeneficiadas", source = "request.personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "request.tareasRealizadas")
    JusticiaItinerante toDomainRegistrar(RegistrarFjiRequest request, PeticionServicios peticion);


    // =========================================================
    // 3. ACTUALIZAR (ID + Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "request.codigo")

    // -- Auditoría de Modificación --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    // -- Listas Hijas --
    @Mapping(target = "personasAtendidas", source = "request.personasAtendidas")
    @Mapping(target = "casosAtendidos", source = "request.casosAtendidos")
    @Mapping(target = "personasBeneficiadas", source = "request.personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "request.tareasRealizadas")
    JusticiaItinerante toDomainActualizar(Long id, RegistrarFjiRequest request, PeticionServicios peticion);


    // =========================================================
    // 4. PERSISTENCIA (Domain <-> Entity)
    // =========================================================
    @Mapping(target = "personasAtendidas", source = "personasAtendidas")
    @Mapping(target = "casosAtendidos", source = "casosAtendidos")
    @Mapping(target = "personasBeneficiadas", source = "personasBeneficiadas")
    @Mapping(target = "tareasRealizadas", source = "tareasRealizadas")

    // -- Mapeo Auditoría a Columnas BD --
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIdRed", source = "red")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    @Mapping(target = "FAud", ignore = true)
    @Mapping(target = "BAud", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)
    MovJusticiaItineranteEntity toEntity(JusticiaItinerante domain);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "usuario", source = "CAudId")
    @Mapping(target = "red", source = "CAudIdRed")
    @Mapping(target = "nombrePc", source = "CAudPc")
    @Mapping(target = "numeroIp", source = "CAudIp")
    @Mapping(target = "direccionMac", source = "CAudMcAddr")
    JusticiaItinerante toDomain(MovJusticiaItineranteEntity entity);


    // =========================================================
    // 5. UPDATE PARCIAL (Entity Update)
    // =========================================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "personasAtendidas", ignore = true)
    @Mapping(target = "casosAtendidos", ignore = true)
    @Mapping(target = "personasBeneficiadas", ignore = true)
    @Mapping(target = "tareasRealizadas", ignore = true)

    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIdRed", source = "red")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDomain(JusticiaItinerante domain, @MappingTarget MovJusticiaItineranteEntity entity);


    // =========================================================
    // 6. MAPPINGS DE HIJOS
    // =========================================================
    JusticiaItinerante.DetalleAtendida toDomainPA(RegistrarFjiRequest.DetallePARequest r);
    MovJiPersonasAtendidasEntity toEntityPA(JusticiaItinerante.DetalleAtendida d);
    @InheritInverseConfiguration(name = "toEntityPA")
    JusticiaItinerante.DetalleAtendida toDomainPA(MovJiPersonasAtendidasEntity e);

    JusticiaItinerante.DetalleCaso toDomainPCA(RegistrarFjiRequest.DetallePCARequest r);
    @Mapping(target = "cantidadDemandas", source = "numDemandas")
    @Mapping(target = "cantidadAudiencias", source = "numAudiencias")
    @Mapping(target = "cantidadSentencias", source = "numSentencias")
    @Mapping(target = "cantidadProcesos", source = "numProcesos")
    @Mapping(target = "cantidadNotificaciones", source = "numNotificaciones")
    @Mapping(target = "cantidadOrientaciones", source = "numOrientaciones")
    MovJiCasosAtendidosEntity toEntityPCA(JusticiaItinerante.DetalleCaso d);
    @InheritInverseConfiguration(name = "toEntityPCA")
    JusticiaItinerante.DetalleCaso toDomainPCA(MovJiCasosAtendidosEntity e);

    JusticiaItinerante.DetalleBeneficiada toDomainPB(RegistrarFjiRequest.DetallePBRequest r);
    MovJiPersonasBeneficiadasEntity toEntityPB(JusticiaItinerante.DetalleBeneficiada d);
    @InheritInverseConfiguration(name = "toEntityPB")
    JusticiaItinerante.DetalleBeneficiada toDomainPB(MovJiPersonasBeneficiadasEntity e);

    JusticiaItinerante.DetalleTarea toDomainTR(RegistrarFjiRequest.DetalleTRRequest r);
    MovJiTareasRealizadasEntity toEntityTR(JusticiaItinerante.DetalleTarea d);
    @InheritInverseConfiguration(name = "toEntityTR")
    @Mapping(target = "descripcion", expression = "java(e.getTareaMaestra() != null ? e.getTareaMaestra().getDescripcion() : null)")
    JusticiaItinerante.DetalleTarea toDomainTR(MovJiTareasRealizadasEntity e);


    // =========================================================
    // 7. RESPUESTA (Domain -> Response)
    // =========================================================
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "distritoJudicialNombre", source = "distritoJudicialNombre")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    @Mapping(target = "publicoObjetivo", source = "publicoObjetivo")
    @Mapping(target = "FRegistro", source = "FRegistro")
    JusticiaItineranteResponse toResponseListado(JusticiaItinerante dominio);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "distritoJudicialNombre", source = "distritoJudicialNombre")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    @Mapping(target = "publicoObjetivo", source = "publicoObjetivo")
    @Mapping(target = "FRegistro", source = "FRegistro")
    @Mapping(target = "archivos", source = "archivosGuardados")
    JusticiaItineranteResponse toResponseDetalle(JusticiaItinerante dominio);
}