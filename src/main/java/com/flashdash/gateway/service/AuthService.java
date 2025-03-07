package com.flashdash.gateway.service;

import com.flashdash.gateway.dto.request.LoginRequest;
import com.flashdash.gateway.dto.request.RegisterRequest;
import com.flashdash.gateway.dto.response.AuthResponse;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class AuthService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin-client-id}")
    private String adminClientId;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm("master")
                .clientId(adminClientId)
                .username(adminUsername)
                .password(adminPassword)
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }

    public AuthResponse authenticate(LoginRequest loginRequest) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.PASSWORD)
                .username(loginRequest.getUsername())
                .password(loginRequest.getPassword())
                .build();

        AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();

        return new AuthResponse(
                tokenResponse.getTokenType(),
                tokenResponse.getToken(),
                tokenResponse.getExpiresIn(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getRefreshExpiresIn()
        );
    }

    public RegisterRequest register(RegisterRequest registerRequest) {
        Keycloak keycloak = getKeycloakInstance();
        UsersResource usersResource = keycloak.realm(realm).users();

        String userFrn = UUID.randomUUID().toString();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setAttributes(Collections.singletonMap("userFrn", Collections.singletonList(userFrn)));

        Response response = usersResource.create(user);
        if (response.getStatus() != 201) {
            throw new RuntimeException("User registration failed: " + response.getStatus());
        }

        String userId = usersResource.search(registerRequest.getUsername()).get(0).getId();

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(registerRequest.getPassword());

        usersResource.get(userId).resetPassword(passwordCred);

        emailVerification(userId);

        return registerRequest;
    }

    public void emailVerification(String userId) {
        Keycloak keycloak = getKeycloakInstance();
        UsersResource usersResource = keycloak.realm(realm).users();
        usersResource.get(userId).sendVerifyEmail();
    }
}
