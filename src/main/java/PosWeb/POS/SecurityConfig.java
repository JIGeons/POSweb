package PosWeb.POS;

import PosWeb.POS.custom.CustomLoginAuthenticationEntrypoint;
import PosWeb.POS.custom.filter.CustomAuthenticationFilter;
import PosWeb.POS.custom.handler.CustomAuthenticationFailureHandler;
import PosWeb.POS.custom.handler.CustomAuthenticationSuccessHandler;
import PosWeb.POS.custom.handler.CustomLogoutSuccessHandler;
import PosWeb.POS.custom.service.CustomUserDetailsService;
import PosWeb.POS.service.MemberService;
import PosWeb.POS.service.MemberTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.validation.Validator;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    private final CustomLoginAuthenticationEntrypoint customLoginAuthenticationEntrypoint;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final Validator validator;

    // password 암호화를 위한 BCryptPasswordEncoder 클래스 생성 & 빈 등록
    @Bean
    public static BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // resource에 접근할 수 있도록 빈 추가
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }


    // 인증 관리자를 빈으로 등록
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // SecurityFilterChain 빈 등록 (springBoot 6.1버전 이후 WebSecurityConfigurerAdapter 를 사용하지 않아 SecurityFilterChain을 빈으로 등록해서 사용한다.)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager am) throws Exception {

        // csrf 비활성화 ( 사이트 위변조 방지 )
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(AbstractHttpConfigurer::disable);

        // 권한에 따른 URL 접근 제어 설정
        http.authorizeHttpRequests(requests -> requests
                // 로그인 없이 접근 가능
                .requestMatchers("/", "/members/login", "/members/login-error", "/members/join").permitAll()
                // 나머지 요청은 인증 필요
                .anyRequest().authenticated()
        );

        // 로그인 설정
        http.formLogin((form) -> form
                .loginPage("/members/login")    // 사용자 정의 로그인 페이지
                .failureUrl("/members/login")
                .permitAll()
        );

        // custom filter 추가
        http.addFilterAt(authenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/members/logout")   // 로그아웃 url
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .logoutSuccessUrl("/")          // 로그아웃 성공 후 이동 페이지
                .invalidateHttpSession(true)    // 세션 무효화
                .permitAll());

        return http.build();
    }

    @Bean
    public CustomAuthenticationFilter authenticationFilter() throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter();
        customAuthenticationFilter.setAuthenticationManager(authenticationManager());

        // 로그인 성공 시 로직 추가
        customAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler);
        // 로그인 실패 시 로직 추가
        customAuthenticationFilter.setAuthenticationFailureHandler(customAuthenticationFailureHandler);

        customAuthenticationFilter.setSecurityContextRepository(
                new DelegatingSecurityContextRepository(
                        new RequestAttributeSecurityContextRepository(),
                        new HttpSessionSecurityContextRepository()
                ));

        return customAuthenticationFilter;
    }
}
