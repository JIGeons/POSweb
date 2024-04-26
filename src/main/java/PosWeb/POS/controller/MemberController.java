package PosWeb.POS.controller;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.dto.Member.JoinMemberDto;
import PosWeb.POS.domain.dto.Member.LoginMemberDto;
import PosWeb.POS.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("members/login")
    public String login(Model model, HttpSession session) {
        List<Member> members = memberService.findMembers();

        // member엔티티의 모든 속성 노출을 방지하기 위해 LoginMemberDto를 사용하여 필요한 정보만 전송
        List<LoginMemberDto> LoginMembers = members.stream()
                                .map(m -> new LoginMemberDto(m.getStringId(), m.getName()))
                                .collect(Collectors.toList());

        session.setAttribute("members", LoginMembers);
        model.addAttribute("loginMember", new LoginMemberDto());
        return "members/login";
    }

    @PostMapping("members/login")
    public String login(
            @Valid @ModelAttribute("loginMember") LoginMemberDto loginMember,
            BindingResult bindingResult,
            HttpServletRequest httpServletRequest,
            Model model) {

        model.addAttribute("loginMember", loginMember);
        System.out.println("loginMember.id = " + loginMember.getStringId());
        System.out.println("loginMember.pw = " + loginMember.getPw());
        Member member = memberService.login(loginMember);


        // 로그인 실패 (아이디나 비밀번호가 틀린 경우)
        if (member == null) {
            bindingResult.reject("loginFail", "로그인 아이디 또는 비밀번호가 틀렸습니다.");

            return "members/login";
        }

        log.info("login? {}", loginMember);

        // 로그인 성공 -> 세션 생성

        // 세션을 생성하기 전 기존의 세션 파기
        httpServletRequest.getSession().invalidate();
        HttpSession session = httpServletRequest.getSession(true);  // Session 생성
        // 세션에 memberId를 삽입
        session.setAttribute("stringId", member.getStringId());

        return "redirect:/order/posweb";
    }

    @GetMapping("members/join")
    public String join(Model model){
        JoinMemberDto joinMemberForm = new JoinMemberDto();
        model.addAttribute("joinMemberForm", joinMemberForm);
        return "members/join";
    }

}
