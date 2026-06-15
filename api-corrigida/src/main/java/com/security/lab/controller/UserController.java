package com.security.lab.controller;

import com.security.lab.model.User;
import com.security.lab.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Correção da Vuln 1: Endpoint protegido pelo Spring Security e JWT
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Accessing all users (SECURE)");
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Correção da Vuln 1: Endpoint protegido pelo Spring Security e JWT
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("Accessing user ID: {} (SECURE)", id);
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}