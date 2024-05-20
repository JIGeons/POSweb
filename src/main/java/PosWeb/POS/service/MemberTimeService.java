package PosWeb.POS.service;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.MemberTime;
import PosWeb.POS.repository.MemberTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

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
        int hours = (int) duration.toHours();
        int minutes = (int) duration.toMinutes();

        int hourlyRate = member.getHourlyRate();
        // 일한 시간과 시급을 곱해서 일급을 계산
        int rate = hours * hourlyRate +  (int) Math.round(((double) minutes / 60) * hourlyRate);

        // 일급을 update
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

}
