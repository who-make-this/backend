package university.likelion.wmt.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.order.entity.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}
