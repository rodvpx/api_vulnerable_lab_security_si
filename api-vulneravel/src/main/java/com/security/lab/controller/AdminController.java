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

    // Vulnerabilidade 1: Acesso indevido (qualquer um acessa painel de admin)
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        logger.info("Accessing admin dashboard (VULNERABLE)");
        return ResponseEntity.ok(Map.of(
            "message", "Bem-vindo ao painel de administração!",
            "systemStatus", "CRITICAL",
            "activeUsers", 42
        ));
    }
}