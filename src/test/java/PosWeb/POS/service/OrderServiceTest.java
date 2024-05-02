package PosWeb.POS.service;

import PosWeb.POS.domain.*;
import PosWeb.POS.domain.dto.Item.AddItemForm;
import PosWeb.POS.exception.NotEnoughStockException;
import PosWeb.POS.exception.TooLessOrMuchDiscountException;
import PosWeb.POS.repository.CategoryRepository;
import PosWeb.POS.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ItemService itemService;

    @Test
    public void 상품주문() throws Exception {
        // given
        Item bisket = createItem("과자", 1200, 100, "bisket");

        int orderCount = 11;     // 주문수량
        int discount = 0;       // 할인률

        // when
        Long orderId = orderService.order(bisket.getId(), OrderApprove.CARD, orderCount, discount);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, getOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다.", 1200 * orderCount, getOrder.getAmount());
        assertEquals("주문 수량만큼 재고가 줄어야 한다.", 89, bisket.getStockQuantity());
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        // given
        Item bisket = createItem("과자", 1200, 100, "bisket");

        int orderCount = 101;
        int discount = 0;

        // when
        Assertions.assertThrows(NotEnoughStockException.class, () -> orderService.order(bisket.getId(), OrderApprove.CARD, orderCount, discount));

        // then
        // fail("재고 수량 부족 예외가 발생해야 한다.");
    }

    @Test
    public void 상품주문_할인률초과() throws Exception {
        // given
        Item bisket = createItem("과자", 1200, 100, "bisket");

        int orderCount = 15;
        int discount = 101;

        // when
        Assertions.assertThrows(TooLessOrMuchDiscountException.class, () -> orderService.order(bisket.getId(), OrderApprove.CARD, orderCount, discount));

        // then
        // fail("할인률 초과 예외가 발생해야 한다.");
    }

    @Test
    public void 주문취소() throws Exception {
        // given
        Item bisket = createItem("과자", 1200, 100, "bisket");

        int orderCount = 2;
        int discount = 0;

        Long orderId = orderService.order(bisket.getId(), OrderApprove.CARD, orderCount, discount);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("주문 취소시 상태는 CANCEL 이다.", OrderStatus.CANCEL, getOrder.getStatus());
        assertEquals("주문이 취소된 상품은 그만큼 재고가 증가해야 한다.", 100, bisket.getStockQuantity());
    }

    private Item createItem(String name, int price, int quantity, String category) {
        AddItemForm bisket = new AddItemForm();
        bisket.setName(name);
        bisket.setPrice(price);
        bisket.setCategory("biskuit");
        bisket.setCompany("롯데제과");
        itemService.saveItem(bisket);

        Category ctg = categoryRepository.findOne(1);   // biskuit.category_num == 1
        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        item.setCategory(ctg);
        item.setCompany("롯데제과");
        return item;
    }
}
