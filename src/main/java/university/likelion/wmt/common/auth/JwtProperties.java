package university.likelion.wmt.common.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "wmt.jwt")
public class JwtProperties {
    private final String secretKey;
    private final long accessTokenSeconds;
    private final long refreshTokenSeconds;
}
