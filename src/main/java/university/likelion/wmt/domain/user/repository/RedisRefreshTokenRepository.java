package university.likelion.wmt.domain.user.repository;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import university.likelion.wmt.common.auth.JwtProperties;

@Repository
public class RedisRefreshTokenRepository implements RefreshTokenRepository {
    private static final String KEY_PREFIX = "auth:refresh";
    private static final String INDEX_PREFIX = "auth:uid";

    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOperations;
    private final SetOperations<String, String> setOperations;

    private final JwtProperties jwtProperties;

    public RedisRefreshTokenRepository(RedisTemplate<String, String> redisTemplate, JwtProperties jwtProperties) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.setOperations = redisTemplate.opsForSet();

        this.jwtProperties = jwtProperties;
    }

    @Override
    public void save(final Long userId, final String refreshToken) {
        String tokenKey = getTokenKey(refreshToken);
        String indexKey = getIndexKey(userId);

        redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            valueOperations.set(tokenKey, userId.toString(), Duration.ofSeconds(jwtProperties.getRefreshTokenSeconds()));
            setOperations.add(indexKey, tokenKey);

            return null;
        });
    }

    @Override
    public Optional<Long> findByRefreshToken(String refreshToken) {
        String tokenKey = getTokenKey(refreshToken);

        String memberId = valueOperations.get(tokenKey);
        if (Objects.isNull(memberId)) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(memberId));
    }

    @Override
    public void delete(Long userId, String refreshToken) {
        String tokenKey = getTokenKey(refreshToken);
        String indexKey = getIndexKey(userId);

        redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            setOperations.remove(indexKey, tokenKey);
            redisTemplate.delete(tokenKey);

            Long remaining = setOperations.size(indexKey);
            if (remaining != null && remaining == 0L) {
                redisTemplate.delete(indexKey);
            }

            return null;
        });
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        String indexKey = getIndexKey(userId);

        Set<String> tokens = setOperations.members(indexKey);
        if (tokens != null && !tokens.isEmpty()) {
            redisTemplate.delete(tokens);
        }
        redisTemplate.delete(indexKey);
    }

    private String getTokenKey(final String refreshToken) {
        return KEY_PREFIX + ":" + refreshToken;
    }

    private String getIndexKey(final Long userId) {
        return INDEX_PREFIX + ":" + userId;
    }
}
