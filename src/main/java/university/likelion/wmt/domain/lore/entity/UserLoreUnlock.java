package university.likelion.wmt.domain.lore.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.hypersistence.utils.hibernate.id.Tsid;
import university.likelion.wmt.domain.market.entity.Market;
import university.likelion.wmt.domain.user.entity.User;

@Entity
@Table(name = "user_lore_unlock",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_market_required",
            columnNames = {"user_id", "market_id", "required_mission_count"}
        )
    },
    indexes = {
        @Index(name = "idx_user_market", columnList = "user_id, market_id"),
        @Index(name = "idx_required_mission_count", columnList = "required_mission_count")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class UserLoreUnlock {
    @Id
    @Tsid
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @Column(name = "required_mission_count", nullable = false)
    private Long requiredMissionCount;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public UserLoreUnlock(User user, Market market, Long requiredMissionCount) {
        this.user = user;
        this.market = market;
        this.requiredMissionCount = requiredMissionCount;
    }
}
