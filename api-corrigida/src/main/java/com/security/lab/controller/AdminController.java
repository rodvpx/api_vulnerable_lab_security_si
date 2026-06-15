package com.security.lab.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    // Correção da Vuln 1: Endpoint protegido e restrito à role ADMIN (configurado no SecurityConfig)
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        logger.info("Accessing admin dashboard (SECURE)");
        return ResponseEntity.ok(Map.of(
            "message", "Bem-vindo ao painel de administração seguro!",
            "systemStatus", "OK",
            "activeUsers", 42
        ));
    }
}