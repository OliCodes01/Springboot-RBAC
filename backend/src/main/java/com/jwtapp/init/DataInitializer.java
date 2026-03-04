package com.jwtapp.init;

import com.jwtapp.entity.Role;
import com.jwtapp.entity.User;
import com.jwtapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        long userCount = userRepository.count();
        if (userCount > 0) {
            ensureRoles();
            log.info("Database already has {} users. Skipping initialization.", userCount);
            return;
        }

        userRepository.deleteAll();
        createMockUsers();
    }

    private void ensureRoles() {
        // Backfill roles for existing users on older databases.
        userRepository.findAll().forEach(user -> {
            if (user.getRole() == null) {
                user.setRole(Role.USER);
                userRepository.save(user);
            }
        });

        userRepository.findByUsername("john_doe").ifPresent(user -> {
            if (user.getRole() != Role.ADMIN) {
                user.setRole(Role.ADMIN);
                userRepository.save(user);
            }
        });
    }

    private void createMockUsers() {
        String encodedPassword = passwordEncoder.encode("password123");
       
        User[] mockUsers = {
            User.builder()
                    .username("john_doe")
                    .email("john@example.com")
                    .password(encodedPassword)
                    .firstName("John")
                    .lastName("Doe")
                    .phoneNumber("+1234567890")
                    .bio("Software developer passionate about Java and REST APIs")
                    .enabled(true)
                    .role(Role.ADMIN)
                    .build(),
            User.builder()
                    .username("jane_smith")
                    .email("jane@example.com")
                    .password(encodedPassword)
                    .firstName("Jane")
                    .lastName("Smith")
                    .phoneNumber("+0987654321")
                    .bio("Full-stack developer with expertise in Vue.js")
                    .enabled(true)
                    .role(Role.USER)
                    .build(),
            User.builder()
                    .username("alice_wonder")
                    .email("alice@example.com")
                    .password(encodedPassword)
                    .firstName("Alice")
                    .lastName("Wonder")
                    .phoneNumber("+1122334455")
                    .bio("DevOps engineer specializing in cloud infrastructure")
                    .enabled(true)
                    .role(Role.USER)
                    .build(),
            User.builder()
                    .username("bob_builder")
                    .email("bob@example.com")
                    .password(encodedPassword)
                    .firstName("Bob")
                    .lastName("Builder")
                    .phoneNumber("+5566778899")
                    .bio("Frontend developer with a love for CSS and animations")
                    .enabled(true)
                    .role(Role.USER)
                    .build(),
            User.builder()
                    .username("carol_tech")
                    .email("carol@example.com")
                    .password(encodedPassword)
                    .firstName("Carol")
                    .lastName("Tech")
                    .phoneNumber("+9988776655")
                    .bio("Database architect and performance optimization expert")
                    .enabled(true)
                    .role(Role.USER)
                    .build()
        };

        for (User user : mockUsers) {
            userRepository.save(user);
        }
    }
}
