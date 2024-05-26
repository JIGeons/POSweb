package PosWeb.POS.custom.filter;

import PosWeb.POS.custom.CustomAuthenticationToken;
import PosWeb.POS.domain.dto.Member.LoginMemberDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import javax.sql.rowset.serial.SerialException;
import java.io.IOException;

@Slf4j
public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public CustomAuthenticationFilter() {
        // url과 일치할 경우 해당 필터가 동작
        super(new AntPathRequestMatcher("/members/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException, IOException, ServletException {
        // 해당 요청이 POST 인지 확인
        if(!isPost(request)) {
            log.info("POST 아님!");
            //return null;
            throw new IllegalStateException("Authentication is not supported");
        }

        // LoginMemberDto 클래스를 기반으로 로그인 요청 파라미터를 읽어옴
        try {
            log.info("POST로 들어옴!");
            System.out.println("이게 뭘까? " + request.getReader().toString());
            LoginMemberDto loginMemberDto = new ObjectMapper().readValue(request.getReader(), LoginMemberDto.class);

            log.info("ID : {}, PW : {}", loginMemberDto.getStringId(), loginMemberDto.getPw());

            // ID, Password가 있는지 확인
            if (!StringUtils.hasLength(loginMemberDto.getStringId())
            || !StringUtils.hasLength(loginMemberDto.getPw())) {
                System.out.println("아이디랑 비밀번호가 없어용");
                throw  new IllegalArgumentException("username or password is empty");
            }

            // 처음에는 인증 되지 않은 토큰 생성
            CustomAuthenticationToken token = new CustomAuthenticationToken(loginMemberDto.getStringId(), loginMemberDto.getPw());

            log.info("인증 되지 않은 토큰 생성");

            // Manager에게 인증 처리
            Authentication authentication = getAuthenticationManager().authenticate(token);
            log.info("인증처리 완료~");

            return authentication;

        } catch (IOException e) {
            log.info("실패!!");
            throw new AuthenticationServiceException("Failed to parse authentication request body", e);
        }
    }

    private boolean isPost(HttpServletRequest request) {

        if("POST".equals(request.getMethod())) {
            return true;
        }

        return false;
    }
}
