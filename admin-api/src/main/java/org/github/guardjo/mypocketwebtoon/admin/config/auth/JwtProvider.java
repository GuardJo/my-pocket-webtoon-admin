package org.github.guardjo.mypocketwebtoon.admin.config.auth;

import io.jsonwebtoken.Jwts;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.JwtProperties;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
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

        return Jwts.builder()
                .claim(USER_KEY, adminInfo.getId())
                .claim(USER_ROLE_KEY, adminInfo.getRole().getId())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }
}
