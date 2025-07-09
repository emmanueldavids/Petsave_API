package com.petsave.petsave.Utils;


import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final String jwtSecret = "j8KqQB7rdXeIF6QQhRjdgL6F2IZeAXFjAgNZF1/WBmY=";
    private final long jwtExpirationMs = 86400000; // 24 hours

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 604800000)) // 7 days
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }
    


    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(jwtSecret)
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    
}
