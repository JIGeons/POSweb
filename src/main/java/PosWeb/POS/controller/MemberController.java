package PosWeb.POS.controller;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.dto.Member.JoinMemberForm;
import PosWeb.POS.domain.dto.Member.LoginMemberDto;
import PosWeb.POS.domain.dto.Member.MemberDto;
import PosWeb.POS.domain.dto.MemberTime.MemberTimeDto;
import PosWeb.POS.domain.dto.MemberTime.MemberTimeMonthForm;
import PosWeb.POS.service.MemberService;
import PosWeb.POS.service.MemberTimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@Tag(name = "멤버 컨트롤러", description = "멤버 API")
public class MemberController {

    private final MemberService memberService;
    private final MemberTimeService memberTimeService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "회원 로그인 Get", description = "로그인 정보를 받기위한 빈 LoginMemberDto객체를 생성하고 회원 정보를 조회하여 " +
                                                          "클라이언트로 전송, 로그인시 문제가 생겨 리디렉션이 되면 error 메시지를 출력")
    @GetMapping("members/login")
    public String login(Model model, HttpSession session) {
        List<Member> members = memberService.findMembers();

        // member엔티티의 모든 속성 노출을 방지하기 위해 LoginMemberDto를 사용하여 필요한 정보만 전송
        List<LoginMemberDto> LoginMembers = members.stream()
                                .map(m -> new LoginMemberDto(m.getStringId(), m.getName()))
                                .collect(Collectors.toList());

        model.addAttribute("errMsg", "");
        // 세션 체크
        if (session.getAttribute("errMsg") != null) {
            // 모델에 error massage 추가
            model.addAttribute("errMsg", session.getAttribute("errorMessage"));
            // 세션 해제
            session.removeAttribute("errMsg");
        }

        model.addAttribute("members", LoginMembers);
        model.addAttribute("loginMemberDto", new LoginMemberDto());
        return "members/login";
    }

    @Operation(summary = "회원가입 Get",
            description = "새로운 회원 정보를 입력받기 위한 빈 JoinMemberForm 객체를 생성하여 클라이언트로 전송한다.")
    @GetMapping("members/join")
    public String join(Model model){
        JoinMemberForm joinMemberForm = new JoinMemberForm();
        model.addAttribute("joinMemberForm", joinMemberForm);
        return "members/join";
    }

    @Operation(summary = "회원가입 Post",
            description = "입력받은 정보에 대해 validation을 진행하고 문제가 있을 시 bindigResult에 " +
                    "오류 내용을 담아 클라이언트로 전송, 문제가 없을 시 새로운 member 객체를 생성하고 저장")
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
            bindingResult.addError(new FieldError(
                    "joinMemberForm",
                    "confirmPw",
                    " ※ 비밀번호가 일치하지 않습니다."));
            return "members/join";
        }

        // 회원 생성
        memberService.join(joinMemberForm);
        return "redirect:/";
    }

    // 회원 아이디 중복 확인
    @Operation(summary = "회원 아이디 중복 확인",
            description = "회원 아이디를 조회하고 중복이 있을 경우 false를 반환하고, 중복이 없을 경우 true를 반환")
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

    @Operation(summary = "사용자 관리",
            description = "memberTag가 salary가 아닐 시 사용자 정보를 페이징, " +
                    "회원 정보의 수정 정보를 받기 위해 JoinMemberForm 객체를 생성," +
                    " memberTag가 salary일 경우에는 member별 memberTime 정보를 grouping해서 클라이언트로 전송")
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

    @Operation(summary = "사용자의 관리자 정보 수정",
            description = "전달 받은 사용자의 id로 사용자를 조회한 후 사용자의 관리자 정보를 수정하고 management 페이지를 리디렉션 한다.")
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

    @Operation(summary = "사용자 정보 수정",
            description = "전달 받은 JoinMemberForm 객체를 통해 비밀번호를 검사하고 일치하지 않을 시 오류를 클라이언트로 전송하고, " +
                          "비밀번호 일치시 해당 사용자의 정보를 수정 후 management페이지를 리디렉션 한다.")
    @PostMapping("members/management/memberSet")
    public String updateMember(@RequestParam("page") int page,
                               @Valid @ModelAttribute("memberUpdate") JoinMemberForm memberUpdate,
                               BindingResult bindingResult, Model model) {
        // member객체 페이징 처리
        String searchName = "";
        int size = 10;
        Page<MemberDto> memberDtoList = memberService.findMembersWithPaging(searchName, page, size);
        model.addAttribute("memberDtoList", memberDtoList);
        model.addAttribute("memberUpdate", memberUpdate);
        model.addAttribute("memberTag", "memberSet");

        // JoinMemberForm에 오류가 있을 경우
        if (bindingResult.hasErrors()) {
            log.info("members Form Error");
            return "members/management";
        }
        Member member = memberService.findOne(memberUpdate.getStringId());
        // 비밀번호가 일치 하지 않을 경우
        if (!passwordEncoder.matches(memberUpdate.getPw(), member.getPw())) {
            log.info("members password not equal");
            bindingResult.addError(new FieldError("joinMemberForm", "confirmPw", " ※ 비밀번호가 일치하지 않습니다."));
            return "members/management";
        }
        // 비밀번호가 일치하는 경우
        memberService.update(memberUpdate);
        model.addAttribute("memberTag", "memberSet");
        return "redirect:/members/management?memberTag=memberSet";
    }
}
