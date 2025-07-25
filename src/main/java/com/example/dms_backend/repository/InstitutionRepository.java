package com.example.dms_backend.repository;

import com.example.dms_backend.model.Institution;
import com.example.dms_backend.model.TypeInstitution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    List<Institution> findByEnabledTrue();

    // Méthode manquante utilisée dans SatimTransactionServiceImpl
    List<Institution> findByType(TypeInstitution type);

    // Méthode optionnelle pour récupérer les institutions actives par type
    List<Institution> findByTypeAndEnabledTrue(TypeInstitution type);
}