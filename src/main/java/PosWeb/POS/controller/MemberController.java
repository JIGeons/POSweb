package PosWeb.POS.controller;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.dto.Member.LoginMemberDto;
import PosWeb.POS.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/login")
    public String login(Model model) {
        List<Member> members = memberService.findMembers();

        // member엔티티의 모든 속성 노출을 방지하기 위해 LoginMemberDto를 사용하여 필요한 정보만 전송
        List<LoginMemberDto> LoginMembers = members.stream()
                                .map(m -> new LoginMemberDto(m.getStringId(), m.getName()))
                                .collect(Collectors.toList());

        model.addAttribute("members", LoginMembers);
        return "members/login";
    }
}
