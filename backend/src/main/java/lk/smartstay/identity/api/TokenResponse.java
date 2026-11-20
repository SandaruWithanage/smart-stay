package lk.smartstay.identity.api;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) { }
