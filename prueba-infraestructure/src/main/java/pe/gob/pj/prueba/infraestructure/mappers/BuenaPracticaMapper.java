package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import pe.gob.pj.prueba.domain.model.negocio.BuenaPractica;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovBuenaPracticaEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarBuenaPracticaRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.BuenaPracticaResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BuenaPracticaMapper {
    BuenaPractica toDomain(RegistrarBuenaPracticaRequest request);
    MovBuenaPracticaEntity toEntity(BuenaPractica domain);
    BuenaPractica toDomain(MovBuenaPracticaEntity entity);
    @Mapping(target = "estado", constant = "REGISTRADO")
    BuenaPracticaResponse toResponse(BuenaPractica domain);
}