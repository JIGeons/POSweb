package PosWeb.POS.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "imp")
@Getter @Setter
public class ImpProperties {
    private String apiKey;
    private String secretKey;
}
