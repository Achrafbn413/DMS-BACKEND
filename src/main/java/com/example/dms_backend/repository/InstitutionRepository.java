package com.example.dms_backend.repository;

import com.example.dms_backend.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    List<Institution> findByEnabledTrue();

}
