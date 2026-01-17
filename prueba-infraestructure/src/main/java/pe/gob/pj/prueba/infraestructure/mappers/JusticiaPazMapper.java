package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.JpeCasoAtendido;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJpeCasoAtendidoEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarCasoRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.JpeCasoAtendidoResponse;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JusticiaPazMapper {

    // 1. Request -> Domain
    // Mapeo directo 1 a 1. Si un campo viene null del request (y pasó validación), llegará null al dominio.
    @Mapping(target = "id", source = "id")
    @Mapping(target = "fechaRegistro", source = "fechaRegistro")
    // Ignoramos campos que no vienen del formulario
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "distritoJudicialNombre", ignore = true)
    @Mapping(target = "ugelId", ignore = true)
    @Mapping(target = "ugelNombre", ignore = true)
    @Mapping(target = "institucionEducativaId", ignore = true)
    @Mapping(target = "institucionNombre", ignore = true)
    @Mapping(target = "juezEscolarNombre", ignore = true)
    @Mapping(target = "juezGradoSeccion", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "search", ignore = true)
    JpeCasoAtendido toDomain(RegistrarCasoRequest request);

    // 2. Domain -> Entity
    @Mapping(target = "juezEscolar", ignore = true) // Se setea manualmente en el Adapter
    MovJpeCasoAtendidoEntity toEntity(JpeCasoAtendido domain);

    // 3. Entity -> Domain
    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "juezEscolarId", source = "juezEscolar.id")
    JpeCasoAtendido toDomain(MovJpeCasoAtendidoEntity entity);

    // 4. Update
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "juezEscolar", ignore = true) // Se maneja en el Adapter si cambia
    void updateEntityFromDomain(JpeCasoAtendido domain, @MappingTarget MovJpeCasoAtendidoEntity entity);

    // 5. Response
    @Mapping(target = "estado", constant = "REGISTRADO") // Opcional, si quieres mostrar algo fijo
    @Mapping(target = "archivos", source = "archivosGuardados")
    JpeCasoAtendidoResponse toResponse(JpeCasoAtendido dominio);

    List<JpeCasoAtendidoResponse> toResponseListCasos(List<JpeCasoAtendido> list);
}