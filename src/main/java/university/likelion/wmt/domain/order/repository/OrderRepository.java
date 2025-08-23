package university.likelion.wmt.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
