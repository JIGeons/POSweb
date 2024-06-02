package PosWeb.POS.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // http://localhost:8080/swagger-ui/index.html - 로 API 조회
    @Bean
    public OpenAPI api() {

        return new OpenAPI().info(apiInfo());
    }
    private Info apiInfo() {
        return new Info()
                .title("Swagger API")
                .description("SwaggerConfig")
                .version("2.3.0");
    }
}
