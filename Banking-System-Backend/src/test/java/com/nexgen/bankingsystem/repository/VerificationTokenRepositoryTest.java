package com.nexgen.bankingsystem.repository;

import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.entity.VerificationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ImportAutoConfiguration(exclude = {com.nexgen.bankingsystem.service.VerificationService.class})
public class VerificationTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    private User user1;
    private VerificationToken token1;

    @BeforeEach
    void setUp() {
        // Create a test user with data that satisfies User entity constraints
        user1 = new User();
        user1.setEmail("mn.de@outlook.com");
        user1.setFirstName("Mahmoud");
        user1.setLastName("Najmeh");
        user1.setAge(39);
        user1.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user1.setBirthDate(new Date(85, 6, 19));
        user1.setPhoneNumber("01639769764");
        user1.setPassword("password123");
        user1.setTaxNumber("MN12542BE22");
        user1.setIdOrPassport("123456");
        user1.setLastKnownIP("178.0.238.173");
        user1.setEnabled(true);
        user1.setRoles(new HashSet<>());
        user1.getRoles().add("ROLE_USER");
        entityManager.persist(user1);

        // Create one verification token to respect @OneToOne constraint
        token1 = new VerificationToken();
        token1.setToken(UUID.randomUUID().toString());
        token1.setUser(user1);
        token1.setExpiryDate(Date.from(Instant.now().plusSeconds(86400)));
        entityManager.persist(token1);

        entityManager.flush();
    }

    @Test
    void findsTokenByExistingTokenValue() {
        // Given
        String token = token1.getToken();

        // When
        VerificationToken foundToken = tokenRepository.findByToken(token);

        // Then
        assertThat(foundToken).isNotNull();
        assertThat(foundToken.getToken()).isEqualTo(token);
        assertThat(foundToken.getUser().getEmail()).isEqualTo("mn.de@outlook.com");
        assertThat(foundToken.getExpiryDate()).isEqualTo(token1.getExpiryDate());
        assertThat(foundToken.isExpired()).isFalse();
    }

    @Test
    void returnsNullForNonExistentToken() {
        // Given
        String token = "nonexistent-token";

        // When
        VerificationToken foundToken = tokenRepository.findByToken(token);

        // Then
        assertThat(foundToken).isNull();
    }
}