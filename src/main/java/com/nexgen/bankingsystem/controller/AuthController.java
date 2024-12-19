package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.entity.VerificationToken;
import com.nexgen.bankingsystem.service.*;
import com.nexgen.bankingsystem.util.EmailUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.nexgen.bankingsystem.service.IPDetectionService;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;


@CrossOrigin(origins = "http://localhost:3000")
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


    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


    private static final String IBAN_REGEX = "^DE\\d{20}$";

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user, HttpServletRequest request) {
        String clientIP = ipDetectionService.getClientIP(request);
        String locationInfo = ipDetectionService.getLocationFromIP(clientIP);
        logger.info("User registration request from IP: {} - Location: {}", clientIP, locationInfo);

        if (userService.findUserByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use.");
        }

        if (user.getAccounts() != null && !user.getAccounts().isEmpty()) {
            Account account = user.getAccounts().get(0);
            if (account.getIban() == null || account.getIban().isEmpty()) {
                return ResponseEntity.badRequest().body("IBAN cannot be null or empty.");
            }
            if (!Pattern.matches(IBAN_REGEX, account.getIban())) {
                return ResponseEntity.badRequest().body("IBAN must be a valid German IBAN starting with 'DE' and 22 characters long.");
            }
        }

        // Register the user
        User registeredUser = userService.registerUser(user);

        // Create verification token
        VerificationToken token = verificationService.createVerificationToken(registeredUser);

        // Create the verification URL
        String baseUrl = "http://localhost:3000";
        String verificationUrl = baseUrl + "/verify/" + token.getToken();

        // Create the HTML content for the email
        String htmlContent = "<p>Welcome " + user.getFirstName() + "!</p>" +
                "<p>Please verify your account by clicking the link below:</p>" +
                "<a href=\"" + verificationUrl + "\">Verify Here</a>" +
                "<p>This link will expire in 24 hours.</p>";

        // Send the email with HTML content
        emailUtil.sendHtmlMessage(user.getEmail(), "Account Verification", htmlContent);

        // Create an initial account for the registered user
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
        return ResponseEntity.ok(Map.of("message", "Your account has been successfully verified!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        String clientIP = ipDetectionService.getClientIP(request);
        String locationInfo = ipDetectionService.getLocationFromIP(clientIP);

        logger.info("Login attempt from IP: {} - Location: {}", clientIP, locationInfo);

        Optional<User> userOptional = userService.findUserByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                if (user.getLastKnownIP() == null || !user.getLastKnownIP().equals(clientIP)) {
                    notificationService.sendIPNotification(user.getEmail(), clientIP);
                    user.setLastKnownIP(clientIP);
                    userService.saveUser(user);
                }
                Account userAccount = user.getAccounts().get(0);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "User logged in successfully.");
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                response.put("accountNumber", userAccount.getAccountNumber());
                response.put("iban", userAccount.getIban());
                response.put("balance", userAccount.getBalance());
                response.put("currency", userAccount.getCurrency());
                response.put("email", user.getEmail());
                response.put("age", user.getAge());
                response.put("address", user.getAddress());
                response.put("phoneNumber", user.getPhoneNumber());
                response.put("birthDate", user.getBirthDate());
                response.put("taxNumber", user.getTaxNumber());
                response.put("idOrPassport", user.getIdOrPassport());

                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid email or password."));
    }

}
