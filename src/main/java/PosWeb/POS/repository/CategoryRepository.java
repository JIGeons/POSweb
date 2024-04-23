package PosWeb.POS.repository;

import PosWeb.POS.domain.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {

    private final EntityManager em;

    public void save(Category category) {
        if (category.getCategory() == null) {
            em.persist(category);
        }
    }

    public Category findOne(int number) {
        return em.find(Category.class, number);
    }

    public Category findByCategory(String category) {
        try {
            return em.createQuery("select c from Category c where c.category = :category", Category.class)
                    .setParameter("category", category)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;    // 쿼리가 없는 경우 null 반환
        }
    }

    public List<Category> findAll() {
        return em.createQuery("select c from Category c", Category.class)
                .getResultList();
    }
}
