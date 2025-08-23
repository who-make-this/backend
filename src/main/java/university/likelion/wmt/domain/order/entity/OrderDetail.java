package university.likelion.wmt.domain.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.hypersistence.utils.hibernate.id.Tsid;
import university.likelion.wmt.domain.shop.entity.Merchandise;

@Entity
@Table(name = "order_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class OrderDetail {
    @Id
    @Tsid
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchandise_id", nullable = false)
    private Merchandise merchandise;

    @Setter
    @Column(name = "voucher_code_id")
    private Long voucherCodeId;

    @Column(name = "price", nullable = false)
    private Long price;

    @Builder
    public OrderDetail(Order order, Merchandise merchandise, Long voucherCodeId, Long price) {
        this.order = order;
        this.merchandise = merchandise;
        this.voucherCodeId = voucherCodeId;
        this.price = price;
    }
}
