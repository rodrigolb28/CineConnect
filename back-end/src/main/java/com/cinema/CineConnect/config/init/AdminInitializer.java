package com.cinema.CineConnect.config.init;

import com.cinema.CineConnect.model.Admin;
import com.cinema.CineConnect.model.User;
import com.cinema.CineConnect.repository.AuthRepository;
import com.cinema.CineConnect.repository.RoleRepository;
import com.cinema.CineConnect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthRepository authRepository;

    public AdminInitializer(UserRepository userRepository,
            RoleRepository roleRepository,
            BCryptPasswordEncoder passwordEncoder, BCryptPasswordEncoder bCryptPasswordEncoder,
            AuthRepository authRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authRepository = authRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (authRepository.findByEmail("admin@example.com").isPresent())
            return; // already exists
        var roleId = roleRepository.findRoleID("Admin");
        if (roleId.isEmpty()) {
            throw new DataIntegrityViolationException("Role does not exist");
        }

        var admin = new Admin(
                "Admin",
                bCryptPasswordEncoder.encode("SuperSecret123!"),
                "admin@example.com",
                "Admin",
                LocalDate.now()

        );

        userRepository.saveUser(admin, roleId.get());
        System.out.println("Admin user created!");
    }
}