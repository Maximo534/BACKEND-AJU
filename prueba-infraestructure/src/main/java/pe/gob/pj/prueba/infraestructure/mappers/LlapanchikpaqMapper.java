package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.LlapanchikpaqJusticia;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.*;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarLlapanchikpaqRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.LlapanchikpaqResponse;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LlapanchikpaqMapper {

    // Request -> Domain
    @Mapping(target = "id", source = "id")
    @Mapping(target = "archivosGuardados", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    LlapanchikpaqJusticia toDomain(RegistrarLlapanchikpaqRequest request);

    // Dominio -> Entity
    MovLlapanchikpaqJusticia toEntity(LlapanchikpaqJusticia domain);

    // Entity -> Dominio
    @InheritInverseConfiguration(name = "toEntity")
    LlapanchikpaqJusticia toDomain(MovLlapanchikpaqJusticia entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioRegistro", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "beneficiadas", ignore = true)
    @Mapping(target = "atendidas", ignore = true)
    @Mapping(target = "casos", ignore = true)
    @Mapping(target = "tareas", ignore = true)
    void updateEntityFromDomain(LlapanchikpaqJusticia domain, @MappingTarget MovLlapanchikpaqJusticia entity);

    // Helpers
    MovLljPersonasBeneficiadasEntity mapBeneficiada(LlapanchikpaqJusticia.DetalleBeneficiada d);
    MovLljPersonasAtendidasEntity mapAtendida(LlapanchikpaqJusticia.DetalleAtendida d);
    MovLljCasosAtendidosEntity mapCaso(LlapanchikpaqJusticia.DetalleCaso d);
    MovLljTareaRealizadasEntity mapTarea(LlapanchikpaqJusticia.DetalleTarea d);

    LlapanchikpaqJusticia.DetalleBeneficiada mapBeneficiadaToDomain(MovLljPersonasBeneficiadasEntity e);
    LlapanchikpaqJusticia.DetalleAtendida mapAtendidaToDomain(MovLljPersonasAtendidasEntity e);
    LlapanchikpaqJusticia.DetalleCaso mapCasoToDomain(MovLljCasosAtendidosEntity e);
    LlapanchikpaqJusticia.DetalleTarea mapTareaToDomain(MovLljTareaRealizadasEntity e);

    // Response
    @Mapping(target = "estado", source = "activo")
    @Mapping(target = "archivos", source = "archivosGuardados")
    LlapanchikpaqResponse toResponse(LlapanchikpaqJusticia dominio);
}