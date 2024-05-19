package PosWeb.POS.repository;

import PosWeb.POS.domain.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    // fetch join으로 모든 주문 검색
    public List<Order> findAllOrdersWithFetch(LocalDate startDate, LocalDate endDate, String name) {
        // 기본 jqpl 작성
        String jpql = "select o from Order o join fetch o.orderItems oi join fetch oi.item i";

        // 모든 파라미터가 null이 아닐 시 where절 추가
        if (startDate != null || endDate != null || name != null) {
            int count = 0;  // count가 1 이상이면 and 추가
            jpql += " where";
            if (startDate != null) {
                jpql += " o.orderDate >= :startDate";
                count++;
            }
            if (endDate != null) {
                if (count > 0 ) jpql += " and ";
                jpql += " o.orderDate <= :endDate";
            }
            if (name != null) {
                if (count > 0 ) jpql += " and ";
                // exists 절을 사용하여 현재 주문에서 특정 아이템을 포함하는 주문을 찾는 서브쿼리 추가
                jpql += " exists (select 1 from OrderItem oi2 join oi2.item i2 where oi2.order = o and i2.name = :name)";
            }
        }

        // 날짜 기준으로 내림차순 정렬
        jpql += " order by o.orderDate desc";

        // 완선된 쿼리문을 entity manager를 사용하여 쿼리 생성
        Query query = em.createQuery(jpql, Order.class);

        // setParameter
        if (startDate != null) {
            // LocalDate를 LocalDateTime으로 형 변환 (startOfDay로 00 : 00 으로 시간 세팅)
            query = query.setParameter("startDate", startDate.atStartOfDay());
        }
        if (endDate != null) {
            // LocalDate를 LocalDateTime으로 형 변환 (시간을 23시 59분 59초로 세팅)
            query = query.setParameter("endDate", endDate.atTime(23,59,59));
        }
        if (name != null) {
            query = query.setParameter("name", name);
        }

        // 데이터를 조회한다.
        return query.getResultList();
    }

    // item 번호로 주문 검색
    public List<Order> findAllWithItems(Long id) {
        return em.createQuery(  // fetch join으로 주문의 아이템을 불러온다.
                "select o from Order o "
                + " join fetch o.orderItems oi"
                + " join fetch oi.item i"
                + " join fetch i.category c"
                + " where o.id =: id", Order.class)
                .setParameter("id", id)
                .getResultList();
    }

    // 날짜로 주문 검색
    public List<Order> findOrdersByMonth(int year, int month) {
        return em.createQuery(
                "select o from Order o"
                + " where year(o.orderDate) =: year and month(o.orderDate) =: month", Order.class)
                .setParameter("year", year)
                .setParameter("month", month)
                .getResultList();
    }
}
