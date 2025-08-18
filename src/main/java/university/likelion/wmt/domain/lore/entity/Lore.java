package university.likelion.wmt.domain.lore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import university.likelion.wmt.domain.market.entity.Market;

@Entity
@Table(name = "lore")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Lore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    // 제목
    @Column(nullable = false)
    private String title;

    // 내용
    @Column(nullable = false, length = 65535)
    private String content;

    // 해금에 필요한 미션 수
    private Long requiredMissionCount;

    @Builder
    public Lore(Market market, String title, String content, Long requiredMissionCount) {
        this.market = market;
        this.title = title;
        this.content = content;
        this.requiredMissionCount = requiredMissionCount;
    }
}
