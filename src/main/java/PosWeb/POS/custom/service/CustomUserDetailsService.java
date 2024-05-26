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

    @Override
    public UserDetails loadUserByUsername(String userStringId) throws UsernameNotFoundException {
        Member member = memberRepository.findByMemberId(userStringId);

        if (member == null) {
            throw new UsernameNotFoundException("No user with " + userStringId + " exists in the system");
        }

        return User.builder()
                .username(member.getStringId())
                .password(member.getPw())
                .roles(member.isAdmin() ? "ADMIN" : "USER")
                .build();
    }
}
