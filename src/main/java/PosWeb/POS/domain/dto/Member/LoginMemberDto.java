package PosWeb.POS.domain.dto.Member;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginMemberDto {

    @NotEmpty(message = "회원 Id는 필수 입니다.")
    private String stringId;
    private String name;
    private String pw;

    public LoginMemberDto(String stringId, String name) {
        this.stringId = stringId;
        this.name = name;
    }
}
