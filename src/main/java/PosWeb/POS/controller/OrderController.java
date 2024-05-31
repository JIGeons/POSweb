package PosWeb.POS.controller;

import PosWeb.POS.domain.*;
import PosWeb.POS.domain.dto.Item.CartItemForm;
import PosWeb.POS.domain.dto.Order.OrderAmountForm;
import PosWeb.POS.domain.dto.Order.OrderDto;
import PosWeb.POS.service.ItemService;
import PosWeb.POS.service.MemberService;
import PosWeb.POS.service.MemberTimeService;
import PosWeb.POS.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    private final MemberService memberService;
    private final MemberTimeService memberTimeService;

    @Value("${imp.api.key}")
    private String apiKey;

    @Value("${imp.api.secretkey}")
    private String secretKey;
    @Value("${imp.code}")
    private String impCode;

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
        model.addAttribute("impCode", impCode);

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
                             @RequestParam(value = "cancelOrderId", defaultValue = "") Long cancelOrderId,
                             Model model) {

        // 주문 리스트 생성
        List<OrderDto> orderList = new ArrayList<>();

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

        // 결제 취소를 완료 한 경우 해당 주문 상태가 바뀐 것을 보여주기 위해서
        model.addAttribute("cancelOrderId", cancelOrderId);

        return "order/check";
    }

    @GetMapping("order/checks")
    @ResponseBody
    public List<OrderDto> orderCheck(@RequestParam(value = "startDate", defaultValue = "") LocalDate startDate,
                                     @RequestParam(value = "endDate", defaultValue="") LocalDate endDate,
                                     @RequestParam(value = "itemName", defaultValue = "") String itemName) {
        // 주문 리스트 생성
        List<OrderDto> orderList = new ArrayList<>();

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

        return orderList;
    }

    // 주문 결제 취소
    @PostMapping("order/cancel")
    public String orderCancel(@RequestParam("cancelOrderId") Long cancelOrderId) throws IOException {
        
        Order cancelOrder = orderService.findOrder(cancelOrderId);
        
        // 주문 상태 이면서
        if (cancelOrder.getStatus().equals(OrderStatus.ORDER)) {
            // 주문 결제 방법이 현금일 경우 그냥 결제 취소
            if (cancelOrder.getApprove().equals(OrderApprove.CASH)){
                orderService.cancelOrder(cancelOrderId);
            } else if (cancelOrder.getApprove().equals(OrderApprove.CARD)) {
                log.info("카드 결제 상품 주문 환불 진행 : 주문 번호 {}", cancelOrderId);
                String token = orderService.getIamportToken(apiKey, secretKey);
                try {
                    orderService.orderRefundWithToken(token, String.valueOf(cancelOrderId));
                    orderService.cancelOrder(cancelOrderId);
                } catch (Exception e) {
                    log.info("Exception e {}", e.getMessage());
                    System.out.println("Exception e : " + e.getMessage());
                    // 테스트용 결제 서비스를 쓰는 것이기 때문에 자동으로 취소가 되므로
                    // 실제론 환불이 불가능 해도 환불 주문 상태면 order이면 데이터베이스에 cancel로 update
                    orderService.cancelOrder(cancelOrderId);
                }
            }
        }
        return "redirect:/order/check?cancelOrderId="+cancelOrderId;
    }

    @GetMapping("order/end")
    public String orderEnd(Model model) {
        LocalDate now = LocalDate.now();
        model.addAttribute("now", now);

        OrderAmountForm dayAmountData = orderService.findOrdersForDay(now);

        model.addAttribute("dayAmountData", dayAmountData);

        return "order/end";
    }
}
