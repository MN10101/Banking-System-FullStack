package com.nexgen.bankingsystem.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IPDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(IPDetectionService.class);

    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            String ip = request.getRemoteAddr();
            logger.info("Client IP found using remote address: {}", ip);
            return ip;
        }

        String ip = xfHeader.split(",")[0];
        logger.info("Client IP found using X-Forwarded-For header: {}", ip);
        return ip;
    }
}
