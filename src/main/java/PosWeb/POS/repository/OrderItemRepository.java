package PosWeb.POS.repository;

import PosWeb.POS.domain.OrderItem;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderItemRepository {
    private final EntityManager em;

    // orderItem 찾기
    public OrderItem findOne(Long id) {
        return em.find(OrderItem.class, id);
    }

    // orderItem 삭제
    public void deleteOrderItem(OrderItem orderItem) {
        if (orderItem != null) {
            // 엔티티 삭제
            em.remove(orderItem);
            // 영속성 컨텍스트 비우고 실제로 삭제
            em.flush();
        }
    }
}
