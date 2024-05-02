package PosWeb.POS.domain.dto.Item;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
@NoArgsConstructor
public class AddItemForm {
    private Long id;

    @NotEmpty(message = " ※ 상품 이름을 입력해주세요.")
    private String name;

    @NotNull(message = " ※ 입고할 상품의 갯수를 입력해주세요.")
    @Range(min = 1, max = 1000, message = " ※ 입고 할 갯수를 1 ~ 1000개 사이로 입력해주세요.")
    private Integer stockQuantity;

    @NotEmpty(message = " ※ 카테고리를 입력해주세요.")
    private String category;
}
