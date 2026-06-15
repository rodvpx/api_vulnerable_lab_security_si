package com.security.lab.config;

import com.security.lab.model.User;
import com.security.lab.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setName("Administrador");
                admin.setEmail("admin@empresa.com");
                // Senhas devem ser hasheadas na versão segura
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);

                User user = new User();
                user.setName("Usuario Comum");
                user.setEmail("user@empresa.com");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRole("USER");
                userRepository.save(user);
                
                System.out.println("Usuários iniciais seguros inseridos no banco de dados.");
            }
        };
    }
}