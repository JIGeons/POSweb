package PosWeb.POS.domain.dto.Order;

import PosWeb.POS.domain.Order;
import PosWeb.POS.domain.OrderApprove;
import PosWeb.POS.domain.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderDto {
    private Long id;
    private String orderName;
    private List<OrderItemDto> orderItems;
    private int amount;                     // 주문 총 금액
    private String orderDate;               // 주문시간
    private OrderStatus status;             // 주문 상태
    private OrderApprove approve;           // 결제 방법

    public OrderDto(Order order) {
        id = order.getId();
        if (order.getOrderItems().size() > 1) {
            orderName = order.getOrderItems().getFirst().getItem().getName() + " 외 " + (order.getOrderItems().size() - 1) + "개";
        } else {
            orderName = order.getOrderItems().getFirst().getItem().getName();
        }
        amount = order.getAmount();
        orderDate = formatDateTime(order.getOrderDate());
        status = order.getStatus();
        approve = order.getApprove();
        orderItems = order.getOrderItems().stream()
                .map(oi -> new OrderItemDto(oi))
                .collect(Collectors.toList());
    }

    private String formatDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy.MM.dd HH:mm:ss");
        return localDateTime.format(formatter);
    }
}
