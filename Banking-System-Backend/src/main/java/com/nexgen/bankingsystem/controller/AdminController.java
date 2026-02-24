package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    // This endpoint will be accessible only by users with 'ROLE_ADMIN'
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.findAllUsers());
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal server error while fetching users.");
        }
    }

    // This endpoint will also be accessible only by users with 'ROLE_ADMIN'
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            boolean isDeleted = userService.deleteUserById(id);
            if (isDeleted) {
                logger.info("User with id {} deleted successfully.", id);
                return ResponseEntity.ok("User deleted successfully.");
            } else {
                logger.warn("User with id {} not found.", id);
                return ResponseEntity.badRequest().body("User not found.");
            }
        } catch (Exception e) {
            logger.error("Error deleting user with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(500).body("Internal server error while deleting user.");
        }
    }
}
