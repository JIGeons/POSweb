package PosWeb.POS.service;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.dto.Member.LoginMemberDto;
import PosWeb.POS.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원가입 service
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    // ID 중복 검사
    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByMemberId(member.getStringId());

        // findMember가 null이 아닐 경우 이미 존재하는 아이디이므로 exception 발생
        // stringId는 unique속성
        if (findMember != null) {
            throw new IllegalStateException("이미 존재하는 아이디 입니다.");
        }
    }

    public Member login(LoginMemberDto loginMemberDto) {
        Member loginMember = memberRepository.findByMemberId(loginMemberDto.getStringId());

        // stringId와 일치하는 member가 없으면 null return
        if(loginMember == null) {
            return null;
        } else {
            if (loginMember.getPw().equals(loginMemberDto.getPw())) {
                return loginMember;
            }
            return null;
        }

    }

    // 회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(String memberId) {
        return memberRepository.findByMemberId(memberId);
    }

    // 회원 정보 수정
    @Transactional
    public void update(Member updateMember) {
        Member member = findOne(updateMember.getStringId());
        member.setName(updateMember.getName());
        member.setAdmin(updateMember.isAdmin());
        member.setBirth(updateMember.getBirth());
        member.setAddress(updateMember.getAddress());
        member.setHourlyRate(updateMember.getHourlyRate());
        member.setWeekOrMonth(updateMember.getWeekOrMonth());
    }
}
