package PosWeb.POS.domain.dto.MemberTime;

import lombok.Getter;

@Getter
public class MemberTimeMonthForm {
    private Long memberId;          // 회원 아이디
    private String memberStringId;  // 회원 String id
    private String memberName;      // 회원 이름
    private double totalTime;       // 일한 총 시간
    private int totalDay;           // 일한 총 일수
    private double avgTime;         // 하루 평균 일한 시간
    private int salary;             // 월급

    public MemberTimeMonthForm(Long id, String stringId, String name, Double totalTime, Long totalDay, Double avgTime, Long salary) {
        this.memberId = id;
        this.memberStringId = stringId;
        this.memberName = name;
        // 소수점 두번째 자리까지만 출력
        this.totalTime = totalTime != null ? Math.round(totalTime * 100) / 100.0 : 0;
        this.totalDay = totalDay != null ? totalDay.intValue() : 0;
        // 소수점 두번째 자리까지만 출력
        this.avgTime = avgTime != null ? Math.round(avgTime * 100) / 100.0 : 0;
        this.salary = salary != null ? salary.intValue() : 0;
    }
}
