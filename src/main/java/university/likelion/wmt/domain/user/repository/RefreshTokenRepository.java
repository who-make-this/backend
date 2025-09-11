package university.likelion.wmt.domain.user.repository;

import java.util.Optional;

public interface RefreshTokenRepository {
    void save(Long userId, String refreshToken);

    Optional<Long> findByRefreshToken(String refreshToken);

    void delete(Long userId, String refreshToken);

    void deleteAllByUserId(Long userId);
}
