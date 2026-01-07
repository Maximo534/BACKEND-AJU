package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovLlapanchikpaqJusticia;

@Repository
public interface MovLlapanchikpaqJusticiaRepository extends JpaRepository<MovLlapanchikpaqJusticia, String> {

    // Busca el último ID para el correlativo (Orden Descendente)
    @Query("SELECT MAX(e.id) FROM MovLlapanchikpaqJusticia e WHERE e.id LIKE '%-LL'")
    String obtenerUltimoId();
}