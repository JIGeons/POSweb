package PosWeb.POS.controller;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.dto.Member.JoinMemberForm;
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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
        JoinMemberForm joinMemberForm = new JoinMemberForm();
        model.addAttribute("joinMemberForm", joinMemberForm);
        return "members/join";
    }

    @PostMapping("members/join")
    public String join(@Valid JoinMemberForm joinMemberForm, BindingResult result, Model model) {

        if (result.hasErrors()) {
            log.info("members Form Error");
            return "members/join";
        }

        // 비밀번호 검증
        if (!joinMemberForm.getPw().equals(joinMemberForm.getConfirmPw())) {
            log.info("members password not equal");
            result.addError(new FieldError("joinMemberForm", "confirmPw", " ※ 비밀번호가 일치하지 않습니다."));
            return "members/join";
        }

        Member member = createMember(joinMemberForm);
        memberService.join(member);
        return "redirect:/";
    }

    public Member createMember(JoinMemberForm joinMemberForm) {

        Member member = new Member();
        member.setStringId(joinMemberForm.getStringId());
        member.setName(joinMemberForm.getName());
        member.setPw(joinMemberForm.getPw());
        member.setAddress(joinMemberForm.getAddress());
        member.setBirth(joinMemberForm.getBirth());

        return member;
    }

    // 회원 아이디 중복 확인
    @GetMapping("members/join/checkId/{stringId}")
    @ResponseBody  // html파일명을 반환하는게 아닌 data 값을 반환하기 때문에 ResponseBody 사용
    public Map<String, Boolean> checkId(@PathVariable String stringId){
        // 여러 쓰레드의 동시 처리를 가능하기 위해 concurrentHashMap 사용
        Map<String, Boolean> response = new ConcurrentHashMap<>();
        boolean isAvailable;
        try {
            isAvailable = memberService.checkId(stringId);
        } catch (IllegalStateException e) {
            isAvailable = false;
        }
        response.put("available", isAvailable);
        return response;
    }

}
