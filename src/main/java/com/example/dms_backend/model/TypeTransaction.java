package com.example.dms_backend.model;

public enum TypeTransaction {
    VIREMENT,
    CARTE,
    AUTRE,      // ✅ Fallback pour valeurs inconnues
    E_WALLET    // ✅ Pour les transactions e-wallet
}