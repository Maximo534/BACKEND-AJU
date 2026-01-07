package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.domain.model.negocio.JuezPazEscolar;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MaeJuezPazEscolarEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJpeCasoAtendidoEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarCasoRequest;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarJuezRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.JpeCasoAtendidoResponse;
import pe.gob.pj.prueba.infraestructure.rest.responses.JuezPazEscolarResponse;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JuezPazEscolarMapper {

    // ==========================================
    // 1. MAPPERS PARA JUEZ ESCOLAR (ALUMNO)
    // ==========================================

    // Request -> Domain
    JuezPazEscolar toDomain(RegistrarJuezRequest request);

    // Domain -> Entity
    @Mapping(target = "institucionEducativa", ignore = true)
    MaeJuezPazEscolarEntity toEntity(JuezPazEscolar domain);

    // Entity -> Domain
    @Mapping(target = "nombreInstitucion", source = "institucionEducativa.nombre")
    JuezPazEscolar toDomain(MaeJuezPazEscolarEntity entity);

    // Domain -> Response
    @Mapping(target = "nombreCompleto", expression = "java(domain.getNombres() + ' ' + domain.getApePaterno() + ' ' + domain.getApeMaterno())")
    @Mapping(target = "nombreColegio", source = "nombreInstitucion")
    JuezPazEscolarResponse toResponse(JuezPazEscolar domain);

    List<JuezPazEscolarResponse> toResponseList(List<JuezPazEscolar> list);


    // ==========================================
    // 2. MAPPERS PARA CASOS (INCIDENTES)
    // ==========================================

    // Request -> Domain
    @Mapping(target = "id", source = "id") // Asegura que el ID del update pase
    JpeCasoAtendido toDomain(RegistrarCasoRequest request);

    // Domain -> Entity (Para guardar en BD)
    MovJpeCasoAtendidoEntity toEntity(JpeCasoAtendido domain);

    // Entity -> Domain (Al leer de BD por ID)
    JpeCasoAtendido toDomain(MovJpeCasoAtendidoEntity entity);

    // Domain -> Response (Para devolver al Front)
    // MapStruct mapea automáticamente:
    // domain.distritoJudicialNombre -> response.distritoJudicialNombre
    // domain.ugelNombre -> response.ugelNombre
    // domain.institucionNombre -> response.institucionNombre
    // domain.archivosGuardados -> response.archivosGuardados
    @Mapping(target = "estado", constant = "REGISTRADO")
    JpeCasoAtendidoResponse toResponse(JpeCasoAtendido domain);

    // ✅ MÉTODO NUEVO OBLIGATORIO (Para el endpoint listarCasos)
    List<JpeCasoAtendidoResponse> toResponseListCasos(List<JpeCasoAtendido> list);
}