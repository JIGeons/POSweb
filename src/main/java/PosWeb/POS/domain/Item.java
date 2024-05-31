package PosWeb.POS.domain;

import PosWeb.POS.exception.NotEnoughStockException;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    @PositiveOrZero(message = " * 재고량은 0과 같거나 많아야 합니다.")
    private int stockQuantity;
    private String company;

    @Column(columnDefinition = "boolean default false")
    private boolean disable=false;    // 상품 비활성화 변수    true일 경우 비활성화

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_num")
    private Category category;


    public Item (Item item) {
        id = item.getId();
        name = item.getName();
        price = item.getPrice();
        stockQuantity = item.getStockQuantity();
        company = item.getCompany();
        category = new Category(item.getCategory());
    }

    //== 비즈니스 로직 ==//
    /**
     * stock 증가(상품 입고)
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소(주문 취소)
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new NotEnoughStockException("재고가 부족합니다.");
        }
        this.stockQuantity = restStock;
    }
}
