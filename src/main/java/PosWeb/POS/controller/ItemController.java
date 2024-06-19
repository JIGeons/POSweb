package PosWeb.POS.controller;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Item;
import PosWeb.POS.domain.dto.Item.CartItemForm;
import PosWeb.POS.domain.dto.Item.StoreItemForm;
import PosWeb.POS.domain.dto.Item.AddItemForm;
import PosWeb.POS.service.CategoryService;
import PosWeb.POS.service.ItemService;
import PosWeb.POS.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@Slf4j
@RequiredArgsConstructor
@Tag(name = "상품 컨트롤러", description = "상품 관련 API")
public class ItemController {

    private final ItemService itemService;
    private final CategoryService categoryService;
    private final MemberService memberService;

    @Operation(summary = "상품 페이지", description = "선택된 카테고리에 대해 상품을 페이징 하고 출력한다. + 상품을 담을 Map객체를 생성하고 session에 저장")
    @GetMapping("items")
    public String items(@RequestParam(value = "ctgPage", defaultValue = "0") int ctgPage,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "category", defaultValue = "biskuit") String category,
                        HttpSession session,
                        Model model) {
        // 현재 인가된 회원 객체를 불러온다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("admin", memberService.findOne(authentication.getName()).isAdmin());
        // 카테고리 가지고 오기
        Page<Category> categoryList = categoryService.findByCtgPaged(ctgPage, 3);
        model.addAttribute("categories", categoryList);
        // 카테고리 별 상품들을 Map에 담기(12개씩 페이징)
        Page<Item> paging = itemService.findByCtgItemsPaged(category, page, 12);
        // model에 저장
        model.addAttribute("categoryItems", paging);
        // 선택한 상품을 담을 CateItemForm을 List로 생성 후 session에 추가
        Map<Integer, CartItemForm> cart = new ConcurrentHashMap<>();
        // session에 "cart"가 존재 하지 않을 시 cart리스트 저장
        if (session.getAttribute("cart") == null) {
            int totalCount = 0;
            int totalAmount = 0;
            int totalDiscount = 0;
            session.setAttribute("cart", cart);
            session.setAttribute("totalCount", totalCount);
            session.setAttribute("totalAmount", totalAmount);
            session.setAttribute("totalDiscount", totalDiscount);
            log.info("cart 세션 생성");
        }
        return "items/posweb";
    }

    @Operation(summary = "상품 선택", description = "선택된 상품의 정보를 session에 추가한다.")
    @PostMapping("items")
    @ResponseBody
    public String items(@RequestBody Map<String, Object> data, HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession();

        // 클라이언트에서 전송된 JSON 데이터를 받아온다.
        int id = (int) data.get("id");
        int orderPrice = (int) data.get("orderPrice");
        int quantity = (int) data.get("quantity");
        int discount= (int) data.get("discount");
        int totalCount = (int) data.get("totalCount");
        int totalAmount = (int) data.get("totalAmount");
        int totalDiscount = (int) session.getAttribute("totalDiscount");

        CartItemForm cartItem = new CartItemForm(itemService.findById(id), orderPrice, quantity, discount);
        // 해당 상품에 cart 세션에 저장이 되어 있는지 확인
        Map<Integer, CartItemForm> cart = (ConcurrentHashMap) session.getAttribute("cart");
        if (cart.get(id) == null) {
            cart.put(id, cartItem);
        } else {
            cart.replace(id, cartItem);
        }
        // 받아온 카트 정보를 세션에 저장한다.
        session.setAttribute("cart", cart);
        session.setAttribute("totalCount", totalCount);
        session.setAttribute("totalAmount", totalAmount);
        session.setAttribute("totalDiscount", totalDiscount + discount);

        return "success";
    }

    // cart 세션 상품 삭제
    @Operation(summary = "장바구니 상품 삭제", description = "선택된 상품에 대해 장바구니에서 삭제한다.")
    @PostMapping("items/cartDelete")
    @ResponseBody
    public String cartDelete(@RequestBody Map<String, Object> data, HttpServletRequest servletRequest) {
        // 클라이언트에서 전송된 Json data를 받아온다.
        List<Map<String, Integer>> cartItems = (List<Map<String, Integer>>) data.get("cartItems");
        int totalCount = (int) data.get("totalCount");
        int totalAmount = (int) data.get("totalAmount");
        int totalDiscount = (int) data.get("totalDiscount");

        // 세션에 cart 데이터를 받아온다.
        HttpSession session = servletRequest.getSession();
        Map<Integer, CartItemForm> cart = (ConcurrentHashMap) session.getAttribute("cart");

        // 삭제 리스트의 담긴 상품을 session에서 삭제
        for (Map<String, Integer> item : cartItems) {
            cart.remove((int) item.get("id"));
        }

        // 삭제한 cart Map을 세션에 다시 저장
        session.setAttribute("cart", cart);
        session.setAttribute("totalCount", totalCount);
        session.setAttribute("totalAmount", totalAmount);
        session.setAttribute("totalDiscount", totalDiscount);

        return "success";
    }

    // 세션 cart item정보 수정
    @Operation(summary = "cart 상품 정보 수정", description = "전달된 상품에 대해 session에 담긴 상품들의 정보를 수정한다.")
    @PostMapping("items/cartApply")
    @ResponseBody
    public String cartApply(@RequestBody Map<String, Object> data, HttpServletRequest servletRequest) {
        // 클라이언트에서 전송된 Json data를 받아온다.
        List<Map<String, Object>> cartItems = (List<Map<String, Object>>) data.get("cartItems");
        int totalCount = (int) data.get("totalCount");
        int totalAmount = (int) data.get("totalAmount");
        int totalDiscount = (int) data.get("totalDiscount");

        // 세션에 cart 데이터를 받아온다.
        HttpSession session = servletRequest.getSession();
        Map<Integer, CartItemForm> cart = (ConcurrentHashMap) session.getAttribute("cart");

        // 수정 리스트의 담긴 상품을 session에서 수정
        for (Map<String, Object> item : cartItems) {
            CartItemForm cartListItem = cart.get((int) item.get("id"));
            cartListItem.setOrderPrice((int) item.get("orderPrice"));
            cartListItem.setQuantity((int) item.get("quantity"));
            cartListItem.setDiscount((int) item.get("discount"));
        }

        // 삭제한 cart Map을 세션에 다시 저장
        session.setAttribute("cart", cart);
        session.setAttribute("totalCount", totalCount);
        session.setAttribute("totalAmount", totalAmount);
        session.setAttribute("totalDiscount", totalDiscount);
        return "success";
    }

    // 상품 추가("items/store")
    @Operation(summary = "상품 추가 Get",
            description = "상품 정보를 입력 받을 수 있도록 StoreItemForm 객체를 생성하여 클라이언트로 전송한다.")
    @GetMapping("items/store")
    public String storeItem(Model model){
        // category의 select 태그에 사용
        model.addAttribute("categories", categoryService.getAllCategories());
        // store를 위한 빈 AddItemForm객체 사용
        model.addAttribute("storeItemForm", new StoreItemForm());
        return "items/storeItem";
    }

    @Operation(summary = "상품 추가 Post", description = "입력받은 정보로 상품 객체를 생성하고 저장한다.")
    @PostMapping("items/store")
    public String storeItem(@ModelAttribute("categories") Category category,
                          @Valid @ModelAttribute("storeItemForm") StoreItemForm storeItemForm,
                          BindingResult bindingResult) {

        // storeItemForm에 문제가 있을 시
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
    @Operation(summary = "상품 갯수 추가 Get",
            description = "선택된 카테고리의 상품들을 페이징하고 입고할 상품의 정보를 담을 " +
                          "빈 AddItemForm 객체를 생성하여 클라이언트로 전송한다.")
    @GetMapping("items/add")
    public String addItem(@RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "category", defaultValue = "biskuit")String category,
                          Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", category);

        // 해당 category의 상품을 10개씩 페이징 처리
        Page<Item> paging = itemService.findByCtgItemsPaged(category, page, 10);
        model.addAttribute("items", paging);

        // select의 value를 설정하기 위해 AddItemForm의 category를 request로 set
        AddItemForm AddItemForm = new AddItemForm();
        AddItemForm.setCategory(category);

        // 입고할 상품의 정보를 담을 빈 객체 생성
        model.addAttribute("selectItem", AddItemForm);

        return "items/addItem";
    }

    // 상품 입고
    @Operation(summary = "상품 입고 Post",
            description = "입력받은 상품의 정보에 대해 validation을 진행하고 오류가 있으면," +
                          "해당 카테고리를 페이징처리하여 addItem페이지를 출력하고 오류가 없으면, " +
                          "해당 상품을 update하고 item페이지를 리디렉션 한다.")
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
            return "items/addItem";
        }

        // 상품 입고 (item.stockQuantity update)
        itemService.addItem(addItemForm);
        return "redirect:/items";
    }

    @Operation(summary = "상품 삭제 Get", description = "선택된 category의 상품을 조회하여 클라이언트로 전송")
    @GetMapping("items/delete")
    public String itemDelete(@RequestParam(value = "category", defaultValue = "null")String category, Model model) {
        List<Category> categories = categoryService.getAllCategories();
        Category ctg = new Category();
        if (category.equals("null")) {
            ctg = categories.getFirst();   // 초기 카테고리는 카테고리 목록에서 첫번째 카테고리로 한다.
        } else {
            for (Category findCtg : categories) {
                if (findCtg.getCategory().equals(category))
                    ctg = findCtg;
            }
        }

        List<Item> itemList = itemService.findCategoryItems(ctg);   // 해당 카테고리에 관련된 상품을 불러온다.
        List<StoreItemForm> itemsForm = new ArrayList<>();      // 찾아온 상품에 필요한 정보만 담을 리스트를 생성한다.

        for (Item item : itemList) {  // 필요한 정보만 리스트에 add
            itemsForm.add(new StoreItemForm(item));
        }

        model.addAttribute("categories", categories);
        model.addAttribute("items", itemsForm);

        return "items/deleteItem";
    }

    // 상품 비활성화
    @Operation(summary = "상품 삭제 Post", description = "선택된 상품들의 비활성화 변수를 true로 update한다.")
    @PostMapping("items/delete")
    public String itemDelete(@RequestParam("deleteList") List<String> deleteList) {

        // 리스트에 있는 상품들을 비활성화 처리
        for (String item : deleteList) {
            itemService.deleteItem(item);
        }

        return "redirect:/items";
    }
}
