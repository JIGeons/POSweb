package PosWeb.POS.controller;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Item;
import PosWeb.POS.service.CategoryService;
import PosWeb.POS.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CategoryService categoryService;

    @GetMapping("items")
    public String items(Model model) {

        // 카테고리 가지고 오기
        List<Category> categoryList = new ArrayList<>();
        categoryList = categoryService.getAllCategories();
        model.addAttribute("categories", categoryList);

        // 카테고리 별 상품들을 Map에 담기
        Map<String, List<Item>> categoryItemMap = new HashMap<>();
        for (Category category : categoryList) {
            List<Item> items = itemService.findCategoryItems(category);
            categoryItemMap.put(category.getCategory(), items);
        }
        // model에 저장
        model.addAttribute("categoryItems", categoryItemMap);

        return "items/posweb";
    }
}
