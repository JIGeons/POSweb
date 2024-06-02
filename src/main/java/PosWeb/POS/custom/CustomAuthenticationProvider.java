package PosWeb.POS.custom;

import PosWeb.POS.custom.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    // 사용자의 인증 정보를 검증하는 메서드
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 사용자가 입력한 로그인 ID와 비밀번호를 가져온다.
        String loginId = authentication.getName();
        String password = (String) authentication.getCredentials();

        // 로그인 ID를 통해 사용자의 세부 정보를 로드한다.
        UserDetails user = (UserDetails) customUserDetailsService.loadUserByUsername(loginId);

        // 입력된 비밀번호와 저장된 비밀번호가 일치하는지 확인한다.
        if(!passwordEncoder.matches(password, user.getPassword())) {
            // 비밀번호가 일치하지 않으면 예외 발생
            throw new BadCredentialsException("Invalid Password");
        }

        // 인증이 성공하면 CustomAuthenticationToken을 반환한다.
        return new CustomAuthenticationToken(user, null, user.getAuthorities());
    }

    // 이 AuthenticationProvider가 특정 인증 클래스를 지원하는지 여부를 반환한다.
    @Override
    public boolean supports(Class<?> authentication) {
        // CustomAuthenticationToken 클래스를 지원
        return authentication.equals(CustomAuthenticationToken.class);
    }
}
