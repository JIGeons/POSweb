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
        initService.dbTestInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;

        public void dbInit() {
            Member admin = createAdmin();   // 관리자 아이디 생성
            em.persist(admin);

            // 기본 카테고리 3개 설정(과자, 음료, 아이스크림)
            Category bisket = createCategory("bisket");
            Category beverage = createCategory("beverage");
            Category iceCream = createCategory("iceCream");
            em.persist(bisket);
            em.persist(beverage);
            em.persist(iceCream);
        }

        public void dbTestInit2() {
            Member Choi = createMember("Choi", "최지성", "981211");
            Member Kim = createMember("Kim", "김지성", "121212");
            Member Park = createMember("Park", "박지성", "34343434");
            em.persist(Choi);
            em.persist(Kim);
            em.persist(Park);
        }

        private static Member createAdmin() {
            Member admin = new Member();
            admin.setStringId("admin");
            admin.setName("admin");
            admin.setPw("1234");    // 초기 비밀번호
            admin.setAdmin(true);   // 관리자 여부 true 설정
            return admin;
        }

        private static Member createMember(String id, String name, String pw) {
            Member admin = new Member();
            admin.setStringId(id);
            admin.setName(name);
            admin.setPw(pw);    // 초기 비밀번호
            return admin;
        }

        private static Category createCategory(String content) {
            Category category = new Category();
            category.setCategory(content);
            return category;
        }
    }
}
