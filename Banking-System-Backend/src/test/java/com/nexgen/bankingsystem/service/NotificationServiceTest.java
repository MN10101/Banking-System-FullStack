package com.nexgen.bankingsystem.service;

import com.nexgen.bankingsystem.util.EmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
        "ipstack.access-key=test-api-key"
})
public class NotificationServiceTest {

    @Mock
    private EmailUtil emailUtil;

    @Mock
    private IPDetectionService ipDetectionService;

    @InjectMocks
    private NotificationService notificationService;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        // Set up log appender to capture logs
        logAppender = new ListAppender<>();
        logAppender.start();
        Logger logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
        logger.addAppender(logAppender);
    }

    @Test
    void successfullySendsIPNotificationWithValidDetails() {
        // Given
        String email = "mn.de@outlook.com";
        String ip = "178.0.238.173";
        String location = "City: Berlin, Region: Berlin, Country: Germany, Coordinates: 52.52,13.405";
        when(ipDetectionService.getLocationFromIP(ip)).thenReturn(location);
        String expectedSubject = "New Login Detected";
        String expectedText = "A new login from IP address: " + ip + " was detected on your account.\nLocation: " + location;
        String expectedHtml = "<p>" + expectedText.replace("\n", "<br>") + "</p>";

        // When
        notificationService.sendIPNotification(email, ip);

        // Then
        verify(ipDetectionService).getLocationFromIP(ip);
        verify(emailUtil).sendHtmlMessage(email, expectedSubject, expectedHtml);
        verifyNoMoreInteractions(emailUtil, ipDetectionService);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Notification sent successfully to " + email);
    }

    @Test
    void skipsIPNotificationWithNullEmail() {
        // Given
        String ip = "178.0.238.173";

        // When
        notificationService.sendIPNotification(null, ip);

        // Then
        verifyNoInteractions(ipDetectionService, emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid email or IP address provided for IP notification.");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
    }

    @Test
    void skipsIPNotificationWithEmptyEmail() {
        // Given
        String ip = "178.0.238.173";

        // When
        notificationService.sendIPNotification("", ip);

        // Then
        verifyNoInteractions(ipDetectionService, emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid email or IP address provided for IP notification.");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
    }

    @Test
    void skipsIPNotificationWithNullIP() {
        // Given
        String email = "mn.de@outlook.com";

        // When
        notificationService.sendIPNotification(email, null);

        // Then
        verifyNoInteractions(ipDetectionService, emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid email or IP address provided for IP notification.");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
    }

    @Test
    void skipsIPNotificationWithEmptyIP() {
        // Given
        String email = "mn.de@outlook.com";

        // When
        notificationService.sendIPNotification(email, "");

        // Then
        verifyNoInteractions(ipDetectionService, emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid email or IP address provided for IP notification.");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
    }

    @Test
    void sendsIPNotificationWhenLocationNotFound() {
        // Given
        String email = "mn.de@outlook.com";
        String ip = "178.0.238.173";
        String location = "Location not found";
        when(ipDetectionService.getLocationFromIP(ip)).thenReturn(location);
        String expectedSubject = "New Login Detected";
        String expectedText = "A new login from IP address: " + ip + " was detected on your account.\nLocation: " + location;
        String expectedHtml = "<p>" + expectedText.replace("\n", "<br>") + "</p>";

        // When
        notificationService.sendIPNotification(email, ip);

        // Then
        verify(ipDetectionService).getLocationFromIP(ip);
        verify(emailUtil).sendHtmlMessage(email, expectedSubject, expectedHtml);
        verifyNoMoreInteractions(emailUtil, ipDetectionService);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Notification sent successfully to " + email);
    }

    @Test
    void successfullySendsBasicNotification() {
        // Given
        String email = "mn.de@outlook.com";
        String subject = "Test Subject";
        String text = "Test message\nLine two";
        String expectedHtml = "<p>Test message<br>Line two</p>";

        // When
        notificationService.sendNotification(email, subject, text);

        // Then
        verify(emailUtil).sendHtmlMessage(email, subject, expectedHtml);
        verifyNoMoreInteractions(emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Notification sent successfully to " + email);
    }

    @Test
    void skipsNotificationWithNullEmail() {
        // Given
        String subject = "Test Subject";
        String text = "Test message";

        // When
        notificationService.sendNotification(null, subject, text);

        // Then
        verifyNoInteractions(emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid notification details. Email, subject, or text is missing.");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
    }

    @Test
    void skipsNotificationWithEmptyEmail() {
        // Given
        String subject = "Test Subject";
        String text = "Test message";

        // When
        notificationService.sendNotification("", subject, text);

        // Then
        verifyNoInteractions(emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid notification details. Email, subject, or text is missing.");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
    }

    @Test
    void skipsNotificationWithNullSubject() {
        // Given
        String email = "mn.de@outlook.com";
        String text = "Test message";

        // When
        notificationService.sendNotification(email, null, text);

        // Then
        verifyNoInteractions(emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid notification details. Email, subject, or text is missing.");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
    }

    @Test
    void skipsNotificationWithEmptySubject() {
        // Given
        String email = "mn.de@outlook.com";
        String text = "Test message";

        // When
        notificationService.sendNotification(email, "", text);

        // Then
        verifyNoInteractions(emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid notification details. Email, subject, or text is missing.");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
    }

    @Test
    void skipsNotificationWithNullText() {
        // Given
        String email = "mn.de@outlook.com";
        String subject = "Test Subject";

        // When
        notificationService.sendNotification(email, subject, null);

        // Then
        verifyNoInteractions(emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid notification details. Email, subject, or text is missing.");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
    }

    @Test
    void skipsNotificationWithEmptyText() {
        // Given
        String email = "mn.de@outlook.com";
        String subject = "Test Subject";

        // When
        notificationService.sendNotification(email, subject, "");

        // Then
        verifyNoInteractions(emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Invalid notification details. Email, subject, or text is missing.");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.WARN);
    }

    @Test
    void logsErrorWhenEmailSendingFails() {
        // Given
        String email = "mn.de@outlook.com";
        String subject = "Test Subject";
        String text = "Test message";
        String expectedHtml = "<p>Test message</p>";
        doThrow(new RuntimeException("Email sending failed")).when(emailUtil)
                .sendHtmlMessage(email, subject, expectedHtml);

        // When
        notificationService.sendNotification(email, subject, text);

        // Then
        verify(emailUtil).sendHtmlMessage(email, subject, expectedHtml);
        verifyNoMoreInteractions(emailUtil);
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Failed to send notification to " + email + ": Email sending failed");
        assertThat(logAppender.list)
                .extracting(ILoggingEvent::getLevel)
                .contains(Level.ERROR);
    }
}