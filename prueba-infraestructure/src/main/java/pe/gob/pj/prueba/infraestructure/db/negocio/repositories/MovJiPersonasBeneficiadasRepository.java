package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.MovJiPersonasBeneficiadasEntity;

public interface MovJiPersonasBeneficiadasRepository extends JpaRepository<MovJiPersonasBeneficiadasEntity, Long> {
    @Modifying
    @Query("DELETE FROM MovJiPersonasBeneficiadasEntity e WHERE e.justiciaItineranteId = :id")
    void deleteByJusticiaItineranteId(@Param("id") String id);
}