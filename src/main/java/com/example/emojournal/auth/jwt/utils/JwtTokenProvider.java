package com.example.emojournal.auth.jwt.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스
 *
 * JWT(JSON Web Token) 기반 인증을 위한 토큰의 생성, 파싱, 검증 기능을 제공합니다.
 * HMAC SHA-512 알고리즘을 사용하여 토큰을 서명하고 검증합니다.
 *
 * 주요 기능:
 * - JWT 토큰 생성 (subject, 만료시간 설정)
 * - 토큰 유효성 검증 (서명, 만료시간 확인)
 * - 토큰에서 회원 ID 추출
 * - Claims 파싱 및 Subject 추출
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /** JWT 서명에 사용되는 비밀키 */
    private final Key key;

    /**
     * JwtTokenProvider 생성자
     *
     * application.yml의 jwt.secret-key 값을 BASE64 디코딩하여
     * HMAC SHA 키로 변환하여 초기화합니다.
     *
     * @param SECRET_KEY BASE64로 인코딩된 JWT 비밀키
     */
    public JwtTokenProvider(@Value("${jwt.secret-key}") String SECRET_KEY) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 토큰을 생성합니다.
     *
     * 주어진 subject(일반적으로 사용자 ID)와 만료시간을 이용하여
     * HMAC SHA-512로 서명된 JWT 토큰을 생성합니다.
     *
     * @param subject 토큰의 주체 (일반적으로 사용자 식별자)
     * @param expiredAt 토큰 만료시간
     * @return 생성된 JWT 토큰 문자열
     */
    public String generate(String subject, Date expiredAt) {
        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 액세스 토큰에서 회원 ID를 추출합니다.
     *
     * JWT 토큰의 subject 클레임에서 회원 ID를 추출하여 Long 타입으로 변환합니다.
     *
     * @param accessToken JWT 액세스 토큰
     * @return 추출된 회원 ID
     * @throws NumberFormatException subject를 Long으로 변환할 수 없는 경우
     */
    public Long extractMemberId(String accessToken) {
        return Long.valueOf(extractSubject(accessToken));
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     *
     * 토큰의 서명과 만료시간을 확인하여 유효성을 검증합니다.
     * 토큰이 유효하지 않거나 만료된 경우 false를 반환합니다.
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
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

    /**
     * 액세스 토큰에서 subject 클레임을 추출합니다.
     *
     * @param accessToken JWT 액세스 토큰
     * @return 토큰의 subject 클레임 값
     */
    private String extractSubject(String accessToken) {
        Claims claims = parseClaims(accessToken);
        return  claims.getSubject();
    }

    /**
     * JWT 토큰을 파싱하여 Claims를 추출합니다.
     *
     * 토큰이 만료된 경우에도 Claims를 반환하여
     * 만료된 토큰에서도 정보를 추출할 수 있도록 합니다.
     *
     * @param accessToken 파싱할 JWT 토큰
     * @return 파싱된 Claims 객체
     */
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
