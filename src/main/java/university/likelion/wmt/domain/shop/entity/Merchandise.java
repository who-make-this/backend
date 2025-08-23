package university.likelion.wmt.domain.shop.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.hypersistence.utils.hibernate.id.Tsid;

@Entity
@Table(name = "merchandise")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Merchandise {
    @Id
    @Tsid
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "varchar(32)")
    private MerchandiseType type;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Merchandise(String name, Long price, MerchandiseType type, Boolean isVisible) {
        this.name = name;
        this.price = price;
        this.type = type;
        this.isVisible = isVisible;
    }

    public void setVisible() {
        this.isVisible = true;
    }

    public void setInvisible() {
        this.isVisible = false;
    }
}
