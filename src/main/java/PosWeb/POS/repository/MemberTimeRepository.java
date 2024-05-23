package PosWeb.POS.repository;

import PosWeb.POS.domain.MemberTime;
import PosWeb.POS.domain.dto.MemberTime.MemberTimeDto;
import PosWeb.POS.domain.dto.MemberTime.MemberTimeMonthForm;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // MemberTime의 정보를 MemberTimeMonthForm으로 페이징 처리해서 전송하기.
    public Page<MemberTimeMonthForm> findMemberTimeWithDate(LocalDateTime startDate, LocalDateTime endDate, String searchName, Pageable pageable) {
        String jpql = " select new " +
                " PosWeb.POS.domain.dto.MemberTime.MemberTimeMonthForm(m.id, m.stringId, m.name, sum (mt.time), count (mt.id), avg (mt.time), sum (mt.rate))" +
                " from MemberTime mt" +
                " join mt.member m" +
                // 일 시작 날짜로 검색
                " where mt.startTime >= :startDate and mt.startTime < :endDate";

        // searchName이 null이 아닌경우 조건문 추가
        if (!searchName.equals("")) {
            jpql += " and m.name = :searchName";
        }

        // member id와 name으로 그룹화
        jpql += " group by m.id";

        // 쿼리 생성
        Query query = em.createQuery(jpql, MemberTimeMonthForm.class);

        if (!searchName.equals(""))
            query.setParameter("searchName", searchName);

        List<MemberTimeMonthForm> memberTimeMonthForms = query.setParameter("startDate", startDate)
                                                            .setParameter("endDate", endDate)
                                                            .setFirstResult((int) pageable.getOffset())
                                                            .setMaxResults(pageable.getPageSize())
                                                            .getResultList();

        return new PageImpl<>(memberTimeMonthForms, pageable, pageable.getPageNumber());
    }

    // memberIds에 있는 id로 memberTime 정보 불러오기
    public List<MemberTimeDto> findMemberTimeWithDateMap(LocalDateTime startDate, LocalDateTime endDate, List<Long> memberIds, String searchName) {
        String jpql = "select new PosWeb.POS.domain.dto.MemberTime.MemberTimeDto(mt)" +
                " from MemberTime mt" +
                " join mt.member m" +
                " where mt.startTime >= :startDate and mt.startTime < :endDate and m.id in :memberIds";

        // 검색어가 있을 경우 조건문 추가
        if(!searchName.equals(""))
            jpql += " m.name = :searchName";

        Query query = em.createQuery(jpql, MemberTimeDto.class);

        // 검색어가 있을 경우 파라미터 추가
        if (!searchName.equals(""))
            query.setParameter("searchName", searchName);

        return query.setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("memberIds", memberIds)
                .getResultList();
    }
}
