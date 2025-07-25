package com.example.dms_backend.service;

import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.NotificationRepository;
import com.example.dms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * ✅ MÉTHODE PRINCIPALE : Notification intelligente
     * Notifie l'AUTRE banque dans la transaction
     */
    public Notification notifierAutreBanque(Transaction transaction, Utilisateur utilisateurSignaleur, String messagePersonnalise) {
        if (transaction == null || utilisateurSignaleur == null) {
            throw new IllegalArgumentException("Transaction et utilisateur sont requis");
        }

        Institution institutionSignaleuse = utilisateurSignaleur.getInstitution();
        if (institutionSignaleuse == null) {
            throw new IllegalArgumentException("L'utilisateur doit être associé à une institution");
        }

        // ✅ LOGIQUE INTELLIGENTE : Déterminer quelle banque notifier
        Institution banqueANotifier = determinerBanqueANotifier(transaction, institutionSignaleuse);

        if (banqueANotifier == null) {
            log.warn("⚠️ Aucune banque à notifier pour la transaction {}", transaction.getReference());
            return null;
        }

        // ✅ Créer et sauvegarder la notification
        String message = messagePersonnalise != null ? messagePersonnalise :
                String.format("Une transaction a été signalée par %s (Réf: %s)",
                        institutionSignaleuse.getNom(), transaction.getReference());

        Notification notification = Notification.builder()
                .destinataire(banqueANotifier)
                .transaction(transaction)
                .message(message)
                .lue(false)
                .dateCreation(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        log.info("✅ Notification créée et envoyée à {} pour transaction {}",
                banqueANotifier.getNom(), transaction.getReference());

        return saved;
    }

    /**
     * ✅ LOGIQUE INTELLIGENTE : Détermine quelle banque notifier
     */
    private Institution determinerBanqueANotifier(Transaction transaction, Institution institutionSignaleuse) {
        // Si l'institution signaleuse est l'émettrice → notifier l'acquéreuse
        if (transaction.getBanqueEmettrice() != null &&
                Objects.equals(transaction.getBanqueEmettrice().getId(), institutionSignaleuse.getId())) {
            log.info("🏦 Institution signaleuse = Émettrice → Notification vers Acquéreuse: {}",
                    transaction.getBanqueAcquereuse() != null ? transaction.getBanqueAcquereuse().getNom() : "null");
            return transaction.getBanqueAcquereuse();
        }

        // Si l'institution signaleuse est l'acquéreuse → notifier l'émettrice
        if (transaction.getBanqueAcquereuse() != null &&
                Objects.equals(transaction.getBanqueAcquereuse().getId(), institutionSignaleuse.getId())) {
            log.info("🏦 Institution signaleuse = Acquéreuse → Notification vers Émettrice: {}",
                    transaction.getBanqueEmettrice() != null ? transaction.getBanqueEmettrice().getNom() : "null");
            return transaction.getBanqueEmettrice();
        }

        log.warn("⚠️ Institution signaleuse {} ne correspond ni à l'émettrice ni à l'acquéreuse",
                institutionSignaleuse.getNom());
        return null;
    }

    /**
     * ✅ Récupérer les notifications non lues pour une institution
     */
    public List<Notification> getNotificationsNonLues(Institution institution) {
        if (institution == null) {
            throw new IllegalArgumentException("Institution requise");
        }

        List<Notification> notifications = notificationRepository.findByDestinataireAndLueFalse(institution);
        log.info("📋 {} notifications non lues trouvées pour {}", notifications.size(), institution.getNom());

        return notifications;
    }

    /**
     * ✅ Marquer une notification comme lue
     */
    public boolean marquerCommeLue(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .map(notification -> {
                    notification.setLue(true);
                    notificationRepository.save(notification);
                    log.info("✅ Notification {} marquée comme lue", notificationId);
                    return true;
                })
                .orElse(false);
    }

    /**
     * ✅ Compter les notifications non lues pour une institution
     */
    public long compterNotificationsNonLues(Institution institution) {
        if (institution == null) return 0;

        long count = notificationRepository.findByDestinataireAndLueFalse(institution).size();
        log.debug("📊 {} notifications non lues pour {}", count, institution.getNom());

        return count;
    }

    /**
     * ✅ Méthode de compatibilité (si encore utilisée ailleurs)
     * @deprecated Utiliser notifierAutreBanque() à la place
     */
    @Deprecated
    public void notifierBanqueAcquereuse(Litige litige) {
        log.warn("⚠️ Méthode deprecated notifierBanqueAcquereuse() appelée");
        if (litige != null && litige.getDeclarePar() != null) {
            notifierAutreBanque(litige.getTransaction(), litige.getDeclarePar(),
                    "Litige signalé sur transaction " + litige.getTransaction().getReference());
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Mise à jour du compteur de notifications (pour Angular)
     */
    public void updateUnreadCount(long count) {
        // Cette méthode peut être étendue pour WebSocket/SSE si nécessaire
        log.debug("📊 Compteur de notifications mis à jour: {}", count);
    }
}