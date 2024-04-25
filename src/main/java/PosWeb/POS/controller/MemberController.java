package PosWeb.POS.controller;

import PosWeb.POS.domain.Member;
import PosWeb.POS.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/login")
    public String login(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/login";
    }
}
