package com.example.dms_backend.service;

import com.example.dms_backend.model.Utilisateur;

public interface UserService {
    Utilisateur getUserByUsername(String email);
}
