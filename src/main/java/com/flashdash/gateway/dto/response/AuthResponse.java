package com.flashdash.gateway.dto.response;

public class AuthResponse {

    private String tokenType;
    private String accessToken;
    private int expiresIn;
    private String refreshToken;
    private int refreshExpiresIn;

    public AuthResponse(String tokenType, String accessToken, int expiresIn, String refreshToken, int refreshExpiresIn) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public int getRefreshExpiresIn() {
        return refreshExpiresIn;
    }
}
