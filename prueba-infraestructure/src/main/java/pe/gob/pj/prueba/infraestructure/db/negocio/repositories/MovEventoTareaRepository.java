package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovEventoTareaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.ids.MovEventoTareaId;

public interface MovEventoTareaRepository extends JpaRepository<MovEventoTareaEntity, MovEventoTareaId> {
    @Modifying
    @Query("DELETE FROM MovEventoTareaEntity e WHERE e.eventoId = :id")
    void deleteByEventoId(@Param("id") String id);
}