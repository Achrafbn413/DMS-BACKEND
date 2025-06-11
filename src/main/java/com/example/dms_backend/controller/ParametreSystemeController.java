package com.example.dms_backend.controller;

import com.example.dms_backend.model.ParametreSysteme;
import com.example.dms_backend.repository.ParametreSystemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parametres")
@RequiredArgsConstructor
public class ParametreSystemeController {

    private final ParametreSystemeRepository parametreRepo;

    @GetMapping
    public List<ParametreSysteme> getAll() {
        return parametreRepo.findAll();
    }

    @PostMapping
    public ParametreSysteme create(@RequestBody ParametreSysteme param) {
        return parametreRepo.save(param);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParametreSysteme> update(@PathVariable Long id, @RequestBody ParametreSysteme param) {
        return parametreRepo.findById(id)
                .map(p -> {
                    p.setNom(param.getNom());
                    p.setValeur(param.getValeur());
                    return ResponseEntity.ok(parametreRepo.save(p));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        parametreRepo.deleteById(id);
    }
}
