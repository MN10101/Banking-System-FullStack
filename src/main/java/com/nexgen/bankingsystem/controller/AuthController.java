package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.entity.VerificationToken;
import com.nexgen.bankingsystem.service.*;
import com.nexgen.bankingsystem.util.EmailUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private IPDetectionService ipDetectionService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (userService.findUserByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use.");
        }
        User registeredUser = userService.registerUser(user);
        VerificationToken token = verificationService.createVerificationToken(registeredUser);
        emailUtil.sendSimpleMessage(user.getEmail(), "Account Verification",
                "Please verify your account using this token: " + token.getToken());

        // Automatically create a default account for the user
        accountService.createAccount(registeredUser, null, 0.0);

        return ResponseEntity.ok("User registered successfully. Check your email for verification instructions.");
    }


    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String token) {
        VerificationToken verificationToken = verificationService.getVerificationToken(token);
        if (verificationToken == null || verificationToken.getExpiryDate().before(new Date())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired verification token."));
        }
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userService.saveUser(user);
        return ResponseEntity.ok(Map.of("message", "User verified successfully."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String email, @RequestParam String password, HttpServletRequest request) {
        Optional<User> userOptional = userService.findUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                String clientIP = ipDetectionService.getClientIP(request);
                notificationService.sendIPNotification(user.getEmail(), clientIP);
                return ResponseEntity.ok(Map.of("message", "User logged in successfully.", "email", user.getEmail()));
            }
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid email or password."));
    }
}
