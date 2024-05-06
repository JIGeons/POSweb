package PosWeb.POS.repository;

import PosWeb.POS.domain.Order;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        if (order.getId() == null) {
            // order가 없는 경우 데이터베이스에 저장
            em.persist(order);
        } else {
            // order가 있는 경우 기존 데이터에 병합을 한다.
            em.merge(order);
        }
    }

    public void delete(Order order) {
        if (order != null){
            em.remove(order);
            em.flush();
        }
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAllOrders() {
        return em.createQuery("select o from Order o", Order.class)
                .getResultList();
    }
}
