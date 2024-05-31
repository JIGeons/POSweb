package PosWeb.POS.repository;

import PosWeb.POS.domain.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    // 카테고리 페이징
    public Page<Category> findByCtgItemsPaged(Pageable pageable) {
        Query query = em.createQuery("select c from Category c", Category.class);
        List<Category> allCategory = query.getResultList();
        // 아이템 조회 쿼리 작성
        List<Category> categories = query.setFirstResult((int) pageable.getOffset())  // 페이지 번호에 따라 결과를 시작하는 위치 설정
                .setMaxResults(pageable.getPageSize())    // 페이지 크기 설정
                .getResultList();

        return new PageImpl<>(categories, pageable, allCategory.size());
    }
}
