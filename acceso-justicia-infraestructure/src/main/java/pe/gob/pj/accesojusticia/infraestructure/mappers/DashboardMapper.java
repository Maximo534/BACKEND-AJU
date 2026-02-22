package pe.gob.pj.accesojusticia.infraestructure.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import pe.gob.pj.accesojusticia.domain.model.negocio.Dashboard;
import pe.gob.pj.accesojusticia.infraestructure.rest.responses.DashboardResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DashboardMapper {

    DashboardResponse toResponse(Dashboard domain);

    DashboardResponse.DetalleGraficoResponse toResponseDetalle(Dashboard.DetalleGrafico domainDetalle);
}