package com.nexgen.bankingsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexgen.bankingsystem.util.EmailUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
        "ipstack.access-key=test-api-key"
})
public class IPDetectionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EmailUtil emailUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private IPDetectionService ipDetectionService;

    private static final String IPIFY_API_URL = "https://api.ipify.org";
    private static final String IPSTACK_API_URL_PREFIX = "http://api.ipstack.com/";

    @BeforeEach
    void setUp() {
    }

    @Test
    void fetchesExternalIPSuccessfully() {
        // Given
        String expectedIP = "178.0.238.173";
        lenient().when(restTemplate.getForObject(eq(IPIFY_API_URL), eq(String.class)))
                .thenReturn(expectedIP);

        // When
        String ip = ipDetectionService.getExternalIP();

        // Then
        assertThat(ip).isEqualTo(expectedIP);
        verify(restTemplate).getForObject(eq(IPIFY_API_URL), eq(String.class));
    }

    @Test
    void returnsDefaultIPWhenExternalAPIFails() {
        // Given
        lenient().when(restTemplate.getForObject(eq(IPIFY_API_URL), eq(String.class)))
                .thenThrow(new RestClientException("API unavailable"));

        // When
        String ip = ipDetectionService.getExternalIP();

        // Then
        assertThat(ip).isEqualTo("8.8.8.8");
        verify(restTemplate).getForObject(eq(IPIFY_API_URL), eq(String.class));
    }

    @Test
    void getsClientIPFromXForwardedHeader() {
        // Given
        String expectedIP = "178.0.238.173";
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(expectedIP + ",192.168.1.1");

        // When
        String ip = ipDetectionService.getClientIP(request);

        // Then
        assertThat(ip).isEqualTo(expectedIP);
        verify(request).getHeader("X-Forwarded-For");
    }

    @Test
    void fallsBackToRemoteAddrWhenNoHeader() {
        // Given
        String expectedIP = "84.164.246.196";
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn(expectedIP);

        // When
        String ip = ipDetectionService.getClientIP(request);

        // Then
        assertThat(ip).isEqualTo(expectedIP);
        verify(request).getHeader("X-Forwarded-For");
        verify(request).getRemoteAddr();
    }

    @Test
    void usesExternalIPWhenRemoteAddrIsLocalhost() {
        // Given
        String externalIP = "178.0.238.173";
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(restTemplate.getForObject(eq(IPIFY_API_URL), eq(String.class)))
                .thenReturn(externalIP);

        // When
        String ip = ipDetectionService.getClientIP(request);

        // Then
        assertThat(ip).isEqualTo(externalIP);
        verify(request).getHeader("X-Forwarded-For");
        verify(request).getRemoteAddr();
        verify(restTemplate).getForObject(eq(IPIFY_API_URL), eq(String.class));
    }

    @Test
    void retrievesLocationDetailsForValidIP178() throws Exception {
        // Given
        String ip = "178.0.238.173";
        String jsonResponse = "{\"success\":true,\"city\":\"Berlin\",\"region_name\":\"Berlin\",\"country_name\":\"Germany\",\"latitude\":52.5200,\"longitude\":13.4050}";
        String url = UriComponentsBuilder.fromHttpUrl(IPSTACK_API_URL_PREFIX + ip)
                .queryParam("access_key", "test-api-key")
                .toUriString();
        System.out.println("Expected URL for 178.0.238.173: " + url);
        JsonNode mockNode = new ObjectMapper().readTree(jsonResponse);
        lenient().when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);
        lenient().when(objectMapper.readTree(jsonResponse)).thenReturn(mockNode);

        // When
        String location = ipDetectionService.getLocationFromIP(ip);

        // Then
        assertThat(location).isEqualTo("City: Berlin, Region: Berlin, Country: Germany, Coordinates: 52.52,13.405");
        verify(restTemplate).getForObject(anyString(), eq(String.class));
        verify(objectMapper).readTree(jsonResponse);
    }

    @Test
    void retrievesLocationDetailsForValidIP84() throws Exception {
        // Given
        String ip = "84.164.246.196";
        String jsonResponse = "{\"success\":true,\"city\":\"Berlin\",\"region_name\":\"Berlin\",\"country_name\":\"Germany\",\"latitude\":52.51919937133789,\"longitude\":13.406100273132324}";
        String url = UriComponentsBuilder.fromHttpUrl(IPSTACK_API_URL_PREFIX + ip)
                .queryParam("access_key", "test-api-key")
                .toUriString();
        System.out.println("Expected URL for 84.164.246.196: " + url);
        JsonNode mockNode = new ObjectMapper().readTree(jsonResponse);
        lenient().when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);
        lenient().when(objectMapper.readTree(jsonResponse)).thenReturn(mockNode);

        // When
        String location = ipDetectionService.getLocationFromIP(ip);

        // Then
        assertThat(location).isEqualTo("City: Berlin, Region: Berlin, Country: Germany, Coordinates: 52.51919937133789,13.406100273132324");
        verify(restTemplate).getForObject(anyString(), eq(String.class));
        verify(objectMapper).readTree(jsonResponse);
    }

    @Test
    void returnsDefaultWhenLocationAPIReturnsNull() {
        // Given
        String ip = "178.0.238.173";
        String url = UriComponentsBuilder.fromHttpUrl(IPSTACK_API_URL_PREFIX + ip)
                .queryParam("access_key", "test-api-key")
                .toUriString();
        System.out.println("Expected URL: " + url);
        lenient().when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(null);

        // When
        String location = ipDetectionService.getLocationFromIP(ip);

        // Then
        assertThat(location).isEqualTo("Location not found");
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void returnsDefaultWhenLocationAPIFails() {
        // Given
        String ip = "178.0.238.173";
        String url = UriComponentsBuilder.fromHttpUrl(IPSTACK_API_URL_PREFIX + ip)
                .queryParam("access_key", "test-api-key")
                .toUriString();
        System.out.println("Expected URL: " + url);
        lenient().when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("API unavailable"));

        // When
        String location = ipDetectionService.getLocationFromIP(ip);

        // Then
        assertThat(location).isEqualTo("Location not found");
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void returnsDefaultWhenLocationAPIReportsError() throws Exception {
        // Given
        String ip = "178.0.238.173";
        String errorJson = "{\"success\":false,\"error\":{\"code\":101,\"type\":\"missing_access_key\",\"info\":\"You have not supplied an API Access Key.\"}}";
        String url = UriComponentsBuilder.fromHttpUrl(IPSTACK_API_URL_PREFIX + ip)
                .queryParam("access_key", "test-api-key")
                .toUriString();
        System.out.println("Expected URL: " + url);
        JsonNode mockNode = new ObjectMapper().readTree(errorJson);
        lenient().when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(errorJson);
        lenient().when(objectMapper.readTree(errorJson)).thenReturn(mockNode);

        // When
        String location = ipDetectionService.getLocationFromIP(ip);

        // Then
        assertThat(location).isEqualTo("Location not found");
        verify(restTemplate).getForObject(anyString(), eq(String.class));
        verify(objectMapper).readTree(errorJson);
    }

    @Test
    void sendsEmailWithLocationForIP178() throws Exception {
        // Given
        String ip = "178.0.238.173";
        String emailRecipient = "mn.de@outlook.com";
        String jsonResponse = "{\"success\":true,\"city\":\"Berlin\",\"region_name\":\"Berlin\",\"country_name\":\"Germany\",\"latitude\":52.5200,\"longitude\":13.4050}";
        String url = UriComponentsBuilder.fromHttpUrl(IPSTACK_API_URL_PREFIX + ip)
                .queryParam("access_key", "test-api-key")
                .toUriString();
        System.out.println("Expected URL for 178.0.238.173: " + url);
        JsonNode mockNode = new ObjectMapper().readTree(jsonResponse);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(ip);
        lenient().when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);
        lenient().when(objectMapper.readTree(jsonResponse)).thenReturn(mockNode);

        // When
        ipDetectionService.sendEmailWithLocation(request, emailRecipient);

        // Then
        String expectedLocation = "City: Berlin, Region: Berlin, Country: Germany, Coordinates: 52.52,13.405";
        String expectedBody = "A new login from IP address: " + ip + " was detected on your account.\nLocation: " + expectedLocation;
        verify(emailUtil).sendHtmlMessage(emailRecipient, "New Login Alert", expectedBody);
        verify(restTemplate).getForObject(anyString(), eq(String.class));
        verify(objectMapper).readTree(jsonResponse);
    }

    @Test
    void sendsEmailWithLocationForIP84() throws Exception {
        // Given
        String ip = "84.164.246.196";
        String emailRecipient = "mn.de@outlook.com";
        String jsonResponse = "{\"success\":true,\"city\":\"Berlin\",\"region_name\":\"Berlin\",\"country_name\":\"Germany\",\"latitude\":52.51919937133789,\"longitude\":13.406100273132324}";
        String url = UriComponentsBuilder.fromHttpUrl(IPSTACK_API_URL_PREFIX + ip)
                .queryParam("access_key", "test-api-key")
                .toUriString();
        System.out.println("Expected URL for 84.164.246.196: " + url);
        JsonNode mockNode = new ObjectMapper().readTree(jsonResponse);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(ip);
        lenient().when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(jsonResponse);
        lenient().when(objectMapper.readTree(jsonResponse)).thenReturn(mockNode);

        // When
        ipDetectionService.sendEmailWithLocation(request, emailRecipient);

        // Then
        String expectedLocation = "City: Berlin, Region: Berlin, Country: Germany, Coordinates: 52.51919937133789,13.406100273132324";
        String expectedBody = "A new login from IP address: " + ip + " was detected on your account.\nLocation: " + expectedLocation;
        verify(emailUtil).sendHtmlMessage(emailRecipient, "New Login Alert", expectedBody);
        verify(restTemplate).getForObject(anyString(), eq(String.class));
        verify(objectMapper).readTree(jsonResponse);
    }
}