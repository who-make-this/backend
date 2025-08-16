package university.likelion.wmt.domain.mileage.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.hypersistence.utils.hibernate.id.Tsid;

@Table(name = "mileage_logs")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MileageLog {
    @Id
    @Tsid
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 마일리지 기록 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 12)
    private MileageLogType type;

    // 마일리지의 원래 양
    @Column(name = "amount", nullable = false)
    private Long amount;

    // 마일리지의 남은 양
    @Transient
    @Setter
    private Long remaining = 0L;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // 마일리지 획득, 사용, 환불, 운영 상 조정의 이유를 나타내기 위한 키
    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false, length = 32)
    private MileageLogReferenceType referenceType;

    // 마일리지 획득, 사용, 환불, 운영 상 조정의 이유를 나타내는 연관 키
    @Column(name = "ref_id", nullable = false)
    private Long referenceId;

    // 환불, 운영 상 조정 시 조정된 마일리지의 원 출처
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_of")
    private MileageLog reversalOf;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public MileageLog(Long userId, MileageLogType type, Long amount, LocalDateTime expiresAt,
        MileageLogReferenceType referenceType, Long referenceId, MileageLog reversalOf) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.expiresAt = expiresAt;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.reversalOf = reversalOf;
    }

    @PrePersist
    @PreUpdate
    void validate() {
        if (amount == null || amount == 0) {
            throw new IllegalArgumentException("획득 또는 차감된 마일리지는 0일 수 없습니다.");
        }

        switch (type) {
            case EARN, REFUND -> {
                if (amount <= 0) {
                    throw new IllegalArgumentException("획득 또는 환불된 마일리지는 양수여야 합니다.");
                }
            }
            case USE, EXPIRE -> {
                if (amount >= 0) {
                    throw new IllegalArgumentException("사용 또는 만료된 마일리지는 음수여야 합니다.");
                }
                if (expiresAt != null) {
                    throw new IllegalArgumentException("사용 또는 만료된 마일리지는 만료일을 지정할 수 없습니다.");
                }
            }
        }
    }
}
