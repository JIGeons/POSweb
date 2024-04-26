package PosWeb.POS.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    private String zipcode;
    private String streetAdr;
    private String detailAdr;
}
