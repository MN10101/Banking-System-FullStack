package com.nexgen.bankingsystem.controller;

import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getsAllUsers() {
        // Create mock User objects
        User user1 = new User("mn.de@outlook.com", "Mahmoud", "Najmeh", 33, "Müllenhoffstr.16, 10967 Berlin",
                Date.valueOf("1988-07-19"), "01639769764", "password1", "123456", "NEX324939864");
        User user2 = new User("mamocool3@gmail.com", "Mike", "Aradem", 45, "21 Harp Ave, Alton GU34 1TT, UK",
                Date.valueOf("1973-01-01"), "+441251522355", "password2", "123456", "NEX324939865");
        User user3 = new User("habibiva74@gmail.com", "Jonathan", "Davies", 36, "1445 Street London",
                Date.valueOf("1988-01-01"), "+441251521265", "password3", "123456", "NEX324939866");

        List<User> users = Arrays.asList(user1, user2, user3);

        // Mock UserService to return the list of users
        when(userService.findAllUsers()).thenReturn(users);

        // Call the endpoint
        ResponseEntity<?> response = adminController.getAllUsers();

        // Assertions
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(users, response.getBody());
    }

    @Test
    public void failsToGetUsersOnError() {
        // Mock UserService to throw an exception
        when(userService.findAllUsers()).thenThrow(new RuntimeException("Database error"));

        // Call the endpoint
        ResponseEntity<?> response = adminController.getAllUsers();

        // Assertions
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Internal server error while fetching users.", response.getBody());
    }

    @Test
    public void deletesUser() {
        Long userId = 1L;

        // Mock UserService to simulate successful deletion
        when(userService.deleteUserById(userId)).thenReturn(true);

        // Call the endpoint
        ResponseEntity<?> response = adminController.deleteUser(userId);

        // Assertions
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User deleted successfully.", response.getBody());
    }

    @Test
    public void failsToDeleteUserIfMissing() {
        Long userId = 1L;

        // Mock UserService to simulate that the user wasn't found
        when(userService.deleteUserById(userId)).thenReturn(false);

        // Call the endpoint
        ResponseEntity<?> response = adminController.deleteUser(userId);

        // Assertions
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User not found.", response.getBody());
    }

    @Test
    public void failsToDeleteUserOnError() {
        Long userId = 1L;

        // Mock UserService to simulate an exception
        when(userService.deleteUserById(userId)).thenThrow(new RuntimeException("Error"));

        // Call the endpoint
        ResponseEntity<?> response = adminController.deleteUser(userId);

        // Assertions
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Internal server error while deleting user.", response.getBody());
    }
}