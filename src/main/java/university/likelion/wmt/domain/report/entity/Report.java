package university.likelion.wmt.domain.report.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import university.likelion.wmt.domain.market.entity.Market;
import university.likelion.wmt.domain.user.entity.User;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime explorationDate;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id")
    private Market market;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer totalScore;

    @Column(nullable = false)
    private String mainImage;

    // JSON 문자열로 저장하기 위해 String 타입으로 변경
    @Column(columnDefinition = "TEXT")
    private String completedMissionsByCategoriesJson;

    @Column(name = "report_title")
    private String reportTitle;

    @Column(name = "journal_content", columnDefinition = "LONGTEXT")
    private String journalContent;

    private Long earnedMileage;

    private Long earnedThisMonth;

    private Long remainingMonthlyMileage;

    @Builder
    public Report(User user, Market market,
        LocalDateTime explorationDate,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer totalScore,
        String mainImage,
        String reportTitle,
        String completedMissionsByCategoriesJson,
        String journalContent,
        Long earnedMileage,
        Long earnedThisMonth,
        Long remainingMonthlyMileage) {
        this.user = user;
        this.market = market;
        this.explorationDate = explorationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalScore = totalScore;
        this.mainImage = mainImage;
        this.reportTitle = reportTitle;
        this.completedMissionsByCategoriesJson = completedMissionsByCategoriesJson;
        this.journalContent = journalContent;
        this.earnedMileage = earnedMileage;
        this.earnedThisMonth = earnedThisMonth;
        this.remainingMonthlyMileage = remainingMonthlyMileage;
    }
}
