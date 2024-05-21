package PosWeb.POS.domain.dto.Member;

import PosWeb.POS.domain.Address;
import PosWeb.POS.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {
    private Long id;
    private String stringId;
    private String name;
    private LocalDate birth;
    private boolean admin;
    private int hourlyRate;
    private Boolean weekOrMonth;
    private Address address;

    public MemberDto(Member member) {
        id = member.getId();
        stringId = member.getStringId();
        name = member.getName();
        birth = member.getBirth();
        admin = member.isAdmin();
        hourlyRate = member.getHourlyRate();
        weekOrMonth = member.getWeekOrMonth();
        address = member.getAddress();
    }
}
