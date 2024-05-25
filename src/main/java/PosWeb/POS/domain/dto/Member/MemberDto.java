package PosWeb.POS.domain.dto.Member;

import PosWeb.POS.domain.Address;
import PosWeb.POS.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
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
    private String stringAds;

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

    public MemberDto(Long id, String stringId, String name, LocalDate birth, boolean admin, int hourlyRate, boolean weekOrMonth, Address address) {
        this.id = id;
        this.stringId = stringId;
        this.name = name;
        this.birth = birth;
        this.admin = admin;
        this.hourlyRate = hourlyRate;
        this.weekOrMonth = weekOrMonth;
        this.address = address;
        this.stringAds = address != null ? address.getStreetAdr() + " " + address.getDetailAdr() : null;
    }
}
