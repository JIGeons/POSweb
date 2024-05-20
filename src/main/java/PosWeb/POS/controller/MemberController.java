package PosWeb.POS.controller;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.MemberTime;
import PosWeb.POS.domain.dto.Member.JoinMemberForm;
import PosWeb.POS.domain.dto.Member.LoginMemberDto;
import PosWeb.POS.service.MemberService;
import PosWeb.POS.service.MemberTimeService;
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
    private final MemberTimeService memberTimeService;

    @GetMapping("members/login")
    public String login(Model model, HttpServletRequest servletRequest) {
        List<Member> members = memberService.findMembers();

        // member엔티티의 모든 속성 노출을 방지하기 위해 LoginMemberDto를 사용하여 필요한 정보만 전송
        List<LoginMemberDto> LoginMembers = members.stream()
                                .map(m -> new LoginMemberDto(m.getStringId(), m.getName()))
                                .collect(Collectors.toList());

        HttpSession session = servletRequest.getSession();
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

        HttpSession session = httpServletRequest.getSession();

        model.addAttribute("loginMember", loginMember);
        Member member = memberService.login(loginMember);

        // valid 검사에서 오류가 있는 경우
        if (bindingResult.hasErrors()) {
            return "members/login";
        }

        // 로그인 실패 (아이디나 비밀번호가 틀린 경우)
        if (member == null) {
            bindingResult.reject("loginFail", "로그인 아이디 또는 비밀번호가 틀렸습니다.");

            return "members/login";
        }

        // 로그인 성공에 대해 로그 출력
        log.info("login? {}", loginMember);


        // 로그인 성공 -> memberTime 객체 & 세션 생성

        // 로그인 성공 시 session에 로그인 정보가 있는지 확인
        String loginId = (String) session.getAttribute("loginMember");
        log.info("loginId : {}", loginId);

        // 세션에 로그인 정보가 있을 경우 사용자 전환을 하는 것이므로 memberTime 객체의 endTime을 현재 시간으로 update
        if (loginId != null) {
            Member endMember = memberService.findOne(loginId);
            MemberTime updateMemberTime = memberTimeService.updateEndTime(endMember);
            log.info("로그아웃 & memberTime update {} ", updateMemberTime);
        }

        // 로그인 하는 member에 대해 memberTime 객체를 생성 & startTime 현재 시간으로 설정
        MemberTime createMemberTime = memberTimeService.createMemberTime(member);
        log.info("로그인 memberTime 객체 생성 : {}", createMemberTime.getStartTime());

        // 세션을 생성하기 전 기존의 세션 파기
        httpServletRequest.getSession().invalidate();
        session = httpServletRequest.getSession(true);  // Session 생성
        // 세션에 memberId를 삽입
        session.setAttribute("loginMember", member.getStringId());

        return "redirect:/items";
    }

    @GetMapping("members/join")
    public String join(Model model){
        JoinMemberForm joinMemberForm = new JoinMemberForm();
        model.addAttribute("joinMemberForm", joinMemberForm);
        return "members/join";
    }

    @PostMapping("members/join")
    public String join(@Valid JoinMemberForm joinMemberForm, BindingResult bindingResult) {

        // JoinMemberForm에 오류가 있을 경우
        if (bindingResult.hasErrors()) {
            log.info("members Form Error");
            return "members/join";
        }

        // 비밀번호 검증
        if (!joinMemberForm.getPw().equals(joinMemberForm.getConfirmPw())) {
            log.info("members password not equal");
            bindingResult.addError(new FieldError("joinMemberForm", "confirmPw", " ※ 비밀번호가 일치하지 않습니다."));
            return "members/join";
        }

        // 회원 생성
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
