package PosWeb.POS.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
@Tag(name = "메인 컨트롤러", description = "메인 API")
public class MainController {

    @Operation(summary = "메인 페이지 출력", description = "메인 페이지를 출력한다.")
    @GetMapping("/")
    public String main() {
        log.info("main controller");
        return "main";
    }
}
