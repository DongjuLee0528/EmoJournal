package com.example.emojournal.auth.jwt.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String SECRET_KEY) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generate(String subject, Date expiredAt) {
        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Long extractMemberId(String accessToken) {
        return Long.valueOf(extractSubject(accessToken));
    }

    public boolean validateToken(String token) {
        try {
            log.info("validateToken");
            log.info(token);
            // 토큰 만료되었는지 확인 하는 코드
            Jwts.parserBuilder().setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }catch (JwtException e) {
            log.warn(e.toString());
            return false;
        }
    }

    private String extractSubject(String accessToken) {
        Claims claims = parseClaims(accessToken);
        return  claims.getSubject();
    }

    private Claims parseClaims(String accessToken) {
        try {

            log.info("parseClaims : " + accessToken);

            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();

        } catch (ExpiredJwtException e) {
            return  e.getClaims();
        }
    }

}
