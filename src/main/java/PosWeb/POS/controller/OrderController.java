package PosWeb.POS.controller;

import PosWeb.POS.component.ImpProperties;
import PosWeb.POS.domain.*;
import PosWeb.POS.domain.dto.Item.CartItemForm;
import PosWeb.POS.domain.dto.Order.OrderAmountForm;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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

    // 매출 확인
    @GetMapping("order/amount")
    public String orderAmount(@RequestParam(value = "year", defaultValue = "-1") int year,
                              @RequestParam(value = "month", defaultValue = "-1") int month,
                              Model model) {

        if (year == -1)
            year = LocalDateTime.now().getYear();
        if (month == -1)
            month = LocalDateTime.now().getMonthValue();

        // 해당 달에 해당하는 order 정보 불러오기
        List<Order> orders = orderService.findOrdersByMonth(year, month);

        // 매출 정보를 등록할 map 생성
        Map<Integer, OrderAmountForm> monthOrderData = new HashMap<>();

        int totalAmount = 0;

        // 매출 정보 update
        for (Order order : orders) {
            int day = order.getOrderDate().getDayOfMonth();
            OrderAmountForm orderData = monthOrderData.get(day);

            // map 내에 해당 날짜의 객체가 없을 경우
            if (orderData == null) {
                // 객체 생성 후 저장
                OrderAmountForm orderAmountForm = new OrderAmountForm(order.getAmount(), order.getStatus());
                monthOrderData.put(day, orderAmountForm);
                totalAmount += orderAmountForm.getAmount();
            } else { // map 내에 해당 날짜의 객체가 있는 경우
                // 해당 날짜의 객체를 update
                orderData.orderAmount(order.getAmount(), order.getStatus());
                totalAmount += order.getAmount();
            }
        }

        model.addAttribute("orderAmount", monthOrderData);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("year", year);
        model.addAttribute("month", month);

        return "order/amount";
    }

    // 주문 조회
    @GetMapping("order/check")
    public String orderCheck(@RequestParam(value = "startDate", defaultValue = "") LocalDate startDate,
                             @RequestParam(value = "endDate", defaultValue="") LocalDate endDate,
                             @RequestParam(value = "itemName", defaultValue = "") String itemName,
                             Model model) {

        // 주문 리스트 생성
        List<Order> orderList = new ArrayList<>();

        // 검색 실패 시 클라이언트에게 검색 실패를 알릴 변수
        boolean searchResult = true;

        // 상품 이름에 대해 존재 유무 검사
        if (itemName != null) {
            if (itemService.findOne(itemName) == null) {
                // 상품이 존재 하지 않는다면 itemName을 null로 설정
                itemName = null;
                // 검색 결과를 false로 설정
                searchResult = false;
            }
        }

        // 지연 로딩을 해결하기 위해 fetch 조인으로 모든 주문 내역 조회
        orderList = orderService.findOrdersWithFetch(startDate, endDate, itemName);

        model.addAttribute("orderList", orderList);
        model.addAttribute("searchResult", searchResult);

        return "order/check";
    }
}
