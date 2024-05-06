package PosWeb.POS.repository;

import PosWeb.POS.domain.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {

    private final EntityManager em;

    public void save(Category category) {
        if (category.getNumber() == null) {
            em.persist(category);
            System.out.println("저장된거임?");
        }
    }

    public Category findOne(Long number) {
        return em.find(Category.class, number);
    }

    public Optional<Category> findByCategory(String category) {
            return em.createQuery("select c from Category c where c.category = :category", Category.class)
                    .setParameter("category", category)
                    .getResultStream()
                    .findFirst();
    }


    public List<Category> findAll() {
        return em.createQuery("select c from Category c", Category.class)
                .getResultList();
    }
}
