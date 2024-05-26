package PosWeb.POS.service;

import PosWeb.POS.domain.Address;
import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.dto.Member.JoinMemberForm;
import PosWeb.POS.domain.dto.Member.LoginMemberDto;
import PosWeb.POS.domain.dto.Member.MemberDto;
import PosWeb.POS.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 service
    @Transactional
    public Long join(JoinMemberForm joinMemberForm) {

            Member member = new Member();
            member.setStringId(joinMemberForm.getStringId());
            member.setName(joinMemberForm.getName());
            member.setPw(passwordEncoder.encode(joinMemberForm.getPw()));
            member.setBirth(joinMemberForm.getBirth());
            member.setAddress(new Address(joinMemberForm.getZipcode(), joinMemberForm.getStreetAdr(), joinMemberForm.getDetailAdr()));

        memberRepository.save(member);
        return member.getId();
    }

    // ID 중복 검사
    public boolean checkId(String stringId) {
        Member findMember = memberRepository.findByMemberId(stringId);

        // findMember가 null이 아닐 경우 이미 존재하는 아이디이므로 exception 발생
        // stringId는 unique속성
        if (findMember != null) {
            throw new IllegalStateException("이미 존재하는 아이디 입니다.");
        }
        return true;
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
    public void update(JoinMemberForm updateMember) {
        Address address = new Address(updateMember.getZipcode(), updateMember.getStreetAdr(), updateMember.getDetailAdr());
        Member member = findOne(updateMember.getStringId());
        member.setName(updateMember.getName());
        member.setBirth(updateMember.getBirth());
        member.setAddress(address);
    }

    public Page<MemberDto> findMembersWithPaging(String searchName, int page, int size) {
        // page 객체 생성
        Pageable pageable = PageRequest.of(page, size);

        // paging된 객체를 반환
        return memberRepository.findMembersWithPaging(searchName, pageable);
    }

    @Transactional
    public void updateAdmin(List<Long> updateAdmin) {

        // list로 member를 조회 한 후 admin true로 set
        List<Member> members = memberRepository.findByMemberIdInList(updateAdmin);
        for (Member member : members) {
            member.setAdmin(true);
        }
    }
}
