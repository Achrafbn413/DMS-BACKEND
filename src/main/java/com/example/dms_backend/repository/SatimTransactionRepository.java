package com.example.dms_backend.repository;

import com.example.dms_backend.model.SatimTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SatimTransactionRepository extends JpaRepository<SatimTransaction, Long> {
    // Méthodes personnalisées si nécessaire
}