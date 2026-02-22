package pe.gob.pj.accesojusticia.infraestructure.db.negocio.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import pe.gob.pj.accesojusticia.domain.exceptions.negocio.MaestroNoEncontradoException; // IMPORTANTE
import pe.gob.pj.accesojusticia.domain.model.common.Pagina;
import pe.gob.pj.accesojusticia.domain.model.negocio.PerfilUsuario;
import pe.gob.pj.accesojusticia.domain.model.negocio.Usuario;
import pe.gob.pj.accesojusticia.domain.model.negocio.query.ListarUsuarioQuery;
import pe.gob.pj.accesojusticia.domain.port.persistence.negocio.UsuarioPersistencePort;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MaePerfilEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovProgramacionEjeEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovUsuarioEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MovUsuarioPerfilEntity;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.*;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.*;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters.MaeEjeRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters.MaeInstanciaRepository;
import pe.gob.pj.accesojusticia.infraestructure.db.negocio.repositories.masters.MaeSedeRepository;
import pe.gob.pj.accesojusticia.infraestructure.mappers.UsuarioMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UsuarioPersistenceAdapter implements UsuarioPersistencePort {


    MovUsuarioRepository repository;
    UsuarioMapper mapper;
    MovUsuarioPerfilRepository usuarioPerfilRepository;
    MovProgramacionEjeRepository programacionEjeRepository;
    MaeRolJerarquiaRepository jerarquiaRepository;
    MaeDistritoJudicialRepository repoDistrito;
    MaeInstanciaRepository repoInstancia;
    MaeEjeRepository repoEje;
    MaePerfilRepository repoPerfil;
    MaeSedeRepository repoSede;
    @Override
    public Usuario buscarPorLoginConDetalle(String cuo, String login) {
        if (login == null) return null;

        MovUsuarioEntity entity = repository.findByActivoAndUsuarioIgnoreCase("1", login.trim())
                .orElse(null);

        if (entity == null) return null;

        Usuario dominio = mapper.toUsuario(entity);
        List<MovUsuarioPerfilEntity> listaPerfilesBd = usuarioPerfilRepository.findByUsuarioId(entity.getId());

        List<PerfilUsuario> perfilesDominio = listaPerfilesBd.stream()
                .filter(p -> "1".equals(p.getActivo()))
                .map(p -> {
                    PerfilUsuario perfilPlano = new PerfilUsuario();
                    perfilPlano.setId(p.getId());
                    perfilPlano.setIdPerfil(p.getPerfil().getId());
                    perfilPlano.setNombre(p.getPerfil().getNombre());
                    perfilPlano.setRol(p.getPerfil().getRol());
                    return perfilPlano;
                })
                .collect(Collectors.toList());

        // Inyectamos la lista llena a tu objeto de dominio
        dominio.setPerfiles(perfilesDominio);
        // -------------------------------------
        var perfilOpt = listaPerfilesBd.stream()
                .filter(p -> "1".equals(p.getActivo()))
                .findFirst();

        String nombrePerfil = null;
        if (perfilOpt.isPresent()) {
            nombrePerfil = perfilOpt.get().getPerfil().getNombre();
        }

        String cargoFinal = entity.getCargo();
        if (cargoFinal == null) cargoFinal = "-";

        if (nombrePerfil != null && nombrePerfil.toUpperCase().contains("JUEZ")) {
            String sigla = entity.getSigla();
            if (sigla != null && !sigla.isBlank() && !"-".equals(sigla)) {
                cargoFinal = cargoFinal.trim() + " (" + sigla.trim() + ")";
            }
        }
        dominio.setCargo(cargoFinal);

        // A. Intentar buscar en Programación Eje (Tabla MOV_PROGRAMACION_EJE)
        var programacionOpt = programacionEjeRepository.findFirstByIdUsuarioAndActivo(entity.getId(), "1");

        if (programacionOpt.isPresent()) {
            var prog = programacionOpt.get();

            if (prog.getIdDistritoJudicial() != null) {
                dominio.setIdDistritoJudicial(prog.getIdDistritoJudicial());
            }
            if (prog.getIdEje() != null) {
                dominio.setIdEje(prog.getIdEje());
            }
        }

        // B. Obtener el nombre del Distrito Judicial
        if (dominio.getIdDistritoJudicial() != null) {
            repoDistrito.findById(dominio.getIdDistritoJudicial().longValue())
                    .ifPresent(dj -> dominio.setNombreDistritoJudicial(dj.getNombre()));
        }

        // C. Obtener el nombre del Eje o Instancia (Y la sede desde la instancia si aplica)
        if (dominio.getIdEje() != null) {
            repoEje.findById(dominio.getIdEje().longValue())
                    .ifPresent(eje -> dominio.setNombreInstancia(eje.getDescripcion()));
        } else if (dominio.getIdInstancia() != null) {
            repoInstancia.findById(dominio.getIdInstancia().longValue())
                    .ifPresent(inst -> {
                        dominio.setNombreInstancia(inst.getDescripcion());

                        // Si la instancia tiene Sede, la asignamos aquí
                        if (inst.getSedeId() != null) {
                            repoSede.findById(inst.getSedeId().longValue())
                                    .ifPresent(sede -> dominio.setNombreSede(sede.getDescripcion()));
                        }
                    });
        }

        if (dominio.getNombreSede() == null && dominio.getIdDistritoJudicial() != null) {
            List<pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.masters.MaeSedeEntity> sedesDistrito =
                    repoSede.findByDistritoJudicialIdAndActivo(dominio.getIdDistritoJudicial().longValue(), "1");

            if (!sedesDistrito.isEmpty()) {
                dominio.setNombreSede(sedesDistrito.get(0).getDescripcion());
            } else {
                dominio.setNombreSede("SEDE CENTRAL");
            }
        }

        return dominio;
    }

    @Override
    public Pagina<Usuario> listar(String cuo, ListarUsuarioQuery query, int pagina, int tamanio) {

        Pageable pageable = PageRequest.of(pagina - 1, tamanio, Sort.by("n_usuario_id").descending());

        if (query == null) query = ListarUsuarioQuery.builder().build();

        var pageResult = repository.listar(
                query.idUsuario(),
                query.usuario(),
                query.nombreCompleto(),
                query.activo(),
                query.idUsuarioSesion(),
                query.rolUsuarioSesion(),
                pageable
        );

        var contenido = pageResult.getContent().stream()
                .map(entity -> {
                    Usuario dominio = mapper.toUsuario(entity);

                    var programacionOpt = programacionEjeRepository.findFirstByIdUsuarioAndActivo(entity.getId(), "1");

                    if (programacionOpt.isPresent()) {
                        var prog = programacionOpt.get();
                        if (prog.getIdDistritoJudicial() != null) {
                            dominio.setIdDistritoJudicial(prog.getIdDistritoJudicial());
                        }
                        if (prog.getIdEje() != null) {
                            dominio.setIdInstancia(prog.getIdEje());
                        }
                    }


                    if (dominio.getIdDistritoJudicial() != null) {
                        repoDistrito.findById(dominio.getIdDistritoJudicial().longValue())
                                .ifPresent(dj -> dominio.setNombreDistritoJudicial(dj.getNombre()));
                    }

                    if (dominio.getIdInstancia() != null) {
                        repoInstancia.findById(dominio.getIdInstancia().longValue())
                                .ifPresent(inst -> dominio.setNombreInstancia(inst.getDescripcion())); // O getDescripcion()
                    }

                    return dominio;
                })
                .collect(Collectors.toList());

        return Pagina.<Usuario>builder()
                .contenido(contenido)
                .totalRegistros(pageResult.getTotalElements())
                .totalPaginas(pageResult.getTotalPages())
                .paginaActual(pagina)
                .tamanioPagina(tamanio)
                .build();
    }

    @Override
    @Transactional
    public Usuario registrar(String cuo, Usuario usuario) {

        boolean requiereEje = false;
        String rolNuevoUsuario = "";

        if (usuario.getPerfiles() != null && !usuario.getPerfiles().isEmpty()) {
            for (var p : usuario.getPerfiles()) {
                var perfilDb = repoPerfil.findById(p.getIdPerfil())
                        .orElseThrow(() -> new MaestroNoEncontradoException("El perfil seleccionado (ID: " + p.getIdPerfil() + ") no existe."));

                rolNuevoUsuario = perfilDb.getRol();

                // Si es Juez o Secretario Técnico, el Eje es obligatorio
                if ("JUZAJUPJ".equals(rolNuevoUsuario) || "SCTAJUPJ".equals(rolNuevoUsuario)) {
                    requiereEje = true;
                    break;
                }
            }
        }

        // SI REQUIERE EJE Y NO LO TIENE -> ERROR (No se guarda nada en BD)
        if (requiereEje && (usuario.getIdEje() == null || usuario.getIdEje() <= 0)) {
            throw new IllegalArgumentException("Para registrar un usuario con perfil de Juez o Secretario Técnico, es obligatorio seleccionar un Eje.");
        }

        // GUARDAR USUARIO (Padre)
        MovUsuarioEntity entity = mapper.toEntity(usuario);
        entity.setIdUsuarioReg(usuario.getIdUsuarioReg());
        MovUsuarioEntity savedUser = repository.save(entity);

        // GUARDAR PERFILES (Hijos)
        if (usuario.getPerfiles() != null && !usuario.getPerfiles().isEmpty()) {
            var perfilesEntity = usuario.getPerfiles().stream().map(p -> {
                MovUsuarioPerfilEntity relacion = new MovUsuarioPerfilEntity();
                relacion.setUsuario(savedUser);

                MaePerfilEntity perfilMaestro = new MaePerfilEntity();
                perfilMaestro.setId(p.getIdPerfil());
                relacion.setPerfil(perfilMaestro);

                relacion.setActivo("1");
                relacion.setCAudId(usuario.getUsuario());
                relacion.setCAudIp(usuario.getNumeroIp());
                relacion.setCAudPc(usuario.getNombrePc());
                relacion.setCAudMcAddr(usuario.getDireccionMac());
                relacion.setCAudIdRed(usuario.getRed());
                return relacion;
            }).collect(Collectors.toList());

            usuarioPerfilRepository.saveAll(perfilesEntity);
        }

        // GUARDAR PROGRAMACIÓN EJE
        if (requiereEje) {
            MovProgramacionEjeEntity progEje = new MovProgramacionEjeEntity();
            progEje.setIdUsuario(savedUser.getId());
            progEje.setIdEje(usuario.getIdEje());
            progEje.setIdDistritoJudicial(usuario.getIdDistritoJudicial());
            progEje.setPeriodo(String.valueOf(java.time.LocalDate.now().getYear()));

            progEje.setActivo("1");
            progEje.setFRegistro(java.time.LocalDateTime.now());

            // Auditoría
            progEje.setCAudId(usuario.getUsuario());
            progEje.setCAudIp(usuario.getNumeroIp());
            progEje.setCAudPc(usuario.getNombrePc());
            progEje.setCAudMcAddr(usuario.getDireccionMac());
            progEje.setCAudIdRed(usuario.getRed());

            programacionEjeRepository.save(progEje);
        }

        return mapper.toUsuario(savedUser);
    }

    @Override
    @Transactional
    public Usuario actualizar(String cuo, Usuario usuario) {

        // 1. Obtener y actualizar Usuario Padre
        MovUsuarioEntity entityDb = repository.findById(usuario.getId())
                .orElseThrow(() -> new MaestroNoEncontradoException("Usuario no encontrado"));

        mapper.updateEntity(usuario, entityDb);
        MovUsuarioEntity savedUser = repository.save(entityDb);

        // 2. ACTUALIZAR PERFIL (Estrategia: Merge/Desactivar)
        if (usuario.getPerfiles() != null) {

            // A. Traer todos los perfiles que el usuario YA TIENE en BD (Activos e Inactivos)
            List<MovUsuarioPerfilEntity> perfilesEnBd = usuarioPerfilRepository.findByUsuarioId(savedUser.getId());

            // B. Obtener los IDs que vienen del Front
            List<Integer> idsNuevos = usuario.getPerfiles().stream()
                    .map(p -> p.getIdPerfil())
                    .toList();

            // C. Procesar los que ya existen en BD
            for (MovUsuarioPerfilEntity relacionDb : perfilesEnBd) {
                if (idsNuevos.contains(relacionDb.getPerfil().getId())) {
                    // CASO 1:  REACTIVAR (por si estaba en '0')
                    relacionDb.setActivo("1");
                    actualizarAuditoria(relacionDb, usuario);
                } else {
                    // CASO 2: DESACTIVAR ('0')
                    relacionDb.setActivo("0");
                    actualizarAuditoria(relacionDb, usuario);
                }
            }

            // D. Detectar los NUEVOS
            // Filtramos: De los nuevos, ¿cuáles NO están en la lista de BD?
            List<Integer> idsEnBd = perfilesEnBd.stream()
                    .map(e -> e.getPerfil().getId())
                    .toList();

            List<MovUsuarioPerfilEntity> nuevosAInsertar = new ArrayList<>();

            for (Integer idNuevo : idsNuevos) {
                if (!idsEnBd.contains(idNuevo)) {
                    // CASO 3: INSERTAR
                    MovUsuarioPerfilEntity nuevaRelacion = new MovUsuarioPerfilEntity();
                    nuevaRelacion.setUsuario(savedUser);

                    MaePerfilEntity perfilRef = new MaePerfilEntity();
                    perfilRef.setId(idNuevo);
                    nuevaRelacion.setPerfil(perfilRef);

                    nuevaRelacion.setActivo("1");
                    actualizarAuditoria(nuevaRelacion, usuario);

                    nuevosAInsertar.add(nuevaRelacion);
                }
            }

            usuarioPerfilRepository.saveAll(perfilesEnBd);
            usuarioPerfilRepository.saveAll(nuevosAInsertar);
        }

        // 3. ACTUALIZAR EJE (Desactivar anteriores)
        if (usuario.getIdEje() != null) {
            String periodoActual = String.valueOf(java.time.LocalDate.now().getYear());

            List<MovProgramacionEjeEntity> ejesEnBd = programacionEjeRepository
                    .findByIdUsuarioAndPeriodo(savedUser.getId(), periodoActual);

            boolean existeElNuevo = false;

            //Recorrer BD: Desactivar lo viejo, Activar si coincide el nuevo
            for (MovProgramacionEjeEntity ejeDb : ejesEnBd) {

                // Verificamos si este registro coincide exactamente con el Eje y Distrito que queremos asignar
                boolean esElMismoEje = ejeDb.getIdEje().equals(usuario.getIdEje()) &&
                        ejeDb.getIdDistritoJudicial().equals(usuario.getIdDistritoJudicial());

                if (esElMismoEje) {
                    // Ya existía este registro -> Lo reactivamos
                    ejeDb.setActivo("1");
                    actualizarAuditoria(ejeDb, usuario);
                    existeElNuevo = true;
                } else {
                    // Es un eje diferente (o distrito diferente) -> Lo desactivamos
                    ejeDb.setActivo("0");
                    actualizarAuditoria(ejeDb, usuario);
                }
            }
            // Guardamos los cambios de estado
            programacionEjeRepository.saveAll(ejesEnBd);

            // C. Si no encontramos el registro en BD, lo creamos nuevo
            if (!existeElNuevo) {
                MovProgramacionEjeEntity nuevoEje = new MovProgramacionEjeEntity();

                // PK Compuesta
                nuevoEje.setIdUsuario(savedUser.getId());
                nuevoEje.setIdEje(usuario.getIdEje());
                nuevoEje.setIdDistritoJudicial(usuario.getIdDistritoJudicial());
                nuevoEje.setPeriodo(periodoActual);

                nuevoEje.setActivo("1");
                actualizarAuditoria(nuevoEje, usuario);

                programacionEjeRepository.save(nuevoEje);
            }
        }

        return mapper.toUsuario(savedUser);
    }

    private void actualizarAuditoria(Object entity, Usuario usuario) {
        if (entity instanceof MovUsuarioPerfilEntity e) {
            e.setFAud(java.time.LocalDateTime.now());
            e.setCAudId(usuario.getUsuario());
            e.setCAudIp(usuario.getNumeroIp());
            e.setCAudPc(usuario.getNombrePc());
            e.setCAudMcAddr(usuario.getDireccionMac());
            e.setCAudIdRed(usuario.getRed());
            e.setBAud("M");
        } else if (entity instanceof MovProgramacionEjeEntity e) {
            e.setFRegistro(java.time.LocalDateTime.now()); // O fAud
            e.setCAudId(usuario.getUsuario());
            e.setCAudIp(usuario.getNumeroIp());
            e.setCAudPc(usuario.getNombrePc());
            e.setCAudMcAddr(usuario.getDireccionMac());
            e.setCAudIdRed(usuario.getRed());
            e.setBAud("M");
        }
    }

    @Override
    public Usuario buscarPorId(String cuo, Integer id) {
        Usuario dominio = repository.findById(id)
                .map(mapper::toUsuario)
                .orElse(null);

        if (dominio != null) {

            var programacionOpt = programacionEjeRepository.findFirstByIdUsuarioAndActivo(dominio.getId(), "1");

            if (programacionOpt.isPresent()) {
                var prog = programacionOpt.get();
                if (prog.getIdDistritoJudicial() != null) dominio.setIdDistritoJudicial(prog.getIdDistritoJudicial());
                if (prog.getIdEje() != null) dominio.setIdInstancia(prog.getIdEje());
            }

            if (dominio.getIdDistritoJudicial() != null) {
                repoDistrito.findById(dominio.getIdDistritoJudicial().longValue())
                        .ifPresent(dj -> dominio.setNombreDistritoJudicial(dj.getNombre()));
            }

            if (dominio.getIdInstancia() != null) {
                repoInstancia.findById(dominio.getIdInstancia().longValue())
                        .ifPresent(inst -> dominio.setNombreInstancia(inst.getDescripcion()));
            }
        }

        return dominio;
    }

    @Override
    public boolean existeUsuarioPorLogin(String cuo, String login) {
        return repository.existsByUsuario(login);
    }

    @Override
    @Transactional
    public void cambiarEstado(String cuo, Usuario usuario) {

        MovUsuarioEntity entityDb = repository.findById(usuario.getId())
                .orElseThrow(() -> new MaestroNoEncontradoException("Usuario no encontrado con ID: " + usuario.getId()));


        mapper.updateEntity(usuario, entityDb);

        entityDb.setFAud(java.time.LocalDateTime.now());
        entityDb.setBAud("M");

        repository.save(entityDb);
    }

    @Override
    public Integer obtenerIdPerfilPorLogin(String login) {
        //Buscamos el usuario por su login
        MovUsuarioEntity usuario = repository.findByActivoAndUsuario("1", login)
                .orElseThrow(() -> new MaestroNoEncontradoException("El usuario creador no existe o no está activo."));

        // Buscamos su perfil activo
        return usuarioPerfilRepository.findByUsuarioId(usuario.getId()).stream()
                .filter(p -> "1".equals(p.getActivo()))
                .findFirst()
                .map(p -> p.getPerfil().getId())
                .orElseThrow(() -> new MaestroNoEncontradoException("El usuario creador no tiene un perfil activo para realizar esta acción."));
    }
    @Override
    public String obtenerRolPorIdPerfil(Integer idPerfil) {
        return repoPerfil.findById(idPerfil)
                .map(pe.gob.pj.accesojusticia.infraestructure.db.negocio.entities.MaePerfilEntity::getRol)
                .orElse("");
    }
    @Override
    public boolean validarJerarquia(Integer idPerfilPadre, Integer idPerfilHijo) {
        return jerarquiaRepository.existeJerarquia(idPerfilPadre, idPerfilHijo);
    }

    @Override
    public Integer obtenerIdPerfilPorIdUsuario(Integer idUsuario) {
        return usuarioPerfilRepository.findByUsuarioId(idUsuario).stream()
                .filter(p -> "1".equals(p.getActivo()))
                .findFirst()
                .map(p -> p.getPerfil().getId())
                .orElse(null);
    }

    @Override
    @Transactional
    public void actualizarClave(String cuo, Integer idUsuario, String nuevaClave) {
        MovUsuarioEntity entity = repository.findById(idUsuario)
                .orElseThrow(() -> new MaestroNoEncontradoException("Usuario no encontrado"));

        entity.setClave(nuevaClave);

        entity.setFAud(java.time.LocalDateTime.now());
        entity.setBAud("M");

        repository.save(entity);
    }

    @Override
    public Usuario buscarPorLogin(String cuo, String login) {
        if (login == null) return null;

        return repository.findByActivoAndUsuarioIgnoreCase("1", login.trim())
                .map(mapper::toUsuario)
                .orElse(null);
    }
}