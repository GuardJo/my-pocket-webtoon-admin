package org.github.guardjo.mypocketwebtoon.admin.config.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.JwtProperties;
import org.github.guardjo.mypocketwebtoon.admin.model.domain.AdminInfoEntity;
import org.github.guardjo.mypocketwebtoon.admin.util.TestDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;

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
}
