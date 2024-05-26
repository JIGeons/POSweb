package PosWeb.POS.service;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.MemberTime;
import PosWeb.POS.domain.dto.MemberTime.MemberTimeDto;
import PosWeb.POS.domain.dto.MemberTime.MemberTimeMonthForm;
import PosWeb.POS.repository.MemberTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberTimeService {

    private final MemberTimeRepository memberTimeRepository;

    // 사용자 전환 & 마감 시 member의 종료시간 update
    @Transactional
    public MemberTime updateEndTime(Member member) {
        // 해당 member의 정보를 불러온다.
        MemberTime updateMember = memberTimeRepository.findOne(member.getStringId());
        updateMember.setEndTime(LocalDateTime.now());

        LocalDateTime startTime = updateMember.getStartTime();
        LocalDateTime endTime = updateMember.getEndTime();

        // 두 개의 시간 객체 사이의 차이를 계산할 때 사용하는 클래스
        Duration duration = Duration.between(startTime, endTime);
        // 시간 차이를 분으로 계산 소수점 세번째 자리에서 반올림
        double time = Math.round((int) duration.toMinutes() / 60.0 * 100) / 100.0;

        int hourlyRate = member.getHourlyRate();
        // 일한 시간과 시급을 곱해서 일급을 계산
        int rate = (int) Math.round(time * hourlyRate);

        // 일한 시간과 일급을 update
        updateMember.setTime(time);
        updateMember.setRate(rate);

        return updateMember;
    }

    @Transactional
    public MemberTime createMemberTime(Member member) {
        // memberTime 객체를 생성
        MemberTime createMemberTime = MemberTime.createMemberTime(member);

        log.info("createMemberTime Data ? {}", createMemberTime);

        // 생성한 memberTime을 저장
        memberTimeRepository.save(createMemberTime);

        return createMemberTime;
    }

    // memberTime 정보에 대해 페이징 처리를 해서 반환
    public Page<MemberTimeMonthForm> findMemberTimeForm(LocalDate searchDate, String searchName, int page, int size) {
        // 검색 달의 첫째날
        LocalDateTime startDate = searchDate.withDayOfMonth(1).atTime(0,0,0);
        // 검색 달의 마지막 날
        LocalDateTime endDate = searchDate.plusMonths(1).withDayOfMonth(1).atTime(0,0,0);
        System.out.println("시작 : " + startDate + ", 끝 : " + endDate);

        // page 객체 생성
        Pageable pageable = PageRequest.of(page, size);

        return memberTimeRepository.findMemberTimeWithDate(startDate, endDate, searchName, pageable);
    }

    // memberIds 리스트에 있는 id만 memberTime 정보 조회
    public Map<Long, List<MemberTimeDto>> findMemberTimeDto(LocalDate searchDate, List<Long> memberIds, String searchName) {
        // 검색 달의 첫째날
        LocalDateTime startDate = searchDate.withDayOfMonth(1).atTime(0,0,0);
        // 검색 달의 마지막 날
        LocalDateTime endDate = searchDate.plusMonths(1).withDayOfMonth(1).atTime(0,0,0);

        List<MemberTimeDto> memberTimeDtoList = memberTimeRepository.findMemberTimeWithDateMap(startDate, endDate, memberIds, searchName);

        Map<Long, List<MemberTimeDto>> memberTimeDtoMap = memberTimeDtoList.stream()
                .collect(Collectors.groupingBy(MemberTimeDto::getMemberId));

        return memberTimeDtoMap;
    }
}
