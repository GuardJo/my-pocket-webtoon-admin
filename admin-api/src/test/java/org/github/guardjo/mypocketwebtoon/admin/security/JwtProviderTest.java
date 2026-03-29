package org.github.guardjo.mypocketwebtoon.admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.JwtProperties;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

class JwtProviderTest {
    private static final String SECRET = "test-secret-key-for-jwt-provider-unit-test-1234";
    private static final long EXPIRATION_MILLIS = 60_000L;

    private final JwtProvider jwtProvider = new JwtProvider(new JwtProperties(SECRET, EXPIRATION_MILLIS));
    private final SecretKey secretKey = new SecretKeySpec(
            SECRET.getBytes(StandardCharsets.UTF_8),
            Jwts.SIG.HS256.key().build().getAlgorithm()
    );

    @DisplayName("특정 관리자의 JWT 토큰 생성 및 토큰 검증")
    @Test
    void test_generateAccessToken_and_validateClaims() {
        AdminInfoEntity adminInfo = TestDataGenerator.adminInfoEntity("tester", "테스터");
        String accessToken = jwtProvider.generateAccessToken(adminInfo);

        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        assertThat(accessToken).isNotBlank();
        assertThat(claims.get("id", String.class)).isEqualTo(adminInfo.getId());
        assertThat(claims.get("role", String.class)).isEqualTo(adminInfo.getRole().getId());
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isEqualTo(EXPIRATION_MILLIS);
    }

    @DisplayName("유효한 JWT 토큰은 검증에 성공한다")
    @Test
    void test_validateAccessToken_success() {
        AdminInfoEntity adminInfo = TestDataGenerator.adminInfoEntity("tester", "테스터");
        String accessToken = jwtProvider.generateAccessToken(adminInfo);

        assertThatCode(() -> jwtProvider.validateAccessToken(accessToken))
                .doesNotThrowAnyException();
    }

    @DisplayName("유효하지 않은 JWT 토큰 검증 시 예외가 발생한다")
    @Test
    void test_validateAccessToken_fail_when_token_is_invalid() {
        assertThatThrownBy(() -> jwtProvider.validateAccessToken("invalid-token"))
                .isInstanceOf(JwtException.class)
                .hasMessage("인증 정보가 올바르지 않습니다.");
    }

    @DisplayName("JWT 토큰에서 관리자 식별키를 조회한다")
    @Test
    void test_getUserKey() {
        AdminInfoEntity adminInfo = TestDataGenerator.adminInfoEntity("tester", "테스터");
        String accessToken = jwtProvider.generateAccessToken(adminInfo);

        assertThat(jwtProvider.getUserKey(accessToken)).isEqualTo(adminInfo.getId());
    }

    @DisplayName("만료된 JWT 토큰 검증 시 예외가 발생한다")
    @Test
    void test_validateAccessToken_fail_when_token_is_expired() {
        AdminInfoEntity adminInfo = TestDataGenerator.adminInfoEntity("tester", "테스터");
        Date now = new Date();
        String expiredToken = Jwts.builder()
                .claim("id", adminInfo.getId())
                .claim("role", adminInfo.getRole().getId())
                .issuedAt(new Date(now.getTime() - 2_000L))
                .expiration(new Date(now.getTime() - 1_000L))
                .signWith(secretKey)
                .compact();

        assertThatThrownBy(() -> jwtProvider.validateAccessToken(expiredToken))
                .isInstanceOf(JwtException.class)
                .hasMessage("인증 정보가 올바르지 않습니다.");
    }
}
