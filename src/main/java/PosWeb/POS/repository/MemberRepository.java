package PosWeb.POS.repository;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.dto.Member.MemberDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public Member findByMemberId(String stringId) {
        try {
            return em.createQuery("select m from Member m where m.stringId = :stringId", Member.class)
                    .setParameter("stringId", stringId)
                    .getSingleResult();
        } catch (NoResultException ex) {    // 쿼리가 없을 경우  null을 반환한다.
            return null;
        }
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

    public Page<MemberDto> findMembersWithPaging(String searchName, Pageable pageable) {
        String jpql = "select new " +
                " PosWeb.POS.domain.dto.Member.MemberDto(m.id, m.stringId, m.name, m.birth, m.admin, m.hourlyRate, m.weekOrMonth, m.address)"+
                " from Member m";

        // 검색어가 있을 경우 조건문 추가
        if (searchName != null)
            jpql += " where m.name = :searchName";

        // 쿼리 생성
        Query query = em.createQuery(jpql, MemberDto.class);

        // 검색어가 있을 경우 파라미터 추가
        if (searchName != null)
            query.setParameter("searchName", searchName);

        // member 조회
        List<MemberDto> members = query.setFirstResult((int) pageable.getOffset()) // 페이지 번호에 따라 결과를 시작하는 위치 설정
                                        .setMaxResults(pageable.getPageSize())  // 페이지 크기 설정
                                        .getResultList();

        return new PageImpl<>(members, pageable, pageable.getPageNumber());
    }
}
