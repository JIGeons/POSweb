package PosWeb.POS.service;

import PosWeb.POS.domain.Item;
import PosWeb.POS.domain.Order;
import PosWeb.POS.domain.OrderApprove;
import PosWeb.POS.domain.OrderItem;
import PosWeb.POS.repository.ItemRepository;
import PosWeb.POS.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long itemId, OrderApprove orderApprove, int count, int discount) {

        // 엔티티 조회
        Item item = itemRepository.findOne(itemId);

        // 주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count, discount);

        // 주문 생성
        Order order = Order.createOrder(orderApprove, orderItem);

        // 주문 저장
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        // 주문 취소
        order.cancel();
    }

    /**
     * 주문 검색
     */
    public List<Order> findOrders() {
        return orderRepository.findAllOrders();
    }
}
