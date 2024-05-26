package PosWeb.POS.service;

import PosWeb.POS.domain.Member;
import PosWeb.POS.domain.dto.Member.JoinMemberForm;
import PosWeb.POS.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional  // test 실행 이후 rollback을 위한 transaction 설정
public class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    @Test
    public void 회원가입() throws Exception {
        // given
        JoinMemberForm member = new JoinMemberForm();
        member.setName("Choi");
        member.setStringId("test01");
        member.setPw("1234");

        // when
        Long savedId = memberService.join(member);

        // then
        em.flush();     // DB에 쿼리가 생성되는것을 보기 위함
        Assertions.assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test
    public void 중복_회원_예외() throws Exception {
        // given
        JoinMemberForm member1 = new JoinMemberForm();
        member1.setStringId("Choi");
        
        JoinMemberForm member2 = new JoinMemberForm();
        member2.setStringId("Choi");


        // when
        memberService.join(member1);
        try {
            memberService.join(member2);    // 동일한 StringId는 존재할 수 없으므로 예외가 발생해야 한다.
        } catch (IllegalStateException e) {
            return ;
        }

        // then
        Assertions.fail("예외가 발생해야 한다.");
    }
}
