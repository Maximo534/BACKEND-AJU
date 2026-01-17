package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeJuezPazEscolarEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.ListarJuezEscolarRequest; // Import necesario
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarJuezRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.JuezPazEscolarResponse;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JuezPazEscolarMapper {

    // --- MAPPINGS DE REGISTRO (Ya existían) ---
    @Mapping(target = "id", source = "id")
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "archivosGuardados", ignore = true)
    // Ignoramos campos de filtro/salida al registrar
    @Mapping(target = "search", ignore = true)
    @Mapping(target = "distritoJudicialId", ignore = true)
    @Mapping(target = "ugelId", ignore = true)
    @Mapping(target = "distritoJudicialNombre", ignore = true)
    @Mapping(target = "ugelNombre", ignore = true)
    @Mapping(target = "institucionEducativaNombre", ignore = true)
    JuezPazEscolar toDomain(RegistrarJuezRequest request);

    // ✅ NUEVO: Request Listado -> Dominio (Filtros)
    @Mapping(target = "search", source = "search")
    @Mapping(target = "distritoJudicialId", source = "distritoJudicialId")
    @Mapping(target = "ugelId", source = "ugelId")
    @Mapping(target = "institucionEducativaId", source = "institucionEducativaId")
    // Ignoramos el resto
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dni", ignore = true)
    // ... (MapStruct ignorará nulls por policy, o puedes ignorar explícitamente)
    JuezPazEscolar toDomain(ListarJuezEscolarRequest request);

    // --- MAPPINGS ENTITY (Sin cambios) ---
    @Mapping(target = "institucionEducativa", ignore = true)
    MaeJuezPazEscolarEntity toEntity(JuezPazEscolar domain);

    @InheritInverseConfiguration(name = "toEntity")
    JuezPazEscolar toDomain(MaeJuezPazEscolarEntity entity);

    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "institucionEducativa", ignore = true)
    void updateEntityFromDomain(JuezPazEscolar domain, @MappingTarget MaeJuezPazEscolarEntity entity);

    // --- RESPONSE (Sin cambios) ---
    @Mapping(target = "nombreCompleto", expression = "java(domain.getNombres() + ' ' + domain.getApePaterno() + ' ' + domain.getApeMaterno())")
    @Mapping(target = "archivos", source = "archivosGuardados")
    JuezPazEscolarResponse toResponse(JuezPazEscolar domain);
}