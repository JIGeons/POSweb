package PosWeb.POS.service;

import PosWeb.POS.domain.*;
import PosWeb.POS.domain.dto.Item.CartItemForm;
import PosWeb.POS.repository.ItemRepository;
import PosWeb.POS.repository.OrderItemRepository;
import PosWeb.POS.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    // 주문 전 orderEntity 생성
    @Transactional
    public Long preOrder(List<CartItemForm> cartItems) {

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemForm item : cartItems){
            // 엔티티 조회
            Item findItem = itemRepository.findOne(item.getItem().getId());

            // 주문상품 생성
            OrderItem orderItem = OrderItem.createOrderItem(findItem, item.getOrderPrice(), item.getQuantity(), item.getDiscount());

            // 생성된 OrderItem을 리스트에 추가
            orderItems.add(orderItem);
        }

        // 주문 생성
        Order order = Order.createOrder(orderItems);

        // 주문 저장
        orderRepository.save(order);

        return order.getId();
    }

    // 결제 완료
    @Transactional
    public void successOrder(Long orderId, OrderApprove approve) {

        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);

        // 결제 완료 시간, 결제 방식 set
        order.setOrderDate(LocalDateTime.now());
        order.setApprove(approve);

        // 주문 저장
        orderRepository.save(order);
    }

    // 주문 부결로 인한 결제 삭제
    @Transactional
    public void failOrder(Order order) {
        order.cancel();
        List<OrderItem> orderItems = order.getOrderItems();
        for(OrderItem orderItem : orderItems) {
            orderItemRepository.deleteOrderItem(orderItem);
        }
        orderRepository.delete(order);
    }

    // 주문 후 Order 저장
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

    public List<Order> findOrdersWithFetch(LocalDate startDate, LocalDate endDate, String itemName) {
        return orderRepository.findAllOrdersWithFetch(startDate, endDate, itemName);
    }

    public List<Order> findOrdersByMonth(int year, int month) {
        return orderRepository.findOrdersByMonth(year, month);
    }

    public Order findOrder(Long id) {
        return orderRepository.findOne(id);
    }

    // 주문 상품을 CartItemForm으로 저장
    public List<CartItemForm> getCartItem(Long id) {

        // 주문 조회
        List<Order> order = orderRepository.findAllWithItems(id);

        // cartItemForm으로 저장
        if (!order.isEmpty()) {
            List<CartItemForm> result = order.getFirst().getOrderItems()
                    .stream()
                    .map(oi -> new CartItemForm(oi))
                    .collect(Collectors.toList());
            return result;
        }

        return null;
    }
}
