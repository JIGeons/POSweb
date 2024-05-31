package PosWeb.POS.custom.handler;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.MemberTime;
import PosWeb.POS.service.MemberService;
import PosWeb.POS.service.MemberTimeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService;
    private final MemberTimeService memberTimeService;

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        DefaultSavedRequest savedRequest = (DefaultSavedRequest) requestCache.getRequest(request, response);

        // 로그인 성공 후 로직 처리하기
        String stringId = authentication.getName();
        Member member = memberService.findOne(stringId);

        if (member != null) {
            // 로그인 하는 member에 대해 memberTime 객체를 생성 & startTime 현재 시간으로 설정
            MemberTime createMemberTime = memberTimeService.createMemberTime(member);
            log.info("로그인 memberTime 객체 생성 : {}", createMemberTime.getStartTime());
        }

        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            response.addHeader("targetUrl", targetUrl);
        } else {
            // 로그인 성공 후 items 페이지 리디렉션
            String targetUrl = "/items";
            response.addHeader("targetUrl", targetUrl);
        }
    }
}
