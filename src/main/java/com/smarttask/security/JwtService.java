package com.smarttask.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    private final String SECRET =
            "mysupersecretkeymysupersecretkeymysupersecretkey";

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    private final long EXPIRATION = 1000 * 60 * 15;

    public String generateToken(String username) {

        log.info("Generating JWT token for user={}", username);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();

        log.info("Token generated successfully for user={}", username);

        return token;
    }

    public String extractUsername(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            log.info("JWT token validated successfully");
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("JWT Token expired");
        } catch (Exception e) {
            log.error("Invalid JWT Token");
        }

        return false;
    }
}