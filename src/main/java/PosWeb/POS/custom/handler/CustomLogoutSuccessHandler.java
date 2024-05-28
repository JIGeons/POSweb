package PosWeb.POS.custom.handler;

import PosWeb.POS.domain.Member;
import PosWeb.POS.service.MemberService;
import PosWeb.POS.service.MemberTimeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final MemberService memberService;
    private final MemberTimeService memberTimeService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        // 로그아웃 하는 사용자에 대해서 종료 시간 입력
        memberTimeService.updateEndTime(memberService.findOne(authentication.getName()));
        response.sendRedirect("/");
    }
}
