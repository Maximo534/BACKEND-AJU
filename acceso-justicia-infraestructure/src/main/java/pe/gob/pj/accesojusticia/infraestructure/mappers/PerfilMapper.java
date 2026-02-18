package pe.gob.pj.accesojusticia.infraestructure.mappers;

import java.util.List;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import pe.gob.pj.accesojusticia.domain.model.negocio.Perfil;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MaePerfilEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PerfilMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    @Mapping(target = "rol", source = "rol")
    @Mapping(target = "activo", source = "activo")
    Perfil toDomain(MaePerfilEntity entity);

    List<Perfil> toDomainList(List<MaePerfilEntity> list);

    @InheritInverseConfiguration
    @Mapping(target = "perfilsOpcion", ignore = true)

    @Mapping(target = "FAud", ignore = true)
    @Mapping(target = "BAud", ignore = true)
    @Mapping(target = "CAudId", ignore = true)
    @Mapping(target = "CAudIdRed", ignore = true)
    @Mapping(target = "CAudPc", ignore = true)
    @Mapping(target = "CAudIp", ignore = true)
    @Mapping(target = "CAudMcAddr", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)

    MaePerfilEntity toEntity(Perfil domain);
}