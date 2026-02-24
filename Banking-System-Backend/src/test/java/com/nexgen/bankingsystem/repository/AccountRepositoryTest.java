package com.nexgen.bankingsystem.repository;

import com.nexgen.bankingsystem.entity.Account;
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
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@ImportAutoConfiguration(exclude = {com.nexgen.bankingsystem.service.AccountServiceImpl.class})
public class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    private User user1;
    private User user2;
    private Account account1;
    private Account account2;

    @BeforeEach
    void setUp() {
        // Create test users based on provided database data, without setting IDs
        user1 = new User();
        user1.setEmail("mn.de@outlook.com");
        user1.setFirstName("Mahmoud");
        user1.setLastName("Najmeh");
        user1.setAge(39);
        user1.setAddress("Müllenhoffstr.16, 10967 Berlin");
        user1.setBirthDate(new Date(85, 6, 19));
        user1.setPhoneNumber("01639769764");
        user1.setPassword("$2a$10$ss8P7V5PvKapGVK2stAGa.ewVNTTush.sBU6aaczzqtsgt2t.bsaW");
        user1.setTaxNumber("MN12542BE22");
        user1.setIdOrPassport("123456");
        user1.setLastKnownIP("178.0.238.173");
        user1.setEnabled(true);
        entityManager.persist(user1);

        user2 = new User();
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
        user2.setLastKnownIP("84.164.246.196");
        user2.setEnabled(true);
        entityManager.persist(user2);

        // Create test accounts based on provided database data, without setting IDs
        account1 = new Account();
        account1.setUser(user1);
        account1.setAccountNumber("NEX172720185");
        account1.setIban("DE54500202220172720185");
        account1.setBalance(35000.0);
        account1.setCurrency("EUR");
        entityManager.persist(account1);

        account2 = new Account();
        account2.setUser(user2);
        account2.setAccountNumber("NEX657984061");
        account2.setIban("DE52500202220657984061");
        account2.setBalance(5000.0);
        account2.setCurrency("EUR");
        entityManager.persist(account2);

        entityManager.flush();
    }

    @Test
    void findByAccountNumberReturnsAccountWhenAccountExists() {
        // Given
        String accountNumber = "NEX172720185";

        // When
        Optional<Account> foundAccount = accountRepository.findByAccountNumber(accountNumber);

        // Then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getAccountNumber()).isEqualTo(accountNumber);
        assertThat(foundAccount.get().getIban()).isEqualTo("DE54500202220172720185");
        assertThat(foundAccount.get().getBalance()).isEqualTo(35000.0);
        assertThat(foundAccount.get().getCurrency()).isEqualTo("EUR");
        assertThat(foundAccount.get().getUser().getEmail()).isEqualTo("mn.de@outlook.com");
    }

    @Test
    void findByAccountNumberReturnsEmptyWhenAccountDoesNotExist() {
        // Given
        String accountNumber = "NEX999999999";

        // When
        Optional<Account> foundAccount = accountRepository.findByAccountNumber(accountNumber);

        // Then
        assertThat(foundAccount).isNotPresent();
    }

    @Test
    void findByIbanReturnsAccountWhenIbanExists() {
        // Given
        String iban = "DE52500202220657984061";

        // When
        Optional<Account> foundAccount = accountRepository.findByIban(iban);

        // Then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getIban()).isEqualTo(iban);
        assertThat(foundAccount.get().getAccountNumber()).isEqualTo("NEX657984061");
        assertThat(foundAccount.get().getBalance()).isEqualTo(5000.0);
        assertThat(foundAccount.get().getCurrency()).isEqualTo("EUR");
        assertThat(foundAccount.get().getUser().getEmail()).isEqualTo("mnde757@gmail.com");
    }

    @Test
    void findByIbanReturnsEmptyWhenIbanDoesNotExist() {
        // Given
        String iban = "DE99999999999999999999";

        // When
        Optional<Account> foundAccount = accountRepository.findByIban(iban);

        // Then
        assertThat(foundAccount).isNotPresent();
    }
}
