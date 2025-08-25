package university.likelion.wmt.domain.lore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.lore.entity.UserLoreUnlock;

public interface UserLoreUnlockRepository extends JpaRepository<UserLoreUnlock, Long> {
    boolean existsByUserIdAndMarketIdAndRequiredMissionCount(Long userId, Long marketId, Long requiredMissionCount);
}
