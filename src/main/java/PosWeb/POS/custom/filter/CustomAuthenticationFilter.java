package PosWeb.POS.custom.filter;

import PosWeb.POS.custom.CustomAuthenticationToken;
import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.dto.Member.LoginMemberDto;
import PosWeb.POS.service.MemberService;
import PosWeb.POS.service.MemberTimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.validation.*;

import javax.sql.rowset.serial.SerialException;
import java.io.IOException;
import java.net.BindException;

@Slf4j
public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberTimeService memberTimeService;

    public CustomAuthenticationFilter() {
        // url과 일치하고 POST 메서드 일 때 해당 필터가 동작
        super(new AntPathRequestMatcher("/members/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException, IOException, ServletException {
        // 해당 요청이 POST 인지 확인
        if(!isPost(request)) {
            //return null;
            throw new IllegalStateException("Authentication is not supported");
        }

        // LoginMemberDto 클래스를 기반으로 로그인 요청 파라미터를 읽어옴
        LoginMemberDto loginMemberDto = new ObjectMapper().readValue(request.getReader(), LoginMemberDto.class);

        // 처음에는 인증 되지 않은 토큰 생성
        CustomAuthenticationToken token = new CustomAuthenticationToken(loginMemberDto.getStringId(), loginMemberDto.getPw());

        log.info("인증 되지 않은 토큰 생성");

        // 이전 사용자에 대한 정보를 가져온다ㅣ
        Authentication preAuthentication = SecurityContextHolder.getContext().getAuthentication();

        // Manager에게 인증 처리
        Authentication authentication = getAuthenticationManager().authenticate(token);

        // 로그인된 사용자가 있을 때 memberTime객체 update
        if (preAuthentication.isAuthenticated()) {
            memberTimeService.updateEndTime(memberService.findOne(preAuthentication.getName()));
        }

        return authentication;
    }

    private boolean isPost(HttpServletRequest request) {

        if("POST".equals(request.getMethod())) {
            return true;
        }

        return false;
    }
}
