package com.cinema.CineConnect.service;

import com.cinema.CineConnect.model.*;
import com.cinema.CineConnect.model.DTO.AdminRegistrationRequestRecord;
import com.cinema.CineConnect.model.DTO.RegistrationRequestRecord;
import com.cinema.CineConnect.model.DTO.UserRecordRoleId;
import com.cinema.CineConnect.repository.AuthRepository;
import com.cinema.CineConnect.repository.RoleRepository;
import com.cinema.CineConnect.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationService {

    private final AuthRepository authRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public RegistrationService(AuthRepository authRepository, RoleRepository roleRepository,
            UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.authRepository = authRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Boolean verifyIfUserExists(String email) {
        return authRepository.findByEmail(email).isEmpty();
    }

    public void createClient(RegistrationRequestRecord registrationRequestRecord) {
        var client = new Client(
                registrationRequestRecord.name(),
                bCryptPasswordEncoder.encode(registrationRequestRecord.password()),
                registrationRequestRecord.email(),
                "Client",
                registrationRequestRecord.birth_date());
        if (!(verifyIfUserExists(registrationRequestRecord.email()))) {
            throw new DataIntegrityViolationException("An user with this email already exists");
        }
        var roleId = roleRepository.findRoleID(client.getRole());
        if (roleId.isEmpty()) {
            throw new DataIntegrityViolationException("Role does not exist");
        }
        userRepository.saveUser(client, roleId.get());
    }

    private User createUserObject(AdminRegistrationRequestRecord adminRegistrationRequestRecord) {

        return switch (adminRegistrationRequestRecord.role()) {
            case "Client" -> new Client(
                    adminRegistrationRequestRecord.name(),
                    bCryptPasswordEncoder.encode(adminRegistrationRequestRecord.password()),
                    adminRegistrationRequestRecord.email(),
                    adminRegistrationRequestRecord.role(),
                    adminRegistrationRequestRecord.birth_date());
            case "Admin" -> new Admin(
                    adminRegistrationRequestRecord.name(),
                    bCryptPasswordEncoder.encode(adminRegistrationRequestRecord.password()),
                    adminRegistrationRequestRecord.email(),
                    adminRegistrationRequestRecord.role(),
                    adminRegistrationRequestRecord.birth_date()

                );
            case "Employee" -> new Employee(
                    adminRegistrationRequestRecord.name(),
                    bCryptPasswordEncoder.encode(adminRegistrationRequestRecord.password()),
                    adminRegistrationRequestRecord.email(),
                    adminRegistrationRequestRecord.role(),
                    adminRegistrationRequestRecord.birth_date());
            case "Cashier" -> new Cashier(
                    adminRegistrationRequestRecord.name(),
                    bCryptPasswordEncoder.encode(adminRegistrationRequestRecord.password()),
                    adminRegistrationRequestRecord.email(),
                    adminRegistrationRequestRecord.role(),
                    adminRegistrationRequestRecord.birth_date()

                );
            case "Manager" -> new Manager(
                    adminRegistrationRequestRecord.name(),
                    bCryptPasswordEncoder.encode(adminRegistrationRequestRecord.password()),
                    adminRegistrationRequestRecord.email(),
                    adminRegistrationRequestRecord.role(),
                    adminRegistrationRequestRecord.birth_date()

                );
            default -> throw new IllegalArgumentException("Invalid role");
        };
    }

    public void adminCreateAndSaveUser(AdminRegistrationRequestRecord adminRegistrationRequestRecord) {
        var user = createUserObject(adminRegistrationRequestRecord);

        if (!(verifyIfUserExists(adminRegistrationRequestRecord.email()))) {
            throw new DataIntegrityViolationException("An user with this email already exists");
        }
        var roleId = roleRepository.findRoleID(user.getRole());
        if (roleId.isEmpty()) {
            throw new DataIntegrityViolationException("Role does not exist");
        }
        userRepository.saveUser(user, roleId.get());

    }
}