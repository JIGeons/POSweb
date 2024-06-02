package PosWeb.POS.custom.service;

import PosWeb.POS.domain.Member;
import PosWeb.POS.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 주어진 사용자 이름(userStringId)을 사용하여 사용자의 세부 정보를 로드하는 메서드
    @Override
    public UserDetails loadUserByUsername(String userStringId) throws UsernameNotFoundException {
        // 사용자 ID로 데이터베이스에서 회원 정보를 조회
        Member member = memberRepository.findByMemberId(userStringId);

        // 회원 정보를 찾을 수 없는 경우 예외를 발생
        if (member == null) {
            throw new UsernameNotFoundException("No user with " + userStringId + " exists in the system");
        }

        // UserDetails 객체를 빌더 패턴을 사용하여 생성하고 반환
        return User.builder()
                .username(member.getStringId())     // 사용자 이름 설정
                .password(member.getPw())           // 비밀번호 설정
                .roles(member.isAdmin() ? "ADMIN" : "USER") // 역할 설정 (관리자 여부에 따라 ADMIN, USER 설정)
                .build();
    }
}
