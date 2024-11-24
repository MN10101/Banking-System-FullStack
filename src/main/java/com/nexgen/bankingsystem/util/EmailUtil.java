package com.nexgen.bankingsystem.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text) {
        // Validate input parameters
        if (to == null || to.isEmpty()) {
            logger.error("Failed to send email: recipient address is missing.");
            return;
        }
        if (subject == null || subject.isEmpty()) {
            logger.error("Failed to send email: subject is missing.");
            return;
        }
        if (text == null || text.isEmpty()) {
            logger.error("Failed to send email: email text is missing.");
            return;
        }

        try {
            // Create and send the email message
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            emailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Error sending email to {}: {}", to, e.getMessage());
        }
    }
}
