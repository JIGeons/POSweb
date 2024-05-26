package PosWeb.POS.controller;

import PosWeb.POS.domain.Address;
import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.MemberTime;
import PosWeb.POS.domain.dto.Member.JoinMemberForm;
import PosWeb.POS.domain.dto.Member.LoginMemberDto;
import PosWeb.POS.domain.dto.Member.MemberDto;
import PosWeb.POS.domain.dto.MemberTime.MemberTimeDto;
import PosWeb.POS.domain.dto.MemberTime.MemberTimeMonthForm;
import PosWeb.POS.service.MemberService;
import PosWeb.POS.service.MemberTimeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
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
        member.setBirth(joinMemberForm.getBirth());
        member.setAddress(new Address(joinMemberForm.getZipcode(), joinMemberForm.getStreetAdr(), joinMemberForm.getDetailAdr()));

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

    @GetMapping("members/management")
    public String memberManagement(@RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "memberTag", defaultValue = "admin") String memberTag,
                                   @RequestParam(value = "searchName", defaultValue = "") String searchName,
                                   @RequestParam(value = "searchDate", defaultValue = "") String searchDate,
                                   Model model) {

        int size = 10;  // 10개씩 페이징 한다.
        LocalDate date;

        if (memberTag.equals("salary")) {    // 월급 관리

            if (searchDate.equals("")) {
                date = LocalDate.now();

                searchDate = date.getYear() + "-";
                if (date.getMonthValue() < 10)
                    searchDate += "0" + date.getMonthValue();
                else
                    searchDate += date.getMonthValue();

                System.out.println("날짜 : " + searchDate);
            } else {
                date = LocalDate.parse(searchDate + "-01");
            }

            // searchDate를 기준으로 memberTime 객체 검색
            Page<MemberTimeMonthForm> memberTimes = memberTimeService.findMemberTimeForm(date, searchName, page, size);

            // memberTime의 객체를 불러오기 위한 id 리스트 생성
            List<Long> memberIds = new ArrayList<>();

            // 리스트에 member_id 추가
            for (MemberTimeMonthForm mtmf : memberTimes.toList()) {
                memberIds.add(mtmf.getMemberId());
                System.out.println("추가 : " + mtmf.getMemberId());
            }

            // memberIds에 있는 id의 정보만 불러와서 Map형식으로 grouping
            Map<Long, List<MemberTimeDto>> memberTimesGroup = memberTimeService.findMemberTimeDto(date, memberIds, searchName);

            // 모델에 객체 추가
            model.addAttribute("memberTimesPaging", memberTimes);
            model.addAttribute("memberTimesGroup", memberTimesGroup);
            model.addAttribute("searchDate", searchDate);
        } else {    // 관리자 & 회원정보 수정
            // 페이징된 memberDto 객체를 불러 온다.
            Page<MemberDto> memberPaging = memberService.findMembersWithPaging(searchName, page, size);

            model.addAttribute("memberDtoList", memberPaging);
        }

        // 회원정보 수정 시 회원을 인증하기 위한 JoinMemberForm을 빈 객체로 생성해서 전달.
        JoinMemberForm join = new JoinMemberForm();
        join.setCheckId(true);
        model.addAttribute("memberUpdate", join);
        model.addAttribute("memberTag", memberTag);
        return "members/management";
    }

    @PostMapping("members/management/admin")
    public String updateAdmin(@RequestParam Map<String, String> updateAdminList,
                              @ModelAttribute("memberTag") String memberTag) {

        List<Long> ids = updateAdminList.values().stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // Map 형식으로 전달받은 id를 list형식으로 변경한 후 admin을 true로 변경한다.
        memberService.updateAdmin(ids);

        return "redirect:/members/management?memberTag=" + memberTag;
    }

    @PostMapping("members/management/memberSet")
    public String updateMember(@RequestParam("page") int page,
                               @Valid @ModelAttribute("memberUpdate") JoinMemberForm memberUpdate,
                               BindingResult bindingResult,
                               Model model) {

        // member객체 페이징 처리
        String searchName = "";
        int size = 10;
        Page<MemberDto> memberDtoList = memberService.findMembersWithPaging(searchName, page, size);
        model.addAttribute("memberDtoList", memberDtoList);
        model.addAttribute("memberUpdate", memberUpdate);
        model.addAttribute("memberTag", "memberSet");

        System.out.println("memberUPdate : " + memberUpdate.getName());

        // JoinMemberForm에 오류가 있을 경우
        if (bindingResult.hasErrors()) {
            log.info("members Form Error");
            System.out.println(bindingResult.toString());
            return "members/management";
        }

        Member member = memberService.findOne(memberUpdate.getStringId());
        // 비밀번호가 일치 하지 않을 경우
        if (!memberUpdate.getPw().equals(member.getPw())) {
            log.info("members password not equal");
            bindingResult.addError(new FieldError("joinMemberForm", "confirmPw", " ※ 비밀번호가 일치하지 않습니다."));
            return "members/management";
        }

        // 비밀번호가 일치하는 경우
        memberService.update(memberUpdate);

        model.addAttribute("memberTag", "memberSet");

        return "redirect:/members/management?memberTag=memberSet";
    }

    @GetMapping("members/logout")
    public String end(HttpServletRequest servletRequest) {

        HttpSession session = servletRequest.getSession();
        // 로그인 돼 있는 사용자의 정보를 받아온다.
        String loginId = (String) session.getAttribute("loginMember");

        loginId = "sangjun02";

        // 해당 사용자의 정보를 검색
        Member loginMember = memberService.findOne(loginId);
        // 현재 시간으로 endTime update
        MemberTime endMember = memberTimeService.updateEndTime(loginMember);

        log.info("{}이 {}시에 마감을 하였습니다.", loginMember.getName(), endMember.getEndTime());

        // 세션 파기
        session.invalidate();
        return "redirect:/";
    }
}
