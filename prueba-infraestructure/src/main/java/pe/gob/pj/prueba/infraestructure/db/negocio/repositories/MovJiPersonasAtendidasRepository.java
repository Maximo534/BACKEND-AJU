package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJiPersonasAtendidasEntity;

public interface MovJiPersonasAtendidasRepository extends JpaRepository<MovJiPersonasAtendidasEntity, Long> {
    @Modifying
    @Query("DELETE FROM MovJiPersonasAtendidasEntity e WHERE e.justiciaItineranteId = :id")
    void deleteByJusticiaItineranteId(@Param("id") String id);
}