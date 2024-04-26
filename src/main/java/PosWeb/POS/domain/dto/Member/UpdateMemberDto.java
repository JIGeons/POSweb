package PosWeb.POS.domain.dto.Member;

import PosWeb.POS.domain.Address;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UpdateMemberDto {

    private String memberId;
    private String name;
    private LocalDate Birth;
    private boolean admin;
    private int hourlyRate;
    private Boolean weekOrMonth;
    private Address address;
}
