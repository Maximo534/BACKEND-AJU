package pe.gob.pj.prueba.domain.model.seguridad;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


@NoArgsConstructor
@Data
@Entity
@Table(name = "roles")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleSecurity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Integer id;

  @Column(nullable = false, unique = true)
  String name;

  String codigo;

  @ManyToMany(mappedBy = "roles")
  List<UserSecurity> users;

  public RoleSecurity(Integer id, String codigo, String name) {
    super();
    this.id = id;
    this.codigo = codigo;
    this.name = name;
  }

}
