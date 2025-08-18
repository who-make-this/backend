package university.likelion.wmt.domain.lore.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.lore.dto.response.LoreResponse;
import university.likelion.wmt.domain.lore.entity.Lore;
import university.likelion.wmt.domain.lore.repository.LoreRepository;
import university.likelion.wmt.domain.market.entity.Market;
import university.likelion.wmt.domain.market.exception.MarketErrorCode;
import university.likelion.wmt.domain.market.exception.MarketException;
import university.likelion.wmt.domain.market.repository.MarketRepository;
import university.likelion.wmt.domain.user.entity.User;
import university.likelion.wmt.domain.user.exception.UserErrorCode;
import university.likelion.wmt.domain.user.exception.UserException;
import university.likelion.wmt.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoreService {
    private final UserRepository userRepository;
    private final MarketRepository marketRepository;
    private final LoreRepository loreRepository;

    public List<LoreResponse> getUnlocked(Long userId, Long marketId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Market market = marketRepository.findById(marketId)
            .orElseThrow(() -> new MarketException(MarketErrorCode.MARKET_NOT_FOUND));

        // TODO: 미션 도메인 완성 시 완료 횟수 계산
        long completedMissionCount = 0L;

        List<Lore> loreList = loreRepository.findAllByMarketAndRequiredMissionCountLessThanEqualOrderByRequiredMissionCountAscIdAsc(
            market, completedMissionCount);

        return loreList.stream()
            .map(LoreResponse::from)
            .toList();
    }
}
