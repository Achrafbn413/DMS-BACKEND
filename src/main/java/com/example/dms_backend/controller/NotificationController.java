package com.example.dms_backend.controller;

import com.example.dms_backend.model.Notification;
import com.example.dms_backend.model.Utilisateur;
import com.example.dms_backend.repository.NotificationRepository;
import com.example.dms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping("/non-lues")
    public List<Notification> getNotificationsNonLues() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Utilisateur user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return notificationRepository.findByDestinataireAndLueFalse(user.getInstitution());
    }
}
