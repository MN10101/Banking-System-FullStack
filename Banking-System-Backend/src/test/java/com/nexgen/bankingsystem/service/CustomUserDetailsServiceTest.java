package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() throws ParseException {
        // Initialize a valid User object based on provided data (User 1)
        user = new User();
        user.setEmail("mn.de@outlook.com");
        user.setPassword("$2a$10$ss8P7V5PvKapGVK2stAGa.ewVNTTush.sBU6aaczzqtsgt2t.bsaW");
        user.setFirstName("Mahmoud");
        user.setLastName("Najmeh");
        user.setAge(40);
        user.setAddress("Müllenhoffstr.16, 10967 Berlin");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        user.setBirthDate(sdf.parse("1985-07-19 02:00:00"));
        user.setPhoneNumber("01639769764");
        user.setTaxNumber("MN12542BE22");
        user.setIdOrPassport("123456");
        user.setLastKnownIP("178.0.238.173");
        user.setEnabled(true);
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        user.setRoles(roles);
    }

    @Test
    void loadsEnabledUserDetailsSuccessfully() {
        // Given
        String email = "mn.de@outlook.com";
        when(userRepository.findByEmail(email)).thenReturn(user);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("mn.de@outlook.com");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$ss8P7V5PvKapGVK2stAGa.ewVNTTush.sBU6aaczzqtsgt2t.bsaW");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        // Fix AssertJ error by extracting authority strings
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void throwsExceptionWhenUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(null);

        // When/Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername(email));
        assertThat(exception.getMessage()).isEqualTo("User not found with email: nonexistent@example.com");
    }

    @Test
    void throwsExceptionWhenUserAccountDisabled() {
        // Given
        String email = "mn.de@outlook.com";
        user.setEnabled(false);
        when(userRepository.findByEmail(email)).thenReturn(user);

        // When/Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername(email));
        assertThat(exception.getMessage()).isEqualTo("User account is not enabled.");
    }

    @Test
    void assignsDefaultRoleWhenNoRolesSpecified() {
        // Given
        String email = "mn.de@outlook.com";
        user.setRoles(null);
        when(userRepository.findByEmail(email)).thenReturn(user);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void mapsMultipleRolesToAuthorities() {
        // Given
        String email = "mn.de@outlook.com";
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        roles.add("ADMIN");
        user.setRoles(roles);
        when(userRepository.findByEmail(email)).thenReturn(user);

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}