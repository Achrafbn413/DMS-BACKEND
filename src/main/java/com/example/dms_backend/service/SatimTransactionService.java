package com.example.dms_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface SatimTransactionService {
    void importFile(MultipartFile file) throws Exception;
}
