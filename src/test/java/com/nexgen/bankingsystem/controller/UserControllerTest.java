package com.nexgen.bankingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.service.IPDetectionService;
import com.nexgen.bankingsystem.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private IPDetectionService ipDetectionService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User user;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        // Initialize a sample user based on database data
        user = new User();
        user.setId(1L);
        user.setEmail("mn.de@outlook.com");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(39);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user.setBirthDate(new Date(85, 6, 19));
        user.setPhoneNumber("01639769764");
        user.setPassword("$2a$10$ss8P7V5PvKapGVK2stAGa.ewVNTTush.sBU6aaczzqtsgt2t.bsaW");
        user.setTaxNumber("MN12542BE22");
        user.setIdOrPassport("123456");
        user.setEnabled(false);
        user.setRoles(new HashSet<>(Collections.singletonList("ROLE_USER")));
    }

    @Test
    public void registersUser() throws Exception {
        // Arrange
        User newUser = new User();
        newUser.setEmail("new.user@example.com");
        newUser.setFirstName("John");
        newUser.setLastName("Doe");
        newUser.setAge(25);
        newUser.setAddress("123 Main St");
        newUser.setBirthDate(new Date(95, 0, 1));
        newUser.setPhoneNumber("+1234567890");
        newUser.setPassword("password123");
        newUser.setTaxNumber("TAX123");
        newUser.setIdOrPassport("ID123");

        when(ipDetectionService.getClientIP(any(HttpServletRequest.class))).thenReturn("127.0.0.1");
        when(ipDetectionService.getLocationFromIP("127.0.0.1")).thenReturn("Localhost");
        when(userService.registerUser(any(User.class))).thenReturn(newUser);

        String requestJson = objectMapper.writeValueAsString(newUser);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new.user@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    public void failsToRegisterIfInvalidUser() throws Exception {
        // Arrange
        User invalidUser = new User();
        invalidUser.setEmail("invalid-email");
        invalidUser.setFirstName("");
        invalidUser.setAge(16);
        invalidUser.setAddress("123 Main St");
        invalidUser.setBirthDate(new Date());
        invalidUser.setPhoneNumber("123");
        invalidUser.setPassword("short");

        String requestJson = objectMapper.writeValueAsString(invalidUser);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void getsUserById() throws Exception {
        // Arrange
        when(userService.findAllUsers()).thenReturn(Collections.singletonList(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("mn.de@outlook.com"))
                .andExpect(jsonPath("$.firstName").value("Mahmoud"))
                .andExpect(jsonPath("$.lastName").value("Najmeh"));
    }

    @Test
    public void failsToGetUserIfIdNotFound() throws Exception {
        // Arrange
        when(userService.findAllUsers()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getsAllUsers() throws Exception {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("mnde757@gmail.com");
        user2.setFirstName("Jonathan");
        user2.setLastName("Davies");
        user2.setAge(39);
        user2.setAddress("39 Park Avenue London SE82 5EY");
        user2.setBirthDate(new Date(85, 11, 1));
        user2.setPhoneNumber("+44123456789");
        user2.setPassword("$2a$10$9LTPTr/soRiN7ynBMVl64eWzwuFahaJ5r2ZbkbGRkvHsR/pbAxupq");
        user2.setTaxNumber("B4582L251455");
        user2.setIdOrPassport("586974");
        user2.setEnabled(false);
        user2.setRoles(new HashSet<>(Collections.singletonList("ROLE_USER")));

        when(userService.findAllUsers()).thenReturn(Arrays.asList(user, user2));

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email").value("mn.de@outlook.com"))
                .andExpect(jsonPath("$[1].email").value("mnde757@gmail.com"));
    }

    @Test
    public void updatesUser() throws Exception {
        // Arrange
        User updatedUser = new User();
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("Updated");
        updatedUser.setLastName("User");
        updatedUser.setAge(30);
        updatedUser.setAddress("456 New St");
        updatedUser.setBirthDate(new Date(90, 0, 1));
        updatedUser.setPhoneNumber("+9876543210");
        updatedUser.setPassword("newpassword123");
        updatedUser.setTaxNumber("TAX456");
        updatedUser.setIdOrPassport("ID456");
        updatedUser.setEnabled(true);

        when(userService.findAllUsers()).thenReturn(Collections.singletonList(user));
        when(userService.saveUser(any(User.class))).thenReturn(updatedUser);

        String requestJson = objectMapper.writeValueAsString(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    public void failsToUpdateIfIdNotFound() throws Exception {
        // Arrange
        User validUser = new User();
        validUser.setEmail("updated@example.com");
        validUser.setFirstName("Updated");
        validUser.setLastName("User");
        validUser.setAge(30);
        validUser.setAddress("456 New St");
        validUser.setBirthDate(new Date(90, 0, 1));
        validUser.setPhoneNumber("+9876543210");
        validUser.setPassword("newpassword123");

        when(userService.findAllUsers()).thenReturn(Collections.emptyList());

        String requestJson = objectMapper.writeValueAsString(validUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void failsToUpdateIfInvalidUser() throws Exception {
        // Arrange
        User invalidUser = new User();
        invalidUser.setEmail("invalid-email");
        invalidUser.setFirstName("");
        invalidUser.setAge(16);

        String requestJson = objectMapper.writeValueAsString(invalidUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    public void deletesUser() throws Exception {
        // Arrange
        when(userService.deleteUserById(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void failsToDeleteIfIdNotFound() throws Exception {
        // Arrange
        when(userService.deleteUserById(999L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getsUserByEmail() throws Exception {
        // Arrange
        when(userService.findUserByEmail("mn.de@outlook.com")).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/email/mn.de@outlook.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mn.de@outlook.com"))
                .andExpect(jsonPath("$.firstName").value("Mahmoud"))
                .andExpect(jsonPath("$.lastName").value("Najmeh"));
    }

    @Test
    public void failsToGetUserIfEmailNotFound() throws Exception {
        // Arrange
        when(userService.findUserByEmail("nexgin.bank@gmail.com")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/email/nexgin.bank@gmail.com"))
                .andExpect(status().isNotFound());
    }
}