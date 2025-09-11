package university.likelion.wmt.common.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import university.likelion.wmt.domain.user.entity.Role;

@Slf4j
@Component
public class JwtUtils {
    private static final String USER_ID = "USER_ID";
    private static final String USER_ROLE = "USER_ROLE";
    private static final long MILLI_SECOND = 1000L;

    private final SecretKey secretKey;
    private final long accessTokenSeconds;
    private final long refreshTokenSeconds;

    public JwtUtils(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
        this.accessTokenSeconds = jwtProperties.getAccessTokenSeconds();
        this.refreshTokenSeconds = jwtProperties.getRefreshTokenSeconds();
    }

    public Tokens generateTokens(JwtClaims claims) {
        String accessToken = generateAccessToken(claims);
        String refreshToken = generateRefreshToken();

        return new Tokens(accessToken, accessTokenSeconds, refreshToken, refreshTokenSeconds);
    }

    public JwtClaims parseToken(String accessToken) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(accessToken)
            .getPayload();

        return convertJwtClaims(claims);
    }

    public Optional<JwtClaims> getClaims(String accessToken) {
        try {
            return Optional.of(parseToken(accessToken));
        } catch (ExpiredJwtException e) {
            return Optional.of(convertJwtClaims(e.getClaims()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String generateAccessToken(JwtClaims claims) {
        Date now = new Date(System.currentTimeMillis());
        Date expired = new Date(now.getTime() + accessTokenSeconds * MILLI_SECOND);

        return Jwts.builder()
            .claims(generateClaims(claims))
            .issuedAt(now)
            .expiration(expired)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    private Map<String, Object> generateClaims(JwtClaims jwtClaims) {
        return Map.of(
            USER_ID, jwtClaims.userId(),
            USER_ROLE, jwtClaims.userRole().getKey());
    }

    private JwtClaims convertJwtClaims(Claims claims) {
        return new JwtClaims(claims.get(USER_ID, Long.class),
            Role.valueOf(claims.get(USER_ROLE, String.class)));
    }

}
