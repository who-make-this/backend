package university.likelion.wmt.domain.lore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.lore.entity.Lore;
import university.likelion.wmt.domain.market.entity.Market;

public interface LoreRepository extends JpaRepository<Lore, Long> {
    List<Lore> findAllByMarketOrderByRequiredMissionCountAscIdAsc(Market market);
}
