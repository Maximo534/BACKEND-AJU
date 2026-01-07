package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovOrientadoraJudicialEntity;

@Repository
public interface MovOrientadoraJudicialRepository extends JpaRepository<MovOrientadoraJudicialEntity, String> {
    @Query("SELECT MAX(e.id) FROM MovOrientadoraJudicialEntity e WHERE e.id LIKE '%-OJ'")
    String obtenerUltimoId();
}