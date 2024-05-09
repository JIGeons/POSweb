package PosWeb.POS.domain.dto.Item;

import PosWeb.POS.domain.Item;
import PosWeb.POS.domain.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemForm {
    private Item item;
    private int orderPrice;     // 주문 가격
    private int quantity;          // 주문 수량
    private int discount;       // 할인금액

    public CartItemForm(OrderItem orderItem) {
        item = new Item(orderItem.getItem());
        orderPrice = orderItem.getOrderPrice();
        quantity = orderItem.getCount();
        discount = orderItem.getDiscount();
    }
}
