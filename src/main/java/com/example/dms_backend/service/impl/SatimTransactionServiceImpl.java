package com.example.dms_backend.service.impl;

import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.InstitutionRepository;
import com.example.dms_backend.repository.InstitutionSatimMappingRepository;
import com.example.dms_backend.repository.MetaTransactionRepository;
import com.example.dms_backend.repository.SatimTransactionRepository;
import com.example.dms_backend.repository.TransactionRepository;
import com.example.dms_backend.service.SatimTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SatimTransactionServiceImpl implements SatimTransactionService {

    private final SatimTransactionRepository satimTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final MetaTransactionRepository metaTransactionRepository;
    private final InstitutionRepository institutionRepository;
    // ✅ NOUVEAU : Injection repository mapping
    private final InstitutionSatimMappingRepository institutionSatimMappingRepository;

    @Override
    @Transactional
    public void importFile(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<SatimTransaction> satimTransactions = new ArrayList<>();

            String line;
            reader.readLine(); // Ignore header

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                SatimTransaction transaction = parseLineToTransaction(line);
                if (transaction != null) {
                    satimTransactions.add(transaction);
                } else {
                    log.warn("⛔ Ligne ignorée : {}", line);
                }
            }

            if (satimTransactions.isEmpty()) {
                log.warn("⚠ Aucun enregistrement importé depuis le fichier");
                return;
            }

            // 🔧 AMÉLIORATION: Import en 3 étapes avec meilleure gestion des liens

            // 1️⃣ Sauvegarder d'abord toutes les SatimTransactions
            satimTransactionRepository.saveAll(satimTransactions);
            log.info("✅ {} SATIM transactions importées", satimTransactions.size());

            // 2️⃣ Créer/récupérer les Transactions principales avec gestion des doublons
            List<Transaction> newTransactions = new ArrayList<>();
            for (SatimTransaction s : satimTransactions) {
                Transaction transaction = getOrCreateTransaction(s);
                if (transaction.getId() == null) { // Nouvelle transaction
                    newTransactions.add(transaction);
                }
            }

            if (!newTransactions.isEmpty()) {
                transactionRepository.saveAll(newTransactions);
                log.info("✅ {} nouvelles Transactions créées", newTransactions.size());
            }

            // 3️⃣ Créer les MetaTransactions avec le bon mapping strCode
            List<MetaTransaction> newMetaTransactions = new ArrayList<>();
            int linkedCount = 0;

            for (SatimTransaction s : satimTransactions) {
                // Récupérer la Transaction correspondante (maintenant sauvegardée)
                Transaction correspondingTransaction = transactionRepository
                        .findByReference(s.getStrRecoCode()).orElse(null);

                if (correspondingTransaction != null) {
                    // ✅ CORRECTION IMPORTANTE: Vérifier par strCode, pas par strRecoCode
                    if (!metaTransactionRepository.existsByStrCode(s.getStrCode())) {
                        MetaTransaction meta = MetaTransaction.builder()
                                .strCode(s.getStrCode()) // ✅ ESSENTIEL: strCode pour le mapping
                                .strRecoCode(s.getStrRecoCode())
                                .strRecoNumb(s.getStrRecoNumb())
                                .strOperCode(s.getStrOperCode())
                                .strProcDate(s.getStrProcDate())
                                .strTermIden(s.getStrTermIden())
                                .transaction(correspondingTransaction)
                                .build();

                        newMetaTransactions.add(meta);
                        linkedCount++;
                    } else {
                        log.debug("MetaTransaction avec strCode={} existe déjà", s.getStrCode());
                    }
                } else {
                    log.warn("⚠️ Aucune Transaction trouvée pour SatimTransaction strCode={}, strRecoCode={}",
                            s.getStrCode(), s.getStrRecoCode());
                }
            }

            if (!newMetaTransactions.isEmpty()) {
                metaTransactionRepository.saveAll(newMetaTransactions);
                log.info("✅ {} MetaTransactions sauvegardées et liées", newMetaTransactions.size());
            }

            log.info("📊 RÉSUMÉ IMPORT:");
            log.info("   - {} SatimTransactions importées", satimTransactions.size());
            log.info("   - {} nouvelles Transactions créées", newTransactions.size());
            log.info("   - {} MetaTransactions liées", linkedCount);

        } catch (Exception e) {
            log.error("❌ Erreur import SATIM", e);
            throw new Exception("Erreur import SATIM : " + e.getMessage());
        }
    }

    /**
     * ✅ MÉTHODE COMPLÈTEMENT CORRIGÉE : Utilise les codes SATIM réels
     */
    private Transaction getOrCreateTransaction(SatimTransaction s) {
        Transaction existingTransaction = transactionRepository
                .findByReference(s.getStrRecoCode()).orElse(null);

        if (existingTransaction != null) {
            log.debug("Transaction existante trouvée: {}", s.getStrRecoCode());
            return existingTransaction;
        }

        // ✅ NOUVELLE LOGIQUE : Utiliser codes SATIM réels
        Institution emettrice = findInstitutionBySatimCode(s.getStrIssuBanCode());
        Institution acquereuse = findInstitutionBySatimCode(s.getStrAcquBanCode());

        // Log pour debug réaliste
        log.info("💳 Transaction {} : {} → {}",
                s.getStrRecoCode(),
                emettrice != null ? emettrice.getNom() : "INCONNUE (" + s.getStrIssuBanCode() + ")",
                acquereuse != null ? acquereuse.getNom() : "INCONNUE (" + s.getStrAcquBanCode() + ")");

        return Transaction.builder()
                .reference(s.getStrRecoCode())
                .montant(s.getStrRecoNumb() != null ?
                        BigDecimal.valueOf(s.getStrRecoNumb()) :
                        BigDecimal.ZERO)
                .type(parseTypeTransaction(s.getStrOperCode()))
                .statut(StatutTransaction.NORMALE)
                .dateTransaction(s.getStrProcDate() != null ?
                        s.getStrProcDate() :
                        LocalDate.now())
                .banqueEmettrice(emettrice)    // ✅ RÉEL depuis SATIM
                .banqueAcquereuse(acquereuse)  // ✅ RÉEL depuis SATIM
                .build();
    }

    // ✅ NOUVELLE MÉTHODE : Trouver institution par code SATIM
    private Institution findInstitutionBySatimCode(String satimCode) {
        if (satimCode == null || satimCode.trim().isEmpty()) {
            log.warn("⚠️ Code SATIM vide ou null");
            return null;
        }

        return institutionSatimMappingRepository
                .findInstitutionBySatimCode(satimCode.trim())
                .orElse(null);
    }

    /**
     * ✅ MÉTHODE CORRIGÉE : Parse 8 champs au lieu de 6
     */
    private SatimTransaction parseLineToTransaction(String line) {
        try {
            String[] fields = line.contains(";") ? line.split(";") : line.split(",");

            if (fields.length < 8) { // ✅ MAINTENANT 8 champs minimum
                log.warn("Ligne invalide (moins de 8 champs): {}", line);
                return null;
            }

            Long strCode = parseLong(fields[0]);
            String recoCode = fields[1].trim();
            Long recoNumb = parseLong(fields[2]);
            String operCode = fields[3].trim();
            LocalDate procDate = parseDate(fields[4]);
            String termIden = fields[5].trim();
            String issuBanCode = fields[6].trim(); // ✅ NOUVEAU
            String acquBanCode = fields[7].trim(); // ✅ NOUVEAU

            // ✅ VALIDATIONS
            if (strCode == null || strCode <= 0) {
                log.warn("strCode invalide : {}", line);
                return null;
            }

            if (recoCode.isEmpty()) {
                log.warn("strRecoCode manquant : {}", line);
                return null;
            }

            return SatimTransaction.builder()
                    .strCode(strCode)
                    .strRecoCode(recoCode)
                    .strRecoNumb(recoNumb)
                    .strOperCode(operCode)
                    .strProcDate(procDate)
                    .strTermIden(termIden)
                    .strIssuBanCode(issuBanCode)  // ✅ NOUVEAU
                    .strAcquBanCode(acquBanCode)  // ✅ NOUVEAU
                    .build();

        } catch (Exception e) {
            log.error("Erreur parsing ligne: {}", line, e);
            return null;
        }
    }

    private TypeTransaction parseTypeTransaction(String operCode) {
        return getTypeTransaction(operCode, log);
    }

    public static TypeTransaction getTypeTransaction(String operCode, Logger log) {
        if (operCode == null || operCode.trim().isEmpty()) {
            return TypeTransaction.AUTRE;
        }

        try {
            return TypeTransaction.valueOf(operCode.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Type de transaction inconnu: {}, utilisation de AUTRE", operCode);
            return TypeTransaction.AUTRE;
        }
    }

    private Long parseLong(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            log.warn("Impossible de parser le nombre: {}", s);
            return null;
        }
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }

        try {
            String dateTrimmed = s.trim();

            // Format principal: yyyy-MM-dd
            try {
                return LocalDate.parse(dateTrimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e1) {
                // Format alternatif: dd/MM/yyyy
                try {
                    return LocalDate.parse(dateTrimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (Exception e2) {
                    // Format alternatif: dd-MM-yyyy
                    return LocalDate.parse(dateTrimmed, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                }
            }
        } catch (Exception e) {
            log.warn("Impossible de parser la date: {}", s);
            return null;
        }
    }
}