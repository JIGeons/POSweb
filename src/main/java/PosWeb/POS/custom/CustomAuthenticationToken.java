package PosWeb.POS.custom;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.util.Assert;

import java.io.Serial;
import java.util.Collection;

// 스프링 시큐리티에서 인증 토큰으로 사용하는 AbstractAuthenticationToken을 상속받아 만든 클래스이다.
// AbstractAuthenticationToken과 코드가 같다.
public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    // 사용자 정보를 나타내는 객체
    private final Object principal;

    // 자격 증명을 나타내는 객체
    private Object credentials;

    // 인증 전 생성자
    public CustomAuthenticationToken(Object principal, Object credentials) {
        super(null);    // 인증 전 권한 없음
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);    // 인증 상태를 false로 설정
    }

    // 인증 후 생성자
    public CustomAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);             // 인증 후 권한 설정
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);   // 인증 상태를 true로 설정
    }

    @Override
    public Object getCredentials() {
        return this.credentials;    // 자격 증명 반환
    }

    @Override
    public Object getPrincipal() {
        return this.principal;      // 사용자 정보 반환
    }

    // 인증 상태를 설정하는 메서드 (재정의하여 직접 설정하는 것을 방지)
    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated,
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);  // 인증 상태를 false로 강제 설정
    }

    // 자격 증명을 지우는 메서드
    @Override
    public void eraseCredentials() {
        super.eraseCredentials();   // 부모 클래스의 자격 증명 지우기 메서드 호출
        this.credentials = null;    // 자격 증명을 null로 설정
    }
}
