package org.github.guardjo.mypocketwebtoon.admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.JwtProperties;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {
    private final static String USER_KEY = "id";
    private final static String USER_ROLE_KEY = "role";

    private final SecretKey secretKey;
    private final Long expirationMillis;

    public JwtProvider(JwtProperties jwtProperties) {
        this.secretKey = new SecretKeySpec(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
        this.expirationMillis = jwtProperties.expirationMillis();
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

        String token = Jwts.builder()
                .claim(USER_KEY, adminInfo.getId())
                .claim(USER_ROLE_KEY, adminInfo.getRole().getId())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();

        log.debug("Generated accessToken, adminId = {} expirationDate = {}, token = {}", adminInfo.getId(), expirationDate, token);

        return token;
    }

    /**
     * 주어진 토큰이 올바른 토큰인지 검증한다.
     *
     * @param token JWT 토큰
     * @throws JwtException 토큰 검증 실패
     */
    public void validateAccessToken(String token) throws JwtException {
        try {
            Claims claims = getClaims(token);
            Date now = new Date();
            if (now.after(claims.getExpiration())) {
                throw new JwtException("인증 기한이 만료되었습니다. 재로그인 하시길 바랍니다.");
            }
        } catch (Exception e) {
            throw new JwtException("인증 정보가 올바르지 않습니다.");
        }
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
}
