package com.example.dms_backend.controller;

import com.example.dms_backend.model.Utilisateur;
import com.example.dms_backend.model.ActionAdmin;
import com.example.dms_backend.model.Institution;
import com.example.dms_backend.repository.UserRepository;
import com.example.dms_backend.repository.InstitutionRepository;
import com.example.dms_backend.repository.ActionAdminRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/utilisateurs")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final ActionAdminRepository actionAdminRepository;
    private final InstitutionRepository institutionRepository;

    public UserController(UserRepository userRepository, ActionAdminRepository actionAdminRepository, InstitutionRepository institutionRepository) {
        this.userRepository = userRepository;
        this.actionAdminRepository = actionAdminRepository;
        this.institutionRepository = institutionRepository;
    }

    @GetMapping
    public List<Utilisateur> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Utilisateur utilisateur) {
        if (userRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email déjà utilisé");
        }
        utilisateur.setEnabled(true);
        utilisateur.setPassword("ENCODE_ME"); // Ajoute encodeur plus tard

        if (utilisateur.getInstitution() != null && utilisateur.getInstitution().getId() != null) {
            institutionRepository.findById(utilisateur.getInstitution().getId()).ifPresent(utilisateur::setInstitution);
        }

        Utilisateur savedUser = userRepository.save(utilisateur);
        logAction("Création de l'utilisateur: " + utilisateur.getEmail());
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Utilisateur utilisateur) {
        return userRepository.findById(id).map(u -> {
            u.setNom(utilisateur.getNom());
            u.setEmail(utilisateur.getEmail());
            u.setRole(utilisateur.getRole());

            if (utilisateur.getInstitution() != null && utilisateur.getInstitution().getId() != null) {
                institutionRepository.findById(utilisateur.getInstitution().getId()).ifPresent(u::setInstitution);
            }

            Utilisateur updated = userRepository.save(u);
            logAction("Mise à jour de l'utilisateur: " + u.getEmail());
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> {
            u.setEnabled(false);
            Utilisateur updated = userRepository.save(u);
            logAction("Désactivation de l'utilisateur: " + u.getEmail());
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/reactiver/{id}")
    public ResponseEntity<?> reactivateUser(@PathVariable Long id) {
        return userRepository.findById(id).map(u -> {
            u.setEnabled(true);
            Utilisateur updated = userRepository.save(u);
            logAction("Réactivation de l'utilisateur: " + u.getEmail());
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    private void logAction(String action) {
        ActionAdmin log = new ActionAdmin();
        log.setAction(action);
        log.setTimestamp(LocalDateTime.now());
        actionAdminRepository.save(log);
    }

    @GetMapping("/institutions")
    public List<Institution> getAllInstitutions() {
        return institutionRepository.findAll();
    }
}
