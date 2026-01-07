package pe.gob.pj.prueba.infraestructure.db.negocio.repositories;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import pe.gob.pj.prueba.domain.model.negocio.Persona;
import pe.gob.pj.prueba.domain.model.negocio.query.ConsultarPersonaQuery;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.QMaeTipoDocumentoPersonaEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.QMovPersonaEntity;

@Repository
public class MovPersonaDslRepositoryImpl implements MovPersonaDslRepository {

  @Qualifier("negocioQDSL")
  private final JPAQueryFactory queryFactory;

  public MovPersonaDslRepositoryImpl(@Qualifier("negocioQDSL")JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public List<Persona> buscarPersona(ConsultarPersonaQuery query) {

    var persona = QMovPersonaEntity.movPersonaEntity;
    var tipoDocumento = QMaeTipoDocumentoPersonaEntity.maeTipoDocumentoPersonaEntity;

    var filtro = new BooleanBuilder();

    if (Objects.nonNull(query.documentoIdentidad())) {
      filtro.and(persona.numeroDocumento.eq(query.documentoIdentidad()));
    }
    
    return queryFactory
        .select(Projections.constructor(Persona.class, persona.id, persona.numeroDocumento,
            persona.fechaNacimiento.stringValue(), persona.primerApellido, persona.segundoApellido,
            persona.nombres, persona.sexo, persona.correo, persona.telefono, persona.activo,
            tipoDocumento.codigo, tipoDocumento.abreviatura))
        .from(persona).innerJoin(persona.tipoDocumento, tipoDocumento).where(filtro).fetch();
  }

}
