package PosWeb.POS.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String memberId;

    private String name;
    private String pw;
    private LocalDate Birth;
    private boolean admin;

    @ColumnDefault("9860")
    private int hourlyRate;         // 최저임금을 default값으로

    @ColumnDefault("true")
    private Boolean weekOrMonth;    // flase: 주급 or true: 월급 / default = true

    @Embedded
    private Address address;
}
