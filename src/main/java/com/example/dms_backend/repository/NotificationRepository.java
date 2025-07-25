package com.example.dms_backend.repository;

import com.example.dms_backend.model.Institution;
import com.example.dms_backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByDestinataireAndLueFalse(Institution destinataire);
}
