package university.likelion.wmt.domain.order.implement;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.order.entity.Order;
import university.likelion.wmt.domain.order.entity.OrderDetail;
import university.likelion.wmt.domain.order.exception.OrderException;
import university.likelion.wmt.domain.order.repository.OrderRepository;
import university.likelion.wmt.domain.shop.entity.Merchandise;
import university.likelion.wmt.domain.shop.exception.ShopErrorCode;
import university.likelion.wmt.domain.shop.exception.ShopException;
import university.likelion.wmt.domain.shop.repository.MerchandiseRepository;
import university.likelion.wmt.domain.user.entity.User;
import university.likelion.wmt.domain.user.exception.UserErrorCode;
import university.likelion.wmt.domain.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderWriter {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final MerchandiseRepository merchandiseRepository;

    @Transactional
    public Order createOrder(Long userId, Long merchandiseId) {
        Merchandise merchandise = merchandiseRepository.findByIdAndIsVisibleTrue(merchandiseId)
            .orElseThrow(() -> new ShopException(ShopErrorCode.MERCHANDISE_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new OrderException(UserErrorCode.USER_NOT_FOUND));

        Order order = new Order(user);
        OrderDetail orderDetail = OrderDetail.builder()
            .order(order)
            .merchandise(merchandise)
            .price(merchandise.getPrice())
            .build();

        order.plusTotalPrice(orderDetail.getPrice());
        order.getOrderDetails().add(orderDetail);
        return orderRepository.save(order);
    }
}
