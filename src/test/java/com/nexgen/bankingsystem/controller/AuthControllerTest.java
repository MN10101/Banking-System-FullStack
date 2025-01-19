package com.nexgen.bankingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexgen.bankingsystem.entity.Account;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.entity.VerificationToken;
import com.nexgen.bankingsystem.service.*;
import com.nexgen.bankingsystem.util.EmailUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private VerificationService verificationService;

    @MockBean
    private EmailUtil emailUtil;

    @MockBean
    private IPDetectionService ipDetectionService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AccountService accountService;

    @Test
    public void testRegisterUser_Success() throws Exception {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Ho");
        user.setEmail("john@me.com");
        user.setPassword("password123");
        user.setAddress("123 Main St");
        user.setPhoneNumber("+1234567890");
        user.setBirthDate(new java.sql.Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 365 * 25));
        user.setAge(25);

        // Create a mock VerificationToken
        VerificationToken mockToken = new VerificationToken("someGeneratedToken", user, new java.sql.Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)); // Valid token with expiration

        Mockito.when(userService.findUserByEmail(user.getEmail())).thenReturn(Optional.empty());
        Mockito.when(userService.registerUser(Mockito.any(User.class))).thenReturn(user);
        Mockito.when(verificationService.createVerificationToken(user)).thenReturn(mockToken);
        Mockito.when(ipDetectionService.getClientIP(Mockito.any())).thenReturn("127.0.0.1");
        Mockito.when(ipDetectionService.getLocationFromIP("127.0.0.1")).thenReturn("Berlin, Germany");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully. Check your email for verification instructions."));
    }

    @Test
    public void testRegisterUser_EmailAlreadyInUse() throws Exception {
        User user = new User(
                "jon@me.com",
                "John",
                "Ho",
                30,
                "123 Main St",
                new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 365 * 25),
                "+1234567890",
                "password123",
                "123-45-6789",
                "ID1234567"
        );

        // Mock the service to return an existing email
        Mockito.when(userService.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Perform the POST request to register the user
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is already in use."));
    }



    @Test
    public void testVerifyUser_Success() throws Exception {
        String token = "validToken123";
        User user = new User();
        user.setEnabled(false);

        // Mock expiry date
        java.sql.Date expiryDate = new java.sql.Date(Date.from(Instant.now().plusSeconds(3600)).getTime()); // 1 hour in the future

        // Mock VerificationToken with expiry date
        Mockito.when(verificationService.getVerificationToken(token)).thenReturn(new VerificationToken(token, user, expiryDate));

        mockMvc.perform(get("/auth/verify")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Your account has been successfully verified!"));

        Mockito.verify(userService).saveUser(Mockito.any(User.class));
    }

    @Test
    public void testVerifyUser_InvalidToken() throws Exception {
        String token = "invalidToken";

        // Mock service to return null for invalid token
        Mockito.when(verificationService.getVerificationToken(token)).thenReturn(null);

        mockMvc.perform(get("/auth/verify").param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token."));
    }


    @Test
    public void testLoginUser_Success() throws Exception {
        String email = "john@me.com";
        String password = "password123";
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setLastKnownIP("127.0.0.1");

        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("12345678");
        account.setUser(user);

        user.setAccounts(List.of(account));

        Mockito.when(userService.findUserByEmail(email)).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        Mockito.when(ipDetectionService.getClientIP(Mockito.any())).thenReturn("127.0.0.1");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User logged in successfully."));
    }

    @Test
    public void testLoginUser_InvalidCredentials() throws Exception {
        String email = "john@me.com";
        String password = "wrongPassword";

        Mockito.when(userService.findUserByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }
}
