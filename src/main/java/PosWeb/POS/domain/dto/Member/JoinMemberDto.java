package PosWeb.POS.domain.dto.Member;

import PosWeb.POS.domain.Address;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class JoinMemberDto {

    @NotEmpty(message = "ID는 필수로 입력해야합니다.")
    private String stringId;
    @NotEmpty(message = "이름은 필수로 입력해야합니다.")
    private String name;
    @NotEmpty(message = "비밀번호는 필수로 입력해야합니다.")
    private String pw;
    private String confirmPw;
    private LocalDate Birth;
    private Address address;

}
