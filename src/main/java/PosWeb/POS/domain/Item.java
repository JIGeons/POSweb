package PosWeb.POS.domain;

import PosWeb.POS.exception.NotEnoughStockException;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
public class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;
    private String company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_num")
    private Category category;

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
