package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
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

    // --- SECCIÓN JUECES ---
    JuezPazEscolar toDomain(RegistrarJuezRequest request);

    @Mapping(target = "institucionEducativa", ignore = true)
    MaeJuezPazEscolarEntity toEntity(JuezPazEscolar domain);

    @Mapping(target = "nombreInstitucion", source = "institucionEducativa.nombre")
    JuezPazEscolar toDomain(MaeJuezPazEscolarEntity entity);

    @Mapping(target = "nombreCompleto", expression = "java(domain.getNombres() + ' ' + domain.getApePaterno() + ' ' + domain.getApeMaterno())")
    @Mapping(target = "nombreColegio", source = "nombreInstitucion")
    JuezPazEscolarResponse toResponse(JuezPazEscolar domain);

    List<JuezPazEscolarResponse> toResponseList(List<JuezPazEscolar> list);


    // --- SECCIÓN CASOS ---

    // Request -> Domain
    @Mapping(target = "id", source = "id")
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", source = "fechaRegistro")
    JpeCasoAtendido toDomain(RegistrarCasoRequest request);

    // Domain -> Entity
    @Mapping(target = "juezEscolar", ignore = true)
    MovJpeCasoAtendidoEntity toEntity(JpeCasoAtendido domain);

    // Entity -> Domain
    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "juezEscolarId", source = "juezEscolar.id")
    JpeCasoAtendido toDomain(MovJpeCasoAtendidoEntity entity);

    // ✅ ACTUALIZACIÓN (Corrección: Quitamos 'activo')
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    // @Mapping(target = "activo", ignore = true) <--- ELIMINADO PORQUE EL CAMPO NO EXISTE EN ENTIDAD
    @Mapping(target = "juezEscolar", ignore = true)
    void updateEntityFromDomain(JpeCasoAtendido domain, @MappingTarget MovJpeCasoAtendidoEntity entity);

    // Domain -> Response
    @Mapping(target = "estado", constant = "REGISTRADO")
    @Mapping(target = "archivos", source = "archivosGuardados")
    JpeCasoAtendidoResponse toResponse(JpeCasoAtendido dominio);

    List<JpeCasoAtendidoResponse> toResponseListCasos(List<JpeCasoAtendido> list);
}