package PosWeb.POS.domain.dto.Member;

import PosWeb.POS.domain.Address;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class JoinMemberForm {

    @NotEmpty(message = " ※ ID는 필수로 입력해야합니다.")
    private String stringId;
    @AssertTrue(message = " ※ 아이디 중복체크를 해야합니다.")
    private boolean checkId;
    @NotEmpty(message = " ※ 이름은 필수로 입력해야합니다.")
    private String name;
    @NotEmpty(message = " ※ 비밀번호는 필수로 입력해야합니다.")
    private String pw;
    private String confirmPw;
    private LocalDate birth;
    private String zipcode;
    private String streetAdr;
    private String detailAdr;
}
