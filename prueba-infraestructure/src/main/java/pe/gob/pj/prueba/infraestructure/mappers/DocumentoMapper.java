package pe.gob.pj.prueba.infraestructure.mappers;

import org.mapstruct.*;
import pe.gob.pj.prueba.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.prueba.domain.model.negocio.Documento;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.DocumentoEntity;
import pe.gob.pj.prueba.infraestructure.rest.requests.RegistrarDocumentoRequest;
import pe.gob.pj.prueba.infraestructure.rest.responses.DocumentoResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface DocumentoMapper {

    // =========================================================
    // 1. REQUEST -> DOMAIN (Registro y Actualización)
    // =========================================================
    @Mapping(target = "id", ignore = true)

    @Mapping(target = "nombre", ignore = true)
    @Mapping(target = "formato", ignore = true)
    @Mapping(target = "ruta", ignore = true)

    @Mapping(target = "tipo", source = "request.tipo")
    @Mapping(target = "periodo", source = "request.periodo")
    @Mapping(target = "categoriaDocumentoId", source = "request.categoriaDocumentoId")

    @Mapping(target = "activo", constant = "1")

    // Auditoría
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")
    Documento toDomainRegistrar(RegistrarDocumentoRequest request, PeticionServicios peticion);

    @Mapping(target = "id", source = "id")

    @Mapping(target = "nombre", ignore = true)
    @Mapping(target = "formato", ignore = true)
    @Mapping(target = "ruta", ignore = true)

    @Mapping(target = "tipo", source = "request.tipo")
    @Mapping(target = "periodo", source = "request.periodo")
    @Mapping(target = "categoriaDocumentoId", source = "request.categoriaDocumentoId")

    // Auditoría
    @Mapping(target = "usuario", source = "peticion.usuarioAuth")
    @Mapping(target = "nombrePc", source = "peticion.nombrePc")
    @Mapping(target = "direccionMac", source = "peticion.codigoMac")
    @Mapping(target = "numeroIp", source = "peticion.ip")
    @Mapping(target = "red", source = "peticion.red")
    Documento toDomainActualizar(Long id, RegistrarDocumentoRequest request, PeticionServicios peticion);

    // =========================================================
    // 2. PERSISTENCIA (Domain <-> Entity)
    // =========================================================
    @Mapping(target = "categoria", ignore = true)

    // -- Mapeo Auditoría a Columnas BD --
    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")

    // Ignorar campos automáticos de BD
    @Mapping(target = "FAud", ignore = true)
    @Mapping(target = "BAud", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)
    DocumentoEntity toEntity(Documento domain);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "categoriaNombre", source = "categoria.descripcion")
    // Mapeo inverso de auditoría
    @Mapping(target = "usuario", source = "CAudId")
    @Mapping(target = "nombrePc", source = "CAudPc")
    @Mapping(target = "numeroIp", source = "CAudIp")
    @Mapping(target = "direccionMac", source = "CAudMcAddr")
    Documento toDomain(DocumentoEntity entity);

    // =========================================================
    // 3. UPDATE ENTITY
    // =========================================================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "FRegistro", ignore = true)

    @Mapping(target = "CAudId", source = "usuario")
    @Mapping(target = "CAudIp", source = "numeroIp")
    @Mapping(target = "CAudPc", source = "nombrePc")
    @Mapping(target = "CAudMcAddr", source = "direccionMac")
    void updateEntityFromDomain(Documento domain, @MappingTarget DocumentoEntity entity);

    // =========================================================
    // 4. RESPONSE (Domain -> Response)
    // =========================================================
    DocumentoResponse toResponse(Documento domain);
}