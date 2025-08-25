package university.likelion.wmt.domain.lore.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.image.implement.ImageReader;
import university.likelion.wmt.domain.lore.dto.response.LoreResponse;
import university.likelion.wmt.domain.lore.entity.Lore;
import university.likelion.wmt.domain.lore.entity.UserLoreUnlock;
import university.likelion.wmt.domain.lore.repository.LoreRepository;
import university.likelion.wmt.domain.lore.repository.UserLoreUnlockRepository;
import university.likelion.wmt.domain.market.entity.Market;
import university.likelion.wmt.domain.market.exception.MarketErrorCode;
import university.likelion.wmt.domain.market.exception.MarketException;
import university.likelion.wmt.domain.market.repository.MarketRepository;
import university.likelion.wmt.domain.mileage.entity.MileageLogReferenceType;
import university.likelion.wmt.domain.mileage.implement.MileageWriter;
import university.likelion.wmt.domain.mission.service.MissionService;
import university.likelion.wmt.domain.user.entity.User;
import university.likelion.wmt.domain.user.exception.UserErrorCode;
import university.likelion.wmt.domain.user.exception.UserException;
import university.likelion.wmt.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoreService {
    private static final List<Long> THRESHOLDS = List.of(1L, 3L, 5L, 10L, 15L);
    private static final List<Long> REWARD_POINTS = List.of(300L, 500L, 800L, 900L, 1000L);

    private final UserRepository userRepository;
    private final MarketRepository marketRepository;
    private final LoreRepository loreRepository;
    private final UserLoreUnlockRepository userLoreUnlockRepository;

    private final ImageReader imageReader;
    private final MissionService missionService;
    private final MileageWriter mileageWriter;

    @Transactional
    public List<LoreResponse> getUnlocked(Long userId, Long marketId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Market market = marketRepository.findById(marketId)
            .orElseThrow(() -> new MarketException(MarketErrorCode.MARKET_NOT_FOUND));

        long completedMissionCount = missionService.getCompletedMissionCount(userId, marketId);

        List<Lore> all = loreRepository.findAllByMarketOrderByRequiredMissionCountAscIdAsc(market);

        Map<Long, LoreResponse.LoreData> unlocked = new LinkedHashMap<>();
        for (Lore lore : all) {
            long required = lore.getRequiredMissionCount();
            if (required > completedMissionCount) {
                continue;
            }

            String imageUri = imageReader.get("LORE", lore.getId()).stream()
                .findFirst()
                .orElse(null);
            unlocked.put(required, new LoreResponse.LoreData(lore.getTitle(), lore.getContent(), imageUri));
        }
        for (long t : THRESHOLDS) {
            unlocked.putIfAbsent(t, null);
        }

        List<LoreResponse> responses = new ArrayList<>(THRESHOLDS.size());
        for (long t : THRESHOLDS) {
            if (completedMissionCount >= t) {
                boolean alreadyRewarded = userLoreUnlockRepository.existsByUserIdAndMarketIdAndRequiredMissionCount(userId, marketId, t);
                if (!alreadyRewarded) {
                    long rewardPoint = REWARD_POINTS.get(THRESHOLDS.indexOf(t));
                    mileageWriter.earn(userId, rewardPoint, LocalDateTime.now().plusYears(1L), MileageLogReferenceType.LORE, 0L);
                    userLoreUnlockRepository.save(UserLoreUnlock.builder()
                        .user(user)
                        .market(market)
                        .requiredMissionCount(t)
                        .build());
                }
            }

            responses.add(new LoreResponse(t, unlocked.get(t)));
        }

        return responses;
    }
}
