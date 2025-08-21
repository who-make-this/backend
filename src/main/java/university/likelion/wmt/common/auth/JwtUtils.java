package university.likelion.wmt.common.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
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

    public JwtUtils(@Value("${wmt.jwt.secret-key}") String secretKey,
        @Value("${wmt.jwt.access-token-seconds}") long accessTokenSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenSeconds = accessTokenSeconds;
    }

    public String generateJwtToken(JwtClaims claims) {
        Date now = new Date(System.currentTimeMillis());
        Date expired = new Date(now.getTime() + accessTokenSeconds * MILLI_SECOND);

        return Jwts.builder()
            .claims(generateClaims(claims))
            .issuedAt(now)
            .expiration(expired)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();
    }

    private Map<String, Object> generateClaims(JwtClaims jwtClaims) {
        return Map.of(
            USER_ID, jwtClaims.userId(),
            USER_ROLE, jwtClaims.userRole().getKey());
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

    private JwtClaims convertJwtClaims(Claims claims) {
        return new JwtClaims(claims.get(USER_ID, Long.class),
            Role.valueOf(claims.get(USER_ROLE, String.class)));
    }

}
