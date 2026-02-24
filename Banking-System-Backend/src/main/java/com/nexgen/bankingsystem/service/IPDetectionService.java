package com.nexgen.bankingsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexgen.bankingsystem.util.EmailUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class IPDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(IPDetectionService.class);
    private final RestTemplate restTemplate;
    private final EmailUtil emailUtil;
    private final ObjectMapper objectMapper;
    private final String ipstackAccessKey;

    public IPDetectionService(RestTemplate restTemplate, EmailUtil emailUtil, ObjectMapper objectMapper,
                              @Value("${ipstack.access-key}") String ipstackAccessKey) {
        this.restTemplate = restTemplate;
        this.emailUtil = emailUtil;
        this.objectMapper = objectMapper;
        this.ipstackAccessKey = ipstackAccessKey;
    }

    public String getExternalIP() {
        try {
            String ip = restTemplate.getForObject("https://api.ipify.org", String.class);
            logger.info("External IP fetched: {}", ip);
            return ip;
        } catch (RestClientException e) {
            logger.error("Error fetching external IP: {}", e.getMessage());
            return "8.8.8.8";
        }
    }

    public String getClientIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress != null && !ipAddress.isEmpty()) {
            ipAddress = ipAddress.split(",")[0].trim();
            logger.info("Client IP found using X-Forwarded-For header: {}", ipAddress);
            return ipAddress;
        }

        ipAddress = request.getRemoteAddr();
        if (ipAddress != null && (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1"))) {
            ipAddress = getExternalIP();
        }
        logger.info("Client IP found using remote address: {}", ipAddress);
        return ipAddress;
    }

    public String getLocationFromIP(String ip) {
        String url = UriComponentsBuilder.fromHttpUrl("http://api.ipstack.com/" + ip)
                .queryParam("access_key", ipstackAccessKey)
                .toUriString();
        logger.info("Fetching location for IP: {}", ip);
        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                logger.warn("No response from IP API for IP: {}", ip);
                return "Location not found";
            }

            logger.info("Raw response from IP API: {}", response);
            JsonNode jsonNode = objectMapper.readTree(response);
            if (!jsonNode.path("success").asBoolean(true)) {
                logger.warn("IP API returned error for IP {}: {}", ip, jsonNode.path("error").toString());
                return "Location not found";
            }

            String city = jsonNode.path("city").asText("Unknown City");
            String region = jsonNode.path("region_name").asText("Unknown Region");
            String country = jsonNode.path("country_name").asText("Unknown Country");
            String latitude = jsonNode.path("latitude").isMissingNode() ? "Unknown Location" : jsonNode.path("latitude").asText();
            String longitude = jsonNode.path("longitude").isMissingNode() ? "Unknown Location" : jsonNode.path("longitude").asText();

            String locationInfo = String.format("City: %s, Region: %s, Country: %s, Coordinates: %s,%s",
                    city, region, country, latitude, longitude);
            logger.info("Location info for IP {}: {}", ip, locationInfo);
            return locationInfo;
        } catch (Exception e) {
            logger.error("Error fetching location for IP {}: {}", ip, e.getMessage());
            return "Location not found";
        }
    }

    public void sendEmailWithLocation(HttpServletRequest request, String email) {
        String ip = getClientIP(request);
        String location = getLocationFromIP(ip);
        String message = String.format("A new login from IP address: %s was detected on your account.\nLocation: %s", ip, location);
        emailUtil.sendHtmlMessage(email, "New Login Alert", message);
    }
}