package com.health.user.controller;

import com.health.auth.dto.RegisterRequest;
import com.health.user.model.Role;
import com.health.auth.service.AuthService;
import com.health.exception.UnauthorizedRoleException;
import com.health.user.model.User;
import com.health.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UsersController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final AuthService authService;

    public UsersController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        return userService.getCurrentUser(authentication);
    }


    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//

    // Create user (SUPER_ADMIN can create any)
    @PostMapping("/user")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest user) {
        String currentRole = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority();

        if(currentRole.equals("ROLE_CLINIC_ADMIN") &&
                !(user.getRole().name().equals("DOCTOR") || user.getRole().name().equals("PATIENT"))) {
            throw new UnauthorizedRoleException(
                    "CLINIC_ADMIN can only create DOCTOR or PATIENT users. You tried to create " + user.getRole()
            );
        }

        return authService.register(user);
    }

    // Update user (SUPER_ADMIN can update any)
    @PutMapping("/user/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String currentRole = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return ResponseEntity.ok(
                userService.updateUser(id, user, currentEmail, Role.valueOf(currentRole))
        );
    }

    // Delete user (SUPER_ADMIN only)
    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // Only SUPER_ADMIN can see all users
    @GetMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Only SUPER_ADMIN can get a user by ID
    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
