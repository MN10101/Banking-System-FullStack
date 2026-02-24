package com.nexgen.bankingsystem.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    @Autowired
    private JavaMailSender emailSender;

    public void sendHtmlMessage(String to, String subject, String htmlContent) {
        if (to == null || to.isEmpty()) {
            logger.error("Failed to send email: recipient address is missing.");
            return;
        }
        if (subject == null || subject.isEmpty()) {
            logger.error("Failed to send email: subject is missing.");
            return;
        }
        if (htmlContent == null || htmlContent.isEmpty()) {
            logger.error("Failed to send email: email content is missing.");
            return;
        }

        try {
            MimeMessagePreparator messagePreparator = mimeMessage -> {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
            };
            emailSender.send(messagePreparator);
            logger.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Error sending email to {}: {}", to, e.getMessage());
        }
    }
}
