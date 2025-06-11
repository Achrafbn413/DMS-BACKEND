package com.example.dms_backend.repository;

import com.example.dms_backend.model.Institution;
import com.example.dms_backend.model.Litige;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LitigeRepository extends JpaRepository<Litige, Long> {
    List<Litige> findByTransactionBanqueAcquereuse(Institution institution);
}