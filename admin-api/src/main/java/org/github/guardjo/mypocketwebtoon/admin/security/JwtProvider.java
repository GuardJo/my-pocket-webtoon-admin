package org.github.guardjo.mypocketwebtoon.admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.JwtProperties;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.model.vo.AdminInfo;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {
    private final static String USER_KEY = "id";
    private final static String USER_ROLE_KEY = "role";

    private final SecretKey secretKey;
    private final long expirationMillis;
    private final long expirationTempMillis;

    public JwtProvider(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = jwtProperties.expirationMillis();
        this.expirationTempMillis = jwtProperties.expirationTempMillis();
    }

    /**
     * 주어진 관리자 계정 Entity 데이터를 기반으로 JWT 토큰을 생성하여 반환한다.
     *
     * @param adminInfo 관리자 계정 Entity
     * @return JWT 토큰
     */
    public String generateAccessToken(AdminInfoEntity adminInfo) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMillis);

        String token = createJwtToken(adminInfo.getId(), adminInfo.getRole().getId(), now, expirationDate);

        log.debug("Generated accessToken, adminId = {} expirationDate = {}, token = {}", adminInfo.getId(), expirationDate, token);

        return token;
    }

    /**
     * 주어진 계정 정보를 기반으로 짧은 유효기간을 지닌 임시 인증 토큰을 생성하여 반환한다.
     *
     * @param adminInfo 계정 정보
     * @return JWT 토큰
     */
    public String generateTemporarilyAccessToken(AdminInfo adminInfo) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTempMillis);

        String token = createJwtToken(adminInfo.id(), adminInfo.roleId(), now, expirationDate);

        log.debug("Generated temporarily accessToken, adminId = {} expirationDate = {}, token = {}", adminInfo.id(), expirationDate, token);

        return token;
    }

    /**
     * 주어진 토큰에 해당하는 관리자 계정 식벽키를 반환한다.
     *
     * @param token JWT 토큰
     * @return 관리자 계정 식별키
     */
    public String getUserKey(String token) {
        return getClaims(token).get(USER_KEY, String.class);
    }

    /*
    토큰 내 Claims 반환
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /*
    JWT 토큰 생성
     */
    private String createJwtToken(String userKey, String userRoleKey, Date now, Date expirationDate) {
        return Jwts.builder()
                .claim(USER_KEY, userKey)
                .claim(USER_ROLE_KEY, userRoleKey)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }
}
