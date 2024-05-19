package PosWeb.POS.service;

import PosWeb.POS.domain.*;
import PosWeb.POS.domain.dto.Item.CartItemForm;
import PosWeb.POS.domain.dto.Order.OrderDto;
import PosWeb.POS.repository.ItemRepository;
import PosWeb.POS.repository.OrderItemRepository;
import PosWeb.POS.repository.OrderRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    // 주문 전 orderEntity 생성
    @Transactional
    public Long preOrder(List<CartItemForm> cartItems) {

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemForm item : cartItems){
            // 엔티티 조회
            Item findItem = itemRepository.findOne(item.getItem().getId());

            // 주문상품 생성
            OrderItem orderItem = OrderItem.createOrderItem(findItem, item.getOrderPrice(), item.getQuantity(), item.getDiscount());

            // 생성된 OrderItem을 리스트에 추가
            orderItems.add(orderItem);
        }

        // 주문 생성
        Order order = Order.createOrder(orderItems);

        // 주문 저장
        orderRepository.save(order);

        return order.getId();
    }

    // 결제 완료
    @Transactional
    public void successOrder(Long orderId, OrderApprove approve) {

        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);

        // 결제 완료 시간, 결제 방식 set
        order.setOrderDate(LocalDateTime.now());
        order.setApprove(approve);

        // 주문 저장
        orderRepository.save(order);
    }

    // 주문 부결로 인한 결제 삭제
    @Transactional
    public void failOrder(Order order) {
        order.cancel();
        List<OrderItem> orderItems = order.getOrderItems();
        for(OrderItem orderItem : orderItems) {
            orderItemRepository.deleteOrderItem(orderItem);
        }
        orderRepository.delete(order);
    }

    // 주문 후 Order 저장
    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        // 주문 취소
        order.cancel();
    }

    /**
     * 주문 검색
     */
    public List<Order> findOrders() {
        return orderRepository.findAllOrders();
    }

    public List<OrderDto> findOrdersWithFetch(LocalDate startDate, LocalDate endDate, String itemName) {
        List<Order> orders = orderRepository.findAllOrdersWithFetch(startDate, endDate, itemName);

        // orderDto로 형식 변환
        if (!orders.isEmpty()) {
            List<OrderDto> result = orders.stream()
                    .map(order -> new OrderDto(order))
                    .collect(Collectors.toList());
            return result;
        }

        // order 주문이 없는 것이므로 null 반환
        return null;
    }

    public List<Order> findOrdersByMonth(int year, int month) {
        return orderRepository.findOrdersByMonth(year, month);
    }

    public Order findOrder(Long id) {
        return orderRepository.findOne(id);
    }

    // 주문 상품을 CartItemForm으로 저장
    public List<CartItemForm> getCartItem(Long id) {

        // 주문 조회
        List<Order> order = orderRepository.findAllWithItems(id);

        // cartItemForm으로 저장
        if (!order.isEmpty()) {
            List<CartItemForm> result = order.getFirst().getOrderItems()
                    .stream()
                    .map(oi -> new CartItemForm(oi))
                    .collect(Collectors.toList());
            return result;
        }

        return null;
    }

    // 환불 시 필요한 iamport의 token을 받아온다.
    public String getIamportToken(String apiKey, String secretKey) throws IOException {
        URL url = new URL("https://api.iamport.kr/users/getToken");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // 요청 방식을 POST로 설정
        conn.setRequestMethod("POST");

        // 요청의 Content-Type과 Accept 헤더 설정
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");

        // 해당 연결을 출력 스트림(요청)으로 사용
        conn.setDoOutput(true);

        // JSON 객체가 해당 API가 필요로 하는 데이터 추가.
        JsonObject json = new JsonObject();
        json.addProperty("imp_key", apiKey);
        json.addProperty("imp_secret", secretKey);

        // 출력 스트림으로 해당 conn에 요청
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        // json 객체를 문자열 형태로 HTTP 요청 본문에 추가
        bw.write(json.toString());
        bw.flush();
        bw.close();

        // 입력 스트림으로 conn 요청에 대한 응답 반환
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        Gson gson = new Gson(); // 응답 데이터를 자바 객체로 변환
        String response = gson.fromJson(br.readLine(), Map.class).get("response").toString();
        String accessToken = gson.fromJson(response, Map.class).get("access_token").toString();
        br.close();

        conn.disconnect();

        log.info("IamPort 엑세스 토큰 발금 성공 : {}", accessToken);
        return accessToken;
    }

    public void orderRefundWidthToken(String access_token, String merchant_uid)  throws IOException {
        URL url = new URL("https://api.iamport.kr/payments/cancel");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        // 요청 방식을 POST로 설정
        conn.setRequestMethod("POST");

        // 요청의 Content-Type, Accept, Authorization 헤더 설정
        conn.setRequestProperty("Content-type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", access_token);

        // 해당 연결을 출력 스트림(요청)으로 사용
        conn.setDoOutput(true);

        // JSON 객체에 해당 API가 필요로하는 데이터 추가.
        JsonObject json = new JsonObject();
        json.addProperty("merchant_uid", merchant_uid);
        // String reason = "단순 변심";
        // json.addProperty("reason", reason);

        // 출력 스트림으로 해당 conn에 요청
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        bw.write(json.toString());
        bw.flush();
        bw.close();

        // 입력 스트림으로 conn 요청에 대한 응답 반환
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        br.close();
        conn.disconnect();

        log.info("결제 취소 완료 : 주문 번호 {}", merchant_uid);
    }
}
