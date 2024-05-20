package PosWeb.POS.repository;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.MemberTime;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberTimeRepository {

    private final EntityManager em;

    public void save(MemberTime memberTime) {
        em.persist(memberTime);
    }

    // 단건 조회(하나만 조회)
    public MemberTime findOne(String stringId) {
        return em.createQuery(
                        "select mt from MemberTime mt"
                                + " where mt.member.stringId = :stringId"
                                + " order by mt.startTime desc", MemberTime.class)
                .setParameter("stringId", stringId)
                .setMaxResults(1)
                .getSingleResult();
    }

    // 모든 사용자 조회
    public List<MemberTime> findAll() {
        return em.createQuery(
                "select mt from MemberTime mt", MemberTime.class
        ).getResultList();
    }
}
