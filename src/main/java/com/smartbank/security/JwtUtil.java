package com.smartbank.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "mysupersecretkeymysupersecretkey";
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // ✅ NEW: role is stored as a claim inside the JWT
    public static String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(key)
                .compact();
    }

    public static String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // ✅ NEW: extract role from token so Spring Security can read it
    public static String extractRole(String token) {
        return (String) getClaims(token).get("role");
    }

    private static Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}