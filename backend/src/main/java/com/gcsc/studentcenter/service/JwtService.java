package com.gcsc.studentcenter.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiresMinutes;

    public JwtService(
        @Value("${security.jwt.secret}") String secret,
        @Value("${security.jwt.expires-minutes:120}") long expiresMinutes
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiresMinutes = expiresMinutes;
    }

    public String generateToken(String username, String displayName) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(username)
            .claim("displayName", displayName)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expiresMinutes, ChronoUnit.MINUTES)))
            .signWith(secretKey)
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
