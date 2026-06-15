package com.security.lab.controller;

import com.security.lab.model.User;
import com.security.lab.repository.UserRepository;
import com.security.lab.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Correção da Vuln 3: Uso de @Valid para garantir que as constraints na classe User sejam respeitadas
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        logger.info("Secure register attempt: {}", user.getEmail());
        
        if(userRepository.findByEmail(user.getEmail()).isPresent()) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "E-mail já está em uso"));
        }

        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso!"));
    }

    // Correção da Vuln 2: Mensagem genérica contra Username Enumeration
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        logger.info("Secure login attempt for email: {}", email);

        Optional<User> userOpt = userRepository.findByEmail(email);

        // Mensagem unificada independentemente do erro (Email não existe ou senha errada)
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            logger.warn("Invalid login attempt for: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciais inválidas"));
        }

        User user = userOpt.get();
        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        logger.info("Secure login successful for: {}", email);
        return ResponseEntity.ok(Map.of(
                "message", "Login realizado com sucesso", 
                "token", token
        ));
    }
}