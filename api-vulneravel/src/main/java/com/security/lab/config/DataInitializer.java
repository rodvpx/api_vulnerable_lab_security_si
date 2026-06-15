package com.security.lab.config;

import com.security.lab.model.User;
import com.security.lab.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadData(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setName("Administrador");
                admin.setEmail("admin@empresa.com");
                admin.setPassword("admin123");
                admin.setRole("ADMIN");
                userRepository.save(admin);

                User user = new User();
                user.setName("Usuario Comum");
                user.setEmail("user@empresa.com");
                user.setPassword("user123");
                user.setRole("USER");
                userRepository.save(user);
                
                System.out.println("Usuários iniciais inseridos no banco de dados.");
            }
        };
    }
}