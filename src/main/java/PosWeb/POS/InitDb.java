package PosWeb.POS;

import PosWeb.POS.domain.Category;
import PosWeb.POS.domain.Member;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct  // 의존성 주입이 이루어진 후 초기화를 수행하는 메서드
    public void init() {
        initService.dbInit();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;

        public void dbInit() {
            Member admin = createMember();
            em.persist(admin);

            Category bisket = createCategory(1, "bisket");
            Category beverage = createCategory(2, "beverage");
            Category iceCream = createCategory(3, "iceCream");
            em.persist(bisket);
            em.persist(beverage);
            em.persist(iceCream);
        }

        private static Member createMember() {
            Member admin = new Member();
            admin.setMemberId("admin");
            admin.setName("admin");
            admin.setPw("1234");
            return admin;
        }

        private static Category createCategory(int number, String content) {
            Category category = new Category();
            category.setNumber(number);
            category.setCategory(content);
            return category;
        }
    }
}
