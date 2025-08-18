package university.likelion.wmt.domain.market.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.market.entity.Market;

public interface MarketRepository extends JpaRepository<Market, Long> {
}
