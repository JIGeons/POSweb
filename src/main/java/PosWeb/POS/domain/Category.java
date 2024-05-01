package PosWeb.POS.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter @Setter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_num")
    private int number;
    private String category;
    @ColumnDefault("0")
    private int total;  // 각 카테고리별 상품 총 개수

    // 상품 추가, 제거 시 한개씩만 추가하고 제거
    /**
     * 상품 추가
     */
    public void addItem() {
        this.total += 1;
    }

    /**
     * 상품 제거
     */
    public void removeItem() {
        this.total -= 1;
    }
}
