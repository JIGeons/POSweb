package PosWeb.POS.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true)
    private String stringId;

    private String name;
    private String pw;
    private LocalDate birth;
    private boolean admin;  // true 일 경우 admin

    @Column(columnDefinition = "integer default 9860")
    private int hourlyRate = 9860;         // 최저임금을 default값으로

    @Column(columnDefinition = "boolean default true")
    private Boolean weekOrMonth = true;    // false: 주급 or true: 월급 / default = true

    @Embedded
    private Address address;
}
