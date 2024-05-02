package PosWeb.POS.controller;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Item;
import PosWeb.POS.domain.dto.Item.StoreItemForm;
import PosWeb.POS.domain.dto.Item.AddItemForm;
import PosWeb.POS.service.CategoryService;
import PosWeb.POS.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

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

        // 카테고리 별 상품들을 Map에 담기(12개씩 페이징)
        Page<Item> paging = itemService.findByCtgItemsPaged(category, page, 12);

        // model에 저장
        model.addAttribute("categoryItems", paging);

        return "items/posweb";
    }

    // 상품 추가("items/store")
    @GetMapping("items/store")
    public String storeItem(Model model){
        model.addAttribute("categories", categoryService.getAllCategories());   // category의 select 태그에 사용
        model.addAttribute("storeItemForm", new StoreItemForm());                   // store를 위한 빈 AddItemForm객체 사용
        return "items/storeItem";
    }

    @PostMapping("items/store")
    public String storeItem(@ModelAttribute("categories") Category category,
                          @Valid @ModelAttribute("storeItemForm") StoreItemForm storeItemForm,
                          BindingResult bindingResult) {

        // addItemForm에 문제가 있을 시
        if (bindingResult.hasErrors()) {
            return "items/storeItem";
        }

        // 상품 생성
        String result = itemService.saveItem(storeItemForm);
        log.info("items save : " + result);

        // item 생성 실패(이미 존재하는 item)
        if (result.equals("Fail")){
            bindingResult.reject("itemAddFail","이미 존재하는 상품입니다.");
            return "items/storeItem";
        };

        // item 생성 성공
        return "redirect:/items";
    }

    // 상품 입고("items/add")
    @GetMapping("items/add")
    public String addItem(@RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "category", defaultValue = "biskuit")String category,
                          Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", category);

        // 해당 category의 상품을 10개씩 페이징 처리
        Page<Item> paging = itemService.findByCtgItemsPaged(category, page, 10);
        model.addAttribute("items", paging);

        // select의 value를 설정하기 위해 StoreItemForm의 category를 request로 set
        AddItemForm AddItemForm = new AddItemForm();
        AddItemForm.setCategory(category);

        // 입고할 상품의 정보를 담을 빈 객체 생성
        model.addAttribute("selectItem", AddItemForm);

        return "items/addItem";
    }

    @PostMapping("items/add")
    public String addItem(@ModelAttribute("categories") Category category,
                          @Valid @ModelAttribute("selectItem") AddItemForm addItemForm,
                          BindingResult bindingResult,
                          Model model) {

        // selectItem에 error가 있을 시 items/addItem return
        if (bindingResult.hasErrors()) {
            // 해당 category의 상품을 10개씩 페이징 처리
            Page<Item> paging = itemService.findByCtgItemsPaged(addItemForm.getCategory(), 0, 10);
            model.addAttribute("items", paging);
            System.out.println("에러 발생!!!!!!!!");
            return "items/addItem";
        }

        // 상품 입고 (item.stockQuantity update)
        itemService.addItem(addItemForm);
        return "redirect:/items";
    }
}
