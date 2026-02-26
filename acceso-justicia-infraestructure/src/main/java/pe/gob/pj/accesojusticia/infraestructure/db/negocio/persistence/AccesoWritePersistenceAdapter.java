package pe.gob.pj.accesojusticia.infraestructure.db.negocio.persistence;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.accesojusticia.domain.model.auditoriageneral.PeticionServicios;
import pe.gob.pj.accesojusticia.domain.port.persistence.negocio.AccesoPersistenceWritePort;
import pe.gob.pj.accesojusticia.infraestructure.common.enums.OperacionBaseDatos;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovSesionEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.MovSesionRepository;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AccesoWritePersistenceAdapter implements AccesoPersistenceWritePort {

    MovSesionRepository movSesionRepository;

    @Override
    public Long registrarInicioSesion(String cuo, Integer idUsuario, PeticionServicios peticion) {
        MovSesionEntity entity = new MovSesionEntity();
        entity.setIdUsuario(idUsuario);
        entity.setFechaIngreso(LocalDateTime.now());

        // Seteando campos de auditoría provenientes de la petición
        entity.setCAudId(peticion.getUsuarioAuth());
        entity.setCAudIp(peticion.getIp());
        entity.setCAudPc(peticion.getNombrePc());
        entity.setCAudMcAddr(peticion.getCodigoMac());
        entity.setCAudIdRed(peticion.getRed());

        movSesionRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void registrarCierreSesion(String cuo, Long idSesion, PeticionServicios peticion) {
        movSesionRepository.findById(idSesion).ifPresent(entity -> {
            entity.setFechaSalida(LocalDateTime.now());
            entity.setBAud(OperacionBaseDatos.ACTUALIZAR.getNombre());
            entity.setFAud(LocalDateTime.now());
            entity.setCAudId(peticion.getUsuarioAuth());

            movSesionRepository.save(entity);
        });
    }
}