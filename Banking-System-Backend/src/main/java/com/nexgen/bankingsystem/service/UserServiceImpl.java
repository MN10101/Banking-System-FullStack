package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountService accountService;

    @Transactional
    @Override
    public User registerUser(User user) {
        logger.info("Registering user: {}", user.getEmail());
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Add a default role if none are specified
        if (user.getRoles().isEmpty()) {
            user.getRoles().add("ROLE_USER");
            logger.debug("Added default ROLE_USER for user: {}", user.getEmail());
        }

        // Save user first
        User savedUser = userRepository.save(user);
        logger.info("User registered: {}", savedUser.getEmail());

        // Automatically create a default account with IBAN generation
        accountService.createAccount(savedUser, null, 0.0);
        logger.debug("Default account created for user: {}", savedUser.getEmail());

        return savedUser;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        Optional<User> user = Optional.ofNullable(userRepository.findByEmail(email));
        if (user.isEmpty()) {
            logger.warn("User not found for email: {}", email);
        } else {
            logger.debug("Found user for email: {}", email);
        }
        return user;
    }

    @Override
    public List<User> findAllUsers() {
        List<User> users = userRepository.findAll();
        logger.info("Retrieved {} users", users.size());
        return users;
    }

    @Override
    public boolean deleteUserById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            logger.info("User deleted: {}", id);
            return true;
        }
        logger.warn("User not found for deletion: {}", id);
        return false;
    }

    @Override
    public User saveUser(User user) {
        User savedUser = userRepository.save(user);
        logger.info("User saved: {}", savedUser.getEmail());
        return savedUser;
    }
}