package pe.gob.pj.prueba.domain.model.client.consultasiga.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Setter
@Getter
public class GeneraTokenResponse {
  String token;
  String exps;
  String refs;
}
