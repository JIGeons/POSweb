package PosWeb.POS.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 직접 생성하는 것을 방지하기 위함
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // CasecadeType.All => order를 persiste 할 시 CascadeType.All 모두를 자동으로 Persist 해준다.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    private int amount;                     // 주문 총 금액
    private LocalDateTime orderDate;        // 주문시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status;             // 주문 상태

    @Enumerated(EnumType.STRING)
    private OrderApprove approve;           // 결제 방법

    //== 연관관계 메서드 ==//
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    /**
     * 주문 생성 메소드
     */
    //== 생성 메소드 ==//
    public static Order createOrder(List<OrderItem> orderItems) {
        Order order = new Order();

        int amount = 0; // 주문 상품들의 총 갯수

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
            amount += orderItem.getTotalPrice();
        }

        order.setStatus(OrderStatus.ORDER);
        order.setAmount(amount);

        return order;
    }

    //== 비즈니스 로직 ==//

    /**
     * 주문 취소
     */
    public void cancel() {
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }
}
