package university.likelion.wmt.domain.mission.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import university.likelion.wmt.domain.image.entity.Image;
import university.likelion.wmt.domain.market.entity.Market;
import university.likelion.wmt.domain.user.entity.User;

@Entity
@Table(name = "mission")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @Column(name = "category", nullable = false, length = 20)
    private String category;

    @Column(name = "missionTitle", nullable = false, length = 20)
    private String missionTitle;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "completed")
    private boolean completed = false;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "report_id")
    private Long reportId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", referencedColumnName = "id")
    private Image image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "failure_reason_id")
    private MissionFailureReason failureReason;

    @Column(name = "missionNumbers", nullable = false)
    private Integer missionNumbers;

    @Column(name = "is_exploration_end", nullable = false)
    private boolean explorationEnded = false;

    @Builder
    public Mission(User user,
        Market market,
        String category,
        String missionTitle,
        String content,
        boolean completed,
        LocalDateTime createdAt,
        Long reportId,
        Image image,
        MissionFailureReason failureReason,
        Integer missionNumbers,
        boolean explorationEnded) {
        this.user = user;
        this.market = market;
        this.category = category;
        this.missionTitle = missionTitle;
        this.content = content;
        this.completed = completed;
        this.createdAt = createdAt;
        this.reportId = reportId;
        this.image = image;
        this.failureReason = failureReason;
        this.missionNumbers = missionNumbers;
        this.explorationEnded = explorationEnded;
    }
}
