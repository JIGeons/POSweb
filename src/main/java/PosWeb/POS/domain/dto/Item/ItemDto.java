package PosWeb.POS.domain.dto.Item;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Item;
import lombok.Getter;

@Getter
public class ItemDto {
    private Long id;
    private String name;
    private int price;
    private String category;

    public ItemDto (Item item) {
        id = item.getId();
        name = item.getName();
        price = item.getPrice();
        category = item.getCategory().getCategory();
    }
}
