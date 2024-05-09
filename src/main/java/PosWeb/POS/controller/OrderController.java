package PosWeb.POS.controller;

import PosWeb.POS.component.ImpProperties;
import PosWeb.POS.domain.*;
import PosWeb.POS.domain.dto.Item.CartItemForm;
import PosWeb.POS.service.ItemService;
import PosWeb.POS.service.OrderService;
import com.siot.IamportRestClient.IamportClient;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ItemService itemService;
    private IamportClient iamportClient;

    // 상품 결제 페이지 이동
    @GetMapping("order")
    public String pay(HttpServletRequest servletRequest, Model model) {

        HttpSession session = servletRequest.getSession();
        // 세션에 저장된 cart 객체 불러오기
        Map<Integer, CartItemForm> cart = (ConcurrentHashMap) session.getAttribute("cart");

        // 주문 저장
        Long orderId = orderService.preOrder(cart.values().stream().toList());

        // 주문을 조회 하고 item들을 CartItemForm리스트로 저장
        List<CartItemForm> cartItems = orderService.getCartItem(orderId);

        // model에 저장
        model.addAttribute("orderId", orderId);
        session.setAttribute("cart", cartItems);

        return "order/pay";
    }

    @PostMapping("order")
    public String orderPay(@RequestParam("orderId") Long orderId,
                           @RequestParam("orderResult") String orderResult,
                           @RequestParam("orderApprove") String orderApprove,
                           HttpServletRequest servletRequest){
        // 세션 데이터 불러오기.
        HttpSession session = servletRequest.getSession();

        System.out.println("result : " + orderResult + ", approve : " + orderApprove);

        // 세션에 cart 객체 삭제
        session.removeAttribute("cart");
        // 세션에 count, amount, discount 삭제
        session.removeAttribute("totalCount");
        session.removeAttribute("totalAmount");
        session.removeAttribute("totalDiscount");

        if (orderResult.equals("SUCCESS")) { // 결제 성공
            if (orderApprove.equals("CASH"))    // 현금 결제 성공
                orderService.successOrder(orderId, OrderApprove.CASH);
            else                                // 카드 결제 성공
                orderService.successOrder(orderId, OrderApprove.CARD);
        } else { // 결제 실패
            Order order = orderService.findOrder(orderId);
            orderService.failOrder(order);
            log.info("주문 삭제 완료");
        }
        return "redirect:/items";
    }
}
