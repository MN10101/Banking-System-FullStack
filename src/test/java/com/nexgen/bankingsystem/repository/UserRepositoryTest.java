package com.nexgen.bankingsystem.repository;

import com.nexgen.bankingsystem.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import java.util.Date;
import java.util.HashSet;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ImportAutoConfiguration(exclude = {com.nexgen.bankingsystem.service.UserServiceImpl.class})
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Create test users with data that satisfies entity constraints
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

        user2 = new User();
        user2.setEmail("mnde757@gmail.com");
        user2.setFirstName("Jonathan");
        user2.setLastName("Davies");
        user2.setAge(39);
        user2.setAddress("39 Park Avenue London SE82 5EY");
        user2.setBirthDate(new Date(85, 11, 1));
        user2.setPhoneNumber("+44123456789");
        user2.setPassword("securepass456");
        user2.setTaxNumber("B4582L251455");
        user2.setIdOrPassport("586974");
        user2.setLastKnownIP("84.164.246.196");
        user2.setEnabled(true);
        user2.setRoles(new HashSet<>());
        user2.getRoles().add("ROLE_USER");
        entityManager.persist(user2);

        entityManager.flush();
    }

    @Test
    void findsUserByExistingEmail() {
        // Given
        String email = "mn.de@outlook.com";

        // When
        User foundUser = userRepository.findByEmail(email);

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(email);
        assertThat(foundUser.getFirstName()).isEqualTo("Mahmoud");
        assertThat(foundUser.getLastName()).isEqualTo("Najmeh");
        assertThat(foundUser.getAge()).isEqualTo(39);
        assertThat(foundUser.getAddress()).isEqualTo("Müllenhoffstr.16, 10967 Berlin");
        assertThat(foundUser.getPhoneNumber()).isEqualTo("01639769764");
        assertThat(foundUser.getPassword()).isEqualTo("password123");
        assertThat(foundUser.getTaxNumber()).isEqualTo("MN12542BE22");
        assertThat(foundUser.getIdOrPassport()).isEqualTo("123456");
        assertThat(foundUser.getLastKnownIP()).isEqualTo("178.0.238.173");
        assertThat(foundUser.isEnabled()).isTrue();
        assertThat(foundUser.getRoles()).containsExactly("ROLE_USER");
        assertThat(foundUser.getAccounts()).isEmpty();
    }

    @Test
    void returnsNullForNonExistentEmail() {
        // Given
        String email = "nonexistent@example.com";

        // When
        User foundUser = userRepository.findByEmail(email);

        // Then
        assertThat(foundUser).isNull();
    }
}