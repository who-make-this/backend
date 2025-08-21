package university.likelion.wmt.domain.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "markets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Market {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 광역자치단체 (도, 특별자치도, 특별시, 광역시)
    private String province;

    // 기초자치단체 (시, 군, 구)
    private String city;

    // 시장 이름
    private String name;

    @Builder
    public Market(String province, String city, String name) {
        this.province = province;
        this.city = city;
        this.name = name;
    }
}
