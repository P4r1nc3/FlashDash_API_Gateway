package com.flashdash.gateway.service;

import com.flashdash.gateway.dto.request.RegisterRequest;
import com.flashdash.gateway.dto.response.AuthResponse;
import com.flashdash.gateway.dto.request.LoginRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    public AuthService() {
        this.restTemplate = new RestTemplate();
    }

    public AuthResponse authenticate(LoginRequest loginRequest) {
        logger.info("Attempting to authenticate user: {}", loginRequest.getUsername());

        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", loginRequest.getUsername());
        form.add("password", loginRequest.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, requestEntity, Map.class);

        logger.info("User {} successfully authenticated, token type: {}",
                loginRequest.getUsername(), response.getBody().get("token_type"));

        return new AuthResponse(
                (String) response.getBody().get("token_type"),
                (String) response.getBody().get("access_token"),
                (Integer) response.getBody().get("expires_in"),
                (String) response.getBody().get("refresh_token"),
                (Integer) response.getBody().get("refresh_expires_in")
        );
    }

    public void register(RegisterRequest registerRequest) {
        logger.info("Registering user: {}", registerRequest.getUsername());

        String keycloakAdminUrl = keycloakUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getAdminAccessToken());

        String userFrn = UUID.randomUUID().toString();

        Map<String, Object> user = new HashMap<>();
        user.put("username", registerRequest.getUsername());
        user.put("email", registerRequest.getEmail());
        user.put("firstName", registerRequest.getFirstName());
        user.put("lastName", registerRequest.getLastName());
        user.put("enabled", true);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("userFrn", userFrn);
        user.put("attributes", attributes);

        Map<String, String> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", registerRequest.getPassword());
        credentials.put("temporary", "false");

        user.put("credentials", List.of(credentials));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(user, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(keycloakAdminUrl, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            logger.info("User {} registered successfully with userFrn {}", registerRequest.getUsername(), userFrn);
        } else {
            throw new RuntimeException("User registration failed: " + response.getBody());
        }
    }

    private String getAdminAccessToken() {
        String tokenUrl = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", "admin-cli");
        form.add("username", "admin");
        form.add("password", "admin");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(form, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return (String) response.getBody().get("access_token");
        } else {
            throw new RuntimeException("Failed to obtain admin token");
        }
    }
}
