package PosWeb.POS.repository;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Item;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            // item이 없을 경우 데이터베이스에 저장한다.
            em.persist(item);
        } else {
            // item이 있는 경우 기존 데이터에 병합을 한다.
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public Item findByName(String name) {
        try {
            return em.createQuery("select i from Item i where i.name = :name", Item.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;    // 쿼리가 없는 경우 null 반환
        }
    }

    public List<Item> findByCategory(Category category) {
        return em.createQuery(
                "select i from Item i " +
                        " where i.category = :category", Item.class)
                .setParameter("category", category)
                .getResultList();
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

    public Page<Item> findByCtgItemsPaged(Category category, Pageable pageable) {
        // 아이템 조회 쿼리 작성
        List<Item> items = em.createQuery(
                "select i from Item i where i.category =: category", Item.class)
                .setParameter("category", category)
                .setFirstResult((int) pageable.getOffset())  // 페이지 번호에 따라 결과를 시작하는 위치 설정
                .setMaxResults(pageable.getPageSize())    // 페이지 크기 설정
                .getResultList();

        return new PageImpl<>(items, pageable, category.getTotal());
    }
}
