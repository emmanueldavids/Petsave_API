package com.petsave.Utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    public String extractUserId(String token) {
        String jwt = token.replace("Bearer ", "");
        Claims claims = Jwts.parserBuilder()
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        return claims.getSubject(); // This is the FusionAuth userId
    }
}

