package PosWeb.POS.domain;

import PosWeb.POS.exception.TooLessOrMuchDiscountException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;     // 주문 가격 (할인 금액을 제외한 실제 결제 금액)
    private int count;          // 주문 수량
    private int discount;       // 할인 금액

    //== 생성 메서드 ==//
    public static OrderItem createOrderItem(Item item, int orderPrice, int count, int discount) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);
        orderItem.setDiscount(discount);

        item.removeStock(count);
        return orderItem;
    }

    //== 비즈니스 로직==//
    public void cancel() {
        getItem().addStock(count);
    }
}
