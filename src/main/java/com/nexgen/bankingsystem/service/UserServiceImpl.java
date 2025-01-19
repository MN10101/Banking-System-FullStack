package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountService accountService;

    @Transactional
    @Override
    public User registerUser(User user) {
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Add a default role if none are specified
        if (user.getRoles().isEmpty()) {
            user.getRoles().add("ROLE_USER");
        }

        // Save user first
        User savedUser = userRepository.save(user);

        // Automatically create a default account with IBAN generation
        accountService.createAccount(savedUser, null, 0.0);

        return savedUser;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean deleteUserById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
