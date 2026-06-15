package com.security.lab.controller;

import com.security.lab.model.User;
import com.security.lab.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Vulnerabilidade 3: Falta de validação (aceita qualquer coisa)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        logger.info("Register attempt: {}", user.getEmail());
        // Não há validação de email, senha, etc.
        user.setRole("USER");
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso!"));
    }

    // Vulnerabilidade 2: Enumeração de Usuários (mensagens específicas)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        logger.info("Login attempt for email: {}", email);

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            logger.warn("User not found: {}", email);
            return ResponseEntity.status(401).body(Map.of("message", "Usuário não encontrado"));
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            logger.warn("Incorrect password for user: {}", email);
            return ResponseEntity.status(401).body(Map.of("message", "Senha incorreta"));
        }

        logger.info("Login successful for: {}", email);
        return ResponseEntity.ok(Map.of("message", "Login realizado com sucesso", "role", user.getRole()));
    }
}