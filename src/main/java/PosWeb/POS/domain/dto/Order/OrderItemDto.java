package PosWeb.POS.domain.dto.Order;

import PosWeb.POS.domain.Item;
import PosWeb.POS.domain.OrderItem;
import PosWeb.POS.domain.dto.Item.ItemDto;
import lombok.Getter;

@Getter
public class OrderItemDto {
    private ItemDto item;
    private int orderPrice;
    private int count;
    private int discount;

    public OrderItemDto(OrderItem orderItem) {
        item = new ItemDto(orderItem.getItem());
        orderPrice = orderItem.getOrderPrice();
        count = orderItem.getCount();
        discount = orderItem.getDiscount();
    }
}
