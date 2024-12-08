package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    Optional<User> findUserByEmail(String email);
    List<User> findAllUsers();
    boolean deleteUserById(Long id);
    User saveUser(User user);
}
