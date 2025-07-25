package com.example.dms_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private boolean lue = false;
    private LocalDateTime dateCreation;

    @ManyToOne
    private Transaction transaction;

    @ManyToOne
    private Institution destinataire;
}
