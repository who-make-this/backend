package university.likelion.wmt.domain.order.implement;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.order.entity.Order;
import university.likelion.wmt.domain.order.exception.OrderErrorCode;
import university.likelion.wmt.domain.order.exception.OrderException;
import university.likelion.wmt.domain.order.repository.OrderRepository;
import university.likelion.wmt.domain.user.exception.UserErrorCode;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderReader {
    private final OrderRepository orderRepository;

    public Order getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!Objects.equals(order.getUser().getId(), userId)) {
            throw new OrderException(UserErrorCode.ACCESS_DENIED);
        }

        return order;
    }
}
