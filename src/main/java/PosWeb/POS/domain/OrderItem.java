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

    private int orderPrice;     // 주문 가격
    private int count;          // 주문 수량
    private int discount;       // 할인률

    //== 생성 메서드 ==//
    public static OrderItem createOrderItem(Item item, int orderPrice, int count, int discount) {
        if (discount < 0 || discount > 100) {
            throw new TooLessOrMuchDiscountException("할인률은 0 ~ 100 사이이어야 합니다.");
        }
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

    //== 조회 로직==//
    /**
     * 주문 상품 전체 가격 조회
     */
    public int getTotalPrice() {
        int amount = getOrderPrice() * getCount();

        if (getDiscount() != 0) {
            double discountPrice = getOrderPrice() * getCount() * getDiscount() / 100;
            amount -= (int) discountPrice;
        }

        return amount;
    }
}
