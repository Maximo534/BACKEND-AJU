package pe.gob.pj.accesojusticia.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarLlapanchikpaqQuery;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.*;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.*;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.ListarLlapanchikpaqRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.requests.RegistrarLlapanchikpaqRequest;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.LlapanchikpaqResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface LlapanchikpaqMapper {

    // =========================================================
    // 1. FILTROS (REST Request -> Domain Query)
    // =========================================================
    @Mapping(target = "search", source = "search")
    @Mapping(target = "distritoJudicialId", source = "distritoJudicialId")
    @Mapping(target = "fechaInicio", source = "fechaInicio")
    @Mapping(target = "fechaFin", source = "fechaFin")
    ListarLlapanchikpaqQuery toQuery(ListarLlapanchikpaqRequest request);

    // =========================================================
    // 2. REGISTRAR (Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)

    // -- Auditoría desde PeticionServicios --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    // -- Listas Hijas (Nombres diferentes Request vs Domain) --
    @Mapping(target = "personasBeneficiadas", source = "request.beneficiadas")
    @Mapping(target = "personasAtendidas", source = "request.atendidas")
    @Mapping(target = "casosAtendidos", source = "request.casos")
    @Mapping(target = "tareasRealizadas", source = "request.tareas")
    LlapanchikpaqJusticia toDomainRegistrar(RegistrarLlapanchikpaqRequest request, PeticionServicios peticion);

    // =========================================================
    // 3. ACTUALIZAR (ID + Request + Auditoría -> Domain)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistroId", ignore = true)
    @Mapping(target = "activo", ignore = true)

    // -- Auditoría de Modificación --
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")

    // -- Listas Hijas --
    @Mapping(target = "personasBeneficiadas", source = "request.beneficiadas")
    @Mapping(target = "personasAtendidas", source = "request.atendidas")
    @Mapping(target = "casosAtendidos", source = "request.casos")
    @Mapping(target = "tareasRealizadas", source = "request.tareas")
    LlapanchikpaqJusticia toDomainActualizar(Long id, RegistrarLlapanchikpaqRequest request, PeticionServicios peticion);

    // =========================================================
    // 4. PERSISTENCIA (Domain <-> Entity)
    // =========================================================
    @Mapping(target = "beneficiadas", source = "personasBeneficiadas")
    @Mapping(target = "atendidas", source = "personasAtendidas")
    @Mapping(target = "casos", source = "casosAtendidos")
    @Mapping(target = "tareas", source = "tareasRealizadas")

    // -- Mapeo Auditoría a Columnas BD --
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    @Mapping(target = "FAud", ignore = true)
    @Mapping(target = "BAud", ignore = true)
    @Mapping(target = "fechaRegistroActividad", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)
    MovLlapanchikpaqJusticiaEntity toEntity(LlapanchikpaqJusticia domain);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "usuario", source = "CAudId")
    @Mapping(target = "nombrePc", source = "CAudPc")
    @Mapping(target = "numeroIp", source = "CAudIp")
    @Mapping(target = "direccionMac", source = "CAudMcAddr")
    LlapanchikpaqJusticia toDomain(MovLlapanchikpaqJusticiaEntity entity);

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

    @Mapping(target = "beneficiadas", ignore = true)
    @Mapping(target = "atendidas", ignore = true)
    @Mapping(target = "casos", ignore = true)
    @Mapping(target = "tareas", ignore = true)

    // Auditoría Update
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    void updateEntityFromDomain(LlapanchikpaqJusticia domain, @MappingTarget MovLlapanchikpaqJusticiaEntity entity);

    // =========================================================
    // 6. MAPPINGS DE HIJOS (Listas)
    // =========================================================

    // --- Beneficiadas ---
    LlapanchikpaqJusticia.DetalleBeneficiada toDomainPB(RegistrarLlapanchikpaqRequest.BeneficiadaRequest r);

    @Mapping(target = "lljId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovLljPersonasBeneficiadasEntity toEntityPB(LlapanchikpaqJusticia.DetalleBeneficiada d);

    @InheritInverseConfiguration(name = "toEntityPB")
    LlapanchikpaqJusticia.DetalleBeneficiada toDomainPB(MovLljPersonasBeneficiadasEntity e);

    // --- Atendidas ---
    LlapanchikpaqJusticia.DetalleAtendida toDomainPA(RegistrarLlapanchikpaqRequest.AtendidaRequest r);

    @Mapping(target = "lljId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovLljPersonasAtendidasEntity toEntityPA(LlapanchikpaqJusticia.DetalleAtendida d);

    @InheritInverseConfiguration(name = "toEntityPA")
    LlapanchikpaqJusticia.DetalleAtendida toDomainPA(MovLljPersonasAtendidasEntity e);

    // --- Casos ---
    LlapanchikpaqJusticia.DetalleCaso toDomainCA(RegistrarLlapanchikpaqRequest.CasoRequest r);

    @Mapping(target = "lljId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovLljCasosAtendidosEntity toEntityCA(LlapanchikpaqJusticia.DetalleCaso d);

    @InheritInverseConfiguration(name = "toEntityCA")
    LlapanchikpaqJusticia.DetalleCaso toDomainCA(MovLljCasosAtendidosEntity e);

    // --- Tareas ---
    LlapanchikpaqJusticia.DetalleTarea toDomainTR(RegistrarLlapanchikpaqRequest.TareaRequest r);

    @Mapping(target = "lljId", ignore = true)
    @Mapping(target = "activo", constant = "1")
    MovLljTareaRealizadasEntity toEntityTR(LlapanchikpaqJusticia.DetalleTarea d);

    @InheritInverseConfiguration(name = "toEntityTR")
    @Mapping(target = "descripcion", expression = "java(e.getTareaMaestra() != null ? e.getTareaMaestra().getDescripcion() : null)")
    LlapanchikpaqJusticia.DetalleTarea toDomainTR(MovLljTareaRealizadasEntity e);

    // =========================================================
    // 7. RESPUESTA (Domain -> Response)
    // =========================================================
    @Mapping(target = "id", source = "id")
    @Mapping(target = "codigo", source = "codigo")
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    // Mapeo de listas (Dominio -> Response)
    @Mapping(target = "beneficiadas", source = "personasBeneficiadas")
    @Mapping(target = "atendidas", source = "personasAtendidas")
    @Mapping(target = "casos", source = "casosAtendidos")
    @Mapping(target = "tareas", source = "tareasRealizadas")
    LlapanchikpaqResponse toResponse(LlapanchikpaqJusticia domain);
}