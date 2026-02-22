package pe.gob.pj.accesojusticia.infraestructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import pe.gob.pj.accesojusticia.domain.model.negocio.EstadisticasData;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.EstadisticasResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EstadisticasMapper {

    EstadisticasResponse toResponse(EstadisticasData domain);
}