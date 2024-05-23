package PosWeb.POS.repository;

import PosWeb.POS.domain.MemberTime;
import PosWeb.POS.domain.dto.MemberTime.MemberTimeDto;
import PosWeb.POS.domain.dto.MemberTime.MemberTimeMonthForm;
import jakarta.persistence.EntityManager;
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
    public Page<MemberTimeMonthForm> findMemberTimeWithDate(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        List<MemberTimeMonthForm> memberTimeMonthForms = em.createQuery(
                " select new " +
                        " PosWeb.POS.domain.dto.MemberTime.MemberTimeMonthForm(m.id, m.stringId, m.name, sum (mt.time), count (mt.id), avg (mt.time), sum (mt.rate))" +
                        " from MemberTime mt" +
                        " join mt.member m" +
                        // 일 시작 날짜로 검색
                        " where mt.startTime >= :startDate and mt.startTime < :endDate" +
                        // member id와 name으로 그룹화
                        " group by m.id", MemberTimeMonthForm.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(memberTimeMonthForms, pageable, pageable.getPageNumber());
    }

    // memberIds에 있는 id로 memberTime 정보 불러오기
    public List<MemberTimeDto> findMemberTimeWithDateMap(LocalDateTime startDate, LocalDateTime endDate, List<Long> memberIds) {
        return em.createQuery("select new PosWeb.POS.domain.dto.MemberTime.MemberTimeDto(mt)" +
                " from MemberTime mt" +
                " join mt.member m" +
                " where mt.startTime >= :startDate and mt.startTime < :endDate and m.id in :memberIds", MemberTimeDto.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("memberIds", memberIds)
                .getResultList();
    }
}
