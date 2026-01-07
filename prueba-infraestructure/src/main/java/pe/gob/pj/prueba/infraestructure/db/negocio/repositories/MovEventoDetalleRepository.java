package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovEventoDetalleEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovEventoDetalleId;

public interface MovEventoDetalleRepository extends JpaRepository<MovEventoDetalleEntity, MovEventoDetalleId> {
    @Modifying
    @Query("DELETE FROM MovEventoDetalleEntity e WHERE e.eventoId = :id")
    void deleteByEventoId(@Param("id") String id);
}