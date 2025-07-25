package com.example.dms_backend.repository;

import com.example.dms_backend.model.InstitutionSatimMapping;
import com.example.dms_backend.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionSatimMappingRepository extends JpaRepository<InstitutionSatimMapping, Long> {

    /**
     * Trouver institution par code SATIM
     */
    @Query("SELECT ism.institution FROM InstitutionSatimMapping ism WHERE ism.satimBankCode = :code")
    Optional<Institution> findInstitutionBySatimCode(@Param("code") String satimBankCode);

    /**
     * Vérifier si un code SATIM existe
     */
    boolean existsBySatimBankCode(String satimBankCode);
}