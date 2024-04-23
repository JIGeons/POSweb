package PosWeb.POS.repository;

import PosWeb.POS.domain.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    // 단건 조회(하나만 조회)
    public Member findByMemberId(String memberId) {
        return em.createQuery("select m from Member m where m.memberId = :memberId", Member.class)
                .setParameter("memberId", memberId)
                .getSingleResult();
    }

    // 모든 사용자 조회
    public List<Member> findAll() {
        return em.createQuery(
                "select m from Member m", Member.class
        ).getResultList();
    }

    // 이름으로 사용자 조회
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
