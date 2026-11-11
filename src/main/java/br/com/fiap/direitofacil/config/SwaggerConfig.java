package br.com.fiap.direitofacil.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentação OpenAPI da API, com o esquema Bearer JWT para o botão Authorize. */
@Configuration
public class SwaggerConfig {

    private static final String ESQUEMA_JWT = "bearer-jwt";

    @Bean
    public OpenAPI diretoFacilOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("DireitoFacil - API Java")
                        .version("1.0.0")
                        .description("""
                                API do DireitoFacil: o laudo que abre portas. Cadastra beneficiários, registra laudos, \
                                avalia a elegibilidade aos direitos federais pela regra PL/SQL do Oracle e acompanha \
                                cada caso até a concessão.**"""))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("Token devolvido por POST /api/v1/auth/login.")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
