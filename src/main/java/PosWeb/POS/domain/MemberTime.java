package PosWeb.POS.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTime {

    @Id @GeneratedValue
    @Column(name = "member_time_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime startTime;    // 시작 시간
    private LocalDateTime endTime;      // 끝난 시간
    private Double time = 0.0;                // 일 한 시간
    private int rate;                   // 일급

    //== 생성 메소드 ==//

    /**
     * MemberTime 객체 생성, 시작 시간 기록
     */
    public static MemberTime createMemberTime(Member member) {
        MemberTime memberTime = new MemberTime();
        memberTime.setMember(member);
        memberTime.setStartTime(LocalDateTime.now());
        return memberTime;
    }

}
