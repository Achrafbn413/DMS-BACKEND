package com.example.dms_backend.model;

public enum StatutLitige {
    CREE,           // Litige créé par le client
    OUVERT,         // Litige ouvert par la banque (signalement)
    EN_ATTENTE,     // En attente de traitement
    VU,             // ✅ NOUVEAU: Litige vu par la banque (pour notifications)
    EN_COURS,       // En cours de traitement
    RESOLU,         // Résolu
    FERME           // Fermé définitivement
}