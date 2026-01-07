package pe.gob.pj.prueba.infraestructure.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.gob.pj.prueba.infraestructure.db.negocio.entities.masters.MaeDistritoJudicialEntity;
import pe.gob.pj.prueba.infraestructure.db.negocio.repositories.masters.MaeDistritoJudicialRepository;

import java.util.List;

@RestController
@RequestMapping("/publico/db")
public class TestDbController {

    @Autowired
    private MaeDistritoJudicialRepository distritoRepository;

    @GetMapping("/distritos")
    public List<MaeDistritoJudicialEntity> listarDistritos() {
        return distritoRepository.findAll();
    }

    @PostMapping("/distritos")
    public MaeDistritoJudicialEntity guardar(@RequestBody MaeDistritoJudicialEntity entidad) {
        return distritoRepository.save(entidad);
    }
}