package university.likelion.wmt.domain.mission.respository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import university.likelion.wmt.domain.market.entity.Market;
import university.likelion.wmt.domain.mission.entity.Mission;
import university.likelion.wmt.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    // 특정 유저의 모든 미션을 삭제
    void deleteAllByUser(User user);
    // 특정 유저의 완료되지 않은 미션 개수를 반환
    long countByUserAndCompletedFalse(User user);
    // 특정 유저의 완료되지 않은 미션 중 가장 첫 번째 미션을 반환
    Optional<Mission> findFirstByUserAndCompletedFalseOrderByCreatedAtAsc(User user);
    // 특정 유저의 완료된 모든 미션 목록을 반환

    @EntityGraph(attributePaths = "image")
    List<Mission> findByUserAndCompletedTrue(User user);
    // 특정 유저의 완료되지 않은 미션만 삭제
    void deleteByUserAndCompletedFalse(User user);
    // 특정 유저의 완료된 미션을 카테고리별로 찾아 반환
    List<Mission> findByUserAndCategoryAndCompletedTrue(User user, String category);

    @Query("SELECT COUNT(m) FROM Mission m WHERE m.user = :user AND m.market = :market AND m.completed = true")
    long countByUserAndCompletedTrue(@Param("user") User user, @Param("market") Market market);

    //특정 유저의 가장 먼저 생성된 미션의 생성 시간을 반환
    Optional<LocalDateTime> findFirstByUserOrderByCreatedAtAsc(User user);

    List<Mission> findByReportId(Long reportId);

    List<Mission> findByMarketId(Long marketId);

    @EntityGraph(attributePaths = "image")
    Optional<Mission> findByUserAndImageCfName(User user, String cfName);
}
