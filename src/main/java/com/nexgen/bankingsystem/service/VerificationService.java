package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.entity.User;
import com.nexgen.bankingsystem.entity.VerificationToken;
import com.nexgen.bankingsystem.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class VerificationService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    public VerificationToken createVerificationToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(calculateExpiryDate());
        return tokenRepository.save(token);
    }

    private Date calculateExpiryDate() {
        return Date.from(Instant.now().plusSeconds(86400));
    }

    public VerificationToken getVerificationToken(String token) {
        return tokenRepository.findByToken(token);
    }


}
