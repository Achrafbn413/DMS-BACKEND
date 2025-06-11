package com.example.dms_backend.repository;

import com.example.dms_backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    boolean existsByReference(String reference);

    @Query("SELECT t FROM Transaction t WHERE LOWER(t.banqueEmettrice.nom) = LOWER(:institution) OR LOWER(t.banqueAcquereuse.nom) = LOWER(:institution)")
    List<Transaction> findByBanqueEmettriceNomOrBanqueAcquereuseNom(@Param("institution") String institution1, @Param("institution") String institution2);
    Optional<Transaction> findByReference(String reference);
}
