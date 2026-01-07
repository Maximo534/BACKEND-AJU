package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import pe.gob.pj.prueba.domain.model.negocio.Dashboard;
import pe.gob.pj.prueba.infraestructure.rest.responses.DashboardResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DashboardMapper {

    DashboardResponse toResponse(Dashboard domain);

    DashboardResponse.GraficoBarrasResponse toResponseGrafico(Dashboard.GraficoBarras domainGrafico);
}