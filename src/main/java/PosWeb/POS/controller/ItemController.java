package PosWeb.POS.controller;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Item;
import PosWeb.POS.domain.dto.Item.AddItemForm;
import PosWeb.POS.service.CategoryService;
import PosWeb.POS.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String items(@RequestParam(value = "page", defaultValue = "0")int page,
                        @RequestParam(value = "category", defaultValue = "biskuit")String category,
                        Model model) {

        // 카테고리 가지고 오기
        List<Category> categoryList = new ArrayList<>();
        categoryList = categoryService.getAllCategories();
        model.addAttribute("categories", categoryList);

        // 카테고리 별 상품들을 Map에 담기
        Page<Item> paging = itemService.findByCtgItemsPaged(category, page);
        // model에 저장
        model.addAttribute("categoryItems", paging);

        return "items/posweb";
    }

    @GetMapping("items/add")
    public String addItem(Model model){
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("addItemForm", new AddItemForm());
        return "items/addItem";
    }

    @PostMapping("items/add")
    public String addItem(@ModelAttribute("categories") Category category,
                          @Valid @ModelAttribute("addItemForm") AddItemForm addItemForm,
                          BindingResult bindingResult) {

        // addItemForm에 문제가 있을 시
        if (bindingResult.hasErrors()) {
            return "items/addItem";
        }

        // 상품 생성
        String result = itemService.saveItem(addItemForm);
        log.info("items save : " + result);

        // item 생성 실패(이미 존재하는 item)
        if (result.equals("Fail")){
            bindingResult.reject("itemAddFail","이미 존재하는 상품입니다.");
            return "items/addItem";
        };

        // item 생성 성공
        return "redirect:/items";
    }
}
