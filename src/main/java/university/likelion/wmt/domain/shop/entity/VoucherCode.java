package university.likelion.wmt.domain.shop.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "voucher_codes",
    indexes = {
        @Index(name = "idx_voucher_code_status", columnList = "status")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class VoucherCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchandise_id", nullable = false)
    private Merchandise merchandise;

    @Column(name = "pin_code", nullable = false, unique = true)
    private String pinCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(32)")
    private VoucherStatus status;

    @Column(name = "starts_at", nullable = false)
    private LocalDate startsAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDate endedAt;

    @Builder
    public VoucherCode(Merchandise merchandise, String pinCode, VoucherStatus status, LocalDate startsAt,
        LocalDate endedAt) {
        if (endedAt.isBefore(startsAt)) {
            throw new IllegalArgumentException("만료일은 발급일보다 앞설 수 없습니다.");
        }

        this.merchandise = merchandise;
        this.pinCode = pinCode;
        this.status = status;
        this.startsAt = startsAt;
        this.endedAt = endedAt;
    }

    public void used() {
        this.status = VoucherStatus.USED;
    }
}
