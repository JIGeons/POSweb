package PosWeb.POS.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_num")
    private int number;
    private String category;
}
