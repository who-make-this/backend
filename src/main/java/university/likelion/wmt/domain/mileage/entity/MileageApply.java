package university.likelion.wmt.domain.mileage.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import io.hypersistence.utils.hibernate.id.Tsid;

@Entity
@Table(name = "mileage_apply",
    indexes = {
        @Index(name = "idx_mileage_apply_src", columnList = "src_log_id"),
        @Index(name = "idx_mileage_apply_dst", columnList = "dst_log_id")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MileageApply {
    @Id
    @Tsid
    private Long id;

    // 획득, 환불, 운영 상 조정된 마일리지의 원 출처
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "src_log_id", nullable = false)
    private MileageLog srcLog;

    // 만료, 사용한 마일리지의 출처
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dst_log_id", nullable = false)
    private MileageLog dstLog;

    // 마일리지 양
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public MileageApply(MileageLog srcLog, MileageLog dstLog, Long amount) {
        this.srcLog = srcLog;
        this.dstLog = dstLog;
        this.amount = amount;
    }

    @PrePersist
    @PreUpdate
    void validate() {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("마일리지 양은 양수여야 합니다.");
        }
    }
}
