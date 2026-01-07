package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import pe.gob.pj.prueba.infraestructure.rest.responses.DocumentoResponse;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentoMapper {

    DocumentoResponse toResponse(Documento domain);
    List<DocumentoResponse> toResponseList(List<Documento> domainList);
}