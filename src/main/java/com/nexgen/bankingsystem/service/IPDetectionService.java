package com.nexgen.bankingsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.nexgen.bankingsystem.util.EmailUtil;

@Service
public class IPDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(IPDetectionService.class);

    // IPStack API URL without the access key, will be injected from properties
    private static final String IPIFY_API_URL = "https://api.ipify.org";

    // Inject IPStack API access key from application.properties
    @Value("${ipstack.access-key}")
    private String ipstackAccessKey;

    @Autowired
    private EmailUtil emailUtil;

    // Fetch external IP dynamically using ipify
    public String getExternalIP() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String ip = restTemplate.getForObject(IPIFY_API_URL, String.class);
            logger.info("External IP fetched: {}", ip);
            return ip;
        } catch (Exception e) {
            logger.error("Error fetching external IP: {}", e.getMessage());
            return "8.8.8.8";
        }
    }

    // Get client IP from HttpServletRequest, checking for possible proxies
    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null || xfHeader.isEmpty()) {
            String ip = request.getRemoteAddr();
            if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
                ip = getExternalIP();
            }
            logger.info("Client IP found using remote address: {}", ip);
            return ip;
        }

        String ip = xfHeader.split(",")[0];
        logger.info("Client IP found using X-Forwarded-For header: {}", ip);
        return ip;
    }

    // Get location information based on IP address
    public String getLocationFromIP(String ip) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            logger.info("Fetching location for IP: {}", ip);

            // Use injected access key for the IPStack API URL
            String url = UriComponentsBuilder.fromHttpUrl("http://api.ipstack.com/{ip}")
                    .queryParam("access_key", ipstackAccessKey)
                    .buildAndExpand(ip)
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            logger.info("Raw response from IP API: {}", response);

            if (response != null) {
                JsonNode jsonResponse = objectMapper.readTree(response);

                String city = jsonResponse.path("city").asText("Unknown City");
                String region = jsonResponse.path("region_name").asText("Unknown Region");
                String country = jsonResponse.path("country_name").asText("Unknown Country");
                String loc = jsonResponse.path("latitude").asText("Unknown Location") + "," +
                        jsonResponse.path("longitude").asText("Unknown Location");

                String locationInfo = String.format("City: %s, Region: %s, Country: %s, Coordinates: %s",
                        city, region, country, loc);
                logger.info("Location info for IP {}: {}", ip, locationInfo);

                return locationInfo;
            } else {
                logger.warn("Could not fetch location info for IP: {}", ip);
            }
        } catch (Exception e) {
            logger.error("Error fetching location info for IP: {}", ip, e);
        }

        return "Location not found";
    }

    // Example method to send email with location info
    public void sendEmailWithLocation(HttpServletRequest request, String emailRecipient) {
        String ip = getClientIP(request);
        String locationInfo = getLocationFromIP(ip);

        // Construct the email body
        String emailBody = "A new login from IP address: " + ip + " was detected on your account.\n"
                + "Location: " + locationInfo;

        // Use the EmailUtil to send the email
        emailUtil.sendHtmlMessage(emailRecipient, "New Login Alert", emailBody);
    }
}
