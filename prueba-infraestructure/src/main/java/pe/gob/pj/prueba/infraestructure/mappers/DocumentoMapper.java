package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.DocumentoEntity;
import pe.gob.pj.prueba.infraestructure.rest.responses.DocumentoResponse;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentoMapper {

    DocumentoResponse toResponse(Documento domain);
    List<DocumentoResponse> toResponseList(List<Documento> domainList);

    @Mapping(target = "ruta", source = "rutaArchivo")
    DocumentoEntity toEntity(Documento domain);

    @Mapping(target = "rutaArchivo", source = "ruta")
    Documento toDomain(DocumentoEntity entity);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDomain(Documento domain, @MappingTarget DocumentoEntity entity);
}