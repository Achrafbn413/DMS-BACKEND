package com.example.dms_backend.service.impl;

import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.MetaTransactionRepository;
import com.example.dms_backend.repository.SatimTransactionRepository;
import com.example.dms_backend.repository.TransactionRepository;
import com.example.dms_backend.service.SatimTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public void importFile(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<SatimTransaction> satimTransactions = new ArrayList<>();
            String line;
            reader.readLine(); // Ignore header

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Ignorer les lignes vides

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

            // 1. Sauvegarder les transactions SATIM
            satimTransactionRepository.saveAll(satimTransactions);
            log.info("✅ {} SATIM transactions importées", satimTransactions.size());

            // 2. Créer les transactions principales (si elles n'existent pas déjà)
            List<Transaction> transactions = new ArrayList<>();
            for (SatimTransaction s : satimTransactions) {
                if (!transactionRepository.existsByReference(s.getStrRecoCode())) {
                    Transaction transaction = convertToTransaction(s);
                    transactions.add(transaction);
                }
            }

            if (!transactions.isEmpty()) {
                transactionRepository.saveAll(transactions);
                log.info("✅ {} nouvelles Transactions créées", transactions.size());
            }

            // 3. Créer les méta-transactions avec le bon mapping
            List<MetaTransaction> metaTransactions = new ArrayList<>();
            for (SatimTransaction s : satimTransactions) {
                // Trouver la transaction correspondante
                Transaction correspondingTransaction = transactionRepository.findByReference(s.getStrRecoCode())
                        .orElse(null);

                if (correspondingTransaction != null) {
                    // Vérifier si la meta-transaction n'existe pas déjà
                    if (!metaTransactionRepository.existsByStrCode(s.getStrCode())) {
                        MetaTransaction meta = MetaTransaction.builder()
                                .strCode(s.getStrCode()) // ✅ Ajout du strCode
                                .strRecoCode(s.getStrRecoCode())
                                .strRecoNumb(s.getStrRecoNumb())
                                .strOperCode(s.getStrOperCode())
                                .strProcDate(s.getStrProcDate())
                                .strTermIden(s.getStrTermIden())
                                .transaction(correspondingTransaction)
                                .build();

                        metaTransactions.add(meta);
                    }
                }
            }

            if (!metaTransactions.isEmpty()) {
                metaTransactionRepository.saveAll(metaTransactions);
                log.info("✅ {} MetaTransactions sauvegardées", metaTransactions.size());
            }

        } catch (Exception e) {
            log.error("❌ Erreur import SATIM", e);
            throw new Exception("Erreur import SATIM : " + e.getMessage());
        }
    }

    private SatimTransaction parseLineToTransaction(String line) {
        try {
            // Support pour différents délimiteurs (virgule ou point-virgule)
            String[] fields = line.contains(";") ? line.split(";") : line.split(",");

            if (fields.length < 6) {
                log.warn("Ligne invalide (moins de 6 champs): {}", line);
                return null;
            }

            Long strCode = parseLong(fields[0]);
            String recoCode = fields[1].trim();
            Long recoNumb = parseLong(fields[2]);
            String operCode = fields[3].trim();
            LocalDate procDate = parseDate(fields[4]);
            String termIden = fields[5].trim();

            if (strCode == null || recoCode.isEmpty() || recoNumb == null || procDate == null) {
                log.warn("Champ obligatoire manquant ou invalide : {}", line);
                return null;
            }

            return SatimTransaction.builder()
                    .strCode(strCode)
                    .strRecoCode(recoCode)
                    .strRecoNumb(recoNumb)
                    .strOperCode(operCode)
                    .strProcDate(procDate)
                    .strTermIden(termIden)
                    .build();

        } catch (Exception e) {
            log.error("Erreur parsing ligne: {}", line, e);
            return null;
        }
    }

    private Transaction convertToTransaction(SatimTransaction s) {
        return Transaction.builder()
                .reference(s.getStrRecoCode()) // Utilise strRecoCode comme référence
                .montant(BigDecimal.valueOf(s.getStrRecoNumb())) // strRecoNumb devient le montant
                .type(parseTypeTransaction(s.getStrOperCode()))
                .statut(StatutTransaction.NORMALE)
                .dateTransaction(s.getStrProcDate())
                // banqueEmettrice et banqueAcquereuse peuvent être définies plus tard
                .build();
    }

    private TypeTransaction parseTypeTransaction(String operCode) {
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
            // Essayer plusieurs formats de date
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