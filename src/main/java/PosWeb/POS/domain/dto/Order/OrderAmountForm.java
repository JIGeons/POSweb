package PosWeb.POS.domain.dto.Order;

import PosWeb.POS.domain.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class OrderAmountForm {
    int sales;  // 판매 금액
    int cancel; // 취소 금액
    int amount; // 총 매출 액

    public OrderAmountForm(int orderPrice, OrderStatus status) {
        sales = orderPrice;
        if (status.equals(OrderStatus.CANCEL)) {
            cancel = orderPrice;
            amount = 0;
        } else
            amount = orderPrice;
    }

    public void orderAmount(int orderPrice, OrderStatus status) {
        sales += orderPrice;
        if (status.equals(OrderStatus.CANCEL))
            cancel += orderPrice;
        else
            amount += orderPrice;
    }

    public OrderAmountForm(Long sales, Long canceled, Long amount) {
        this.sales = sales != null ? sales.intValue() : 0;
        this.cancel = canceled != null ? canceled.intValue() : 0;
        this.amount = amount != null ? amount.intValue() : 0;
    }
}
