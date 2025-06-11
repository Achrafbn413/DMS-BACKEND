package com.example.dms_backend.repository;

import com.example.dms_backend.model.ParametreSysteme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParametreSystemeRepository extends JpaRepository<ParametreSysteme, Long> {
    Optional<ParametreSysteme> findByNom(String nom);
}
