package PosWeb.POS.domain.dto.Item;

import PosWeb.POS.domain.Item;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
@NoArgsConstructor
public class StoreItemForm {
    @NotEmpty(message = " ※ 상품 이름은 필수로 입력해야합니다.")
    private String name;

    @NotNull(message = " ※ 상품 가격은 필수로 입력해야합니다.")
    @Range(min = 100, max = 1000000, message = " ※ 상품 가격은 100원 ~ 1,000,000원 사이로 입력해주세요.")
    private Integer price;

    @NotEmpty(message = " ※ 카테고리는 필수로 선택해야합니다.")
    private String category;
    @NotEmpty(message = " ※ 제조회사는 필수로 입력해야합니다.")
    private String company;

    public StoreItemForm(Item item) {
        name = item.getName();
        price = item.getPrice();
        category = item.getCategory().getCategory();
        company = item.getCompany();
    }
}
