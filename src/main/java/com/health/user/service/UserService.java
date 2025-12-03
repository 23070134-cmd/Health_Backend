package com.health.user.service;

import com.health.user.model.Role;
import com.health.user.model.User;
import com.health.user.repository.UserRepository;
import com.health.exception.UnauthorizedRoleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//

    // get current user detail
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return ResponseEntity.ok(Map.of(
                "userId", user.getUserId(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "profileCompleted", user.getProfileCompleted() != null ? user.getProfileCompleted() : false
        ));
    }


    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//

    //get by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Delete user
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    public User updateUser(Long id, User userDetails, String currentUserEmail, Role currentUserRole) {
        User user = getUserById(id);

        // Role-based access control
        switch (currentUserRole) {
            case SUPER_ADMIN:
                // SUPER_ADMIN can update any user
                break;

            case CLINIC_ADMIN:
                // CLINIC_ADMIN can update only DOCTOR or PATIENT
                if (!(user.getRole() == Role.DOCTOR || user.getRole() == Role.PATIENT)) {
                    throw new UnauthorizedRoleException(
                            "CLINIC_ADMIN can only update DOCTOR or PATIENT users. You tried to update " + user.getRole()
                    );
                }
                break;

            case DOCTOR:
            case LAB_ADMIN:
            case PATIENT:
                // Can update only their own profile
                if (!user.getEmail().equals(currentUserEmail)) {
                    throw new UnauthorizedRoleException("You can only update your own profile.");
                }
                // Prevent changing role
                userDetails.setRole(user.getRole());
                break;

            default:
                throw new UnauthorizedRoleException("You do not have permission to update this user.");
        }

        // Update fields
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());

        // Only update password if it's not null/empty
        if (userDetails.getPasswordHash() != null && !userDetails.getPasswordHash().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(userDetails.getPasswordHash()));
        }

        // Update role (already adjusted for DOCTOR/PATIENT)
        user.setRole(userDetails.getRole());

        return userRepository.save(user);
    }
}
