package PosWeb.POS.controller;

import PosWeb.POS.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {

    @RequestMapping("/")
    public String main() {
        log.info("main controller");
        return "main";
    }
}
