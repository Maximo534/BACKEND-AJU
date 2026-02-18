package pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovUsuarioEntity;

/**
 *
 * Esta interfaz extiende {@link JpaRepository} y proporciona operaciones CRUD
 * básicas para la entidad mencionada.
 *
 * @author oruizb
 * @version 1.0,07/02/2022
 */
public interface MovUsuarioRepository extends JpaRepository<MovUsuarioEntity, Integer> {

  Optional<MovUsuarioEntity> findByActivoAndUsuario(String activo, String usuario);
  Optional<MovUsuarioEntity> findByActivoAndUsuarioIgnoreCase(String activo, String usuario);
  @Query(value = "SELECT * FROM acjust.MOV_USUARIO u WHERE " +
          "(:id IS NULL OR u.N_USUARIO_ID = :id) AND " +
          "(:usuario IS NULL OR UPPER(u.X_USUARIO) LIKE UPPER(CONCAT('%', CAST(:usuario AS TEXT), '%'))) AND " +
          "(:nombreCompleto IS NULL OR UPPER(u.X_NOMBRE_COMPLETO) LIKE UPPER(CONCAT('%', CAST(:nombreCompleto AS TEXT), '%'))) AND " +
          "(:activo IS NULL OR u.L_ACTIVO = :activo)",

          countQuery = "SELECT count(*) FROM acjust.MOV_USUARIO u WHERE " +
                  "(:id IS NULL OR u.N_USUARIO_ID = :id) AND " +
                  "(:usuario IS NULL OR UPPER(u.X_USUARIO) LIKE UPPER(CONCAT('%', CAST(:usuario AS TEXT), '%'))) AND " +
                  "(:nombreCompleto IS NULL OR UPPER(u.X_NOMBRE_COMPLETO) LIKE UPPER(CONCAT('%', CAST(:nombreCompleto AS TEXT), '%'))) AND " +
                  "(:activo IS NULL OR u.L_ACTIVO = :activo)",

          nativeQuery = true)
  Page<MovUsuarioEntity> listar(
          @Param("id") Integer id,
          @Param("usuario") String usuario,
          @Param("nombreCompleto") String nombreCompleto,
          @Param("activo") String activo,
          Pageable pageable);

  boolean existsByUsuario(String nombreUsuario);

}