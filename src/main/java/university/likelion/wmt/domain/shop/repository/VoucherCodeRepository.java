package university.likelion.wmt.domain.shop.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import university.likelion.wmt.domain.shop.entity.Merchandise;
import university.likelion.wmt.domain.shop.entity.MerchandiseType;
import university.likelion.wmt.domain.shop.entity.VoucherCode;
import university.likelion.wmt.domain.shop.entity.VoucherStatus;

public interface VoucherCodeRepository extends JpaRepository<VoucherCode, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VoucherCode> findFirstByMerchandiseAndStatusAndStartsAtLessThanEqualAndEndedAtGreaterThanEqualOrderByIdAsc(
        Merchandise merchandise, VoucherStatus status, LocalDate startsAt, LocalDate endsAt);

    @Query("""
        select vc
          from OrderDetail od
          join od.order o
          join VoucherCode vc on vc.id = od.voucherCodeId
         where o.id = :orderId
           and o.user.id = :userId
           and vc.merchandise.type = :type
         order by vc.endedAt asc, vc.id asc
        """)
    List<VoucherCode> findVoucherCodesByOrderAndUser(@Param("userId") Long userId,
        @Param("orderId") Long orderId,
        @Param("type") MerchandiseType type);
}
