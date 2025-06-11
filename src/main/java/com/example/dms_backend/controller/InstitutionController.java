package com.example.dms_backend.controller;

import com.example.dms_backend.model.Institution;
import com.example.dms_backend.model.Utilisateur;
import com.example.dms_backend.model.NiveauAcces;
import com.example.dms_backend.repository.InstitutionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/institutions")
@CrossOrigin(origins = "*")
public class InstitutionController {

    private final InstitutionRepository institutionRepository;

    public InstitutionController(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    @GetMapping
    public List<Institution> getAll() {
        return institutionRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Institution> createInstitution(@RequestBody Institution institution) {
        Institution savedInstitution = institutionRepository.save(institution);
        return new ResponseEntity<>(savedInstitution, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/enable")
    public Institution enable(@PathVariable Long id) {
        Institution inst = institutionRepository.findById(id).orElseThrow();
        inst.setEnabled(true);
        return institutionRepository.save(inst);
    }

    @PutMapping("/{id}/disable")
    public Institution disable(@PathVariable Long id) {
        Institution inst = institutionRepository.findById(id).orElseThrow();
        inst.setEnabled(false);
        return institutionRepository.save(inst);
    }

    @GetMapping("/enabled")
    public List<Institution> getEnabledInstitutions() {
        return institutionRepository.findByEnabledTrue();
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<Institution>> getInstitutionsFilteredByNiveau(Authentication authentication) {
        Utilisateur user = (Utilisateur) authentication.getPrincipal();

        if (user.getNiveaux() == NiveauAcces.ELEVE) {
            return ResponseEntity.ok(institutionRepository.findAll());
        } else {
            return ResponseEntity.ok(
                    institutionRepository.findById(user.getInstitution().getId()).stream().toList()
            );
        }
    }
}
