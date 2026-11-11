package br.com.fiap.direitofacil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Sobe o contexto completo (Oracle, Flyway, RabbitMQ). Só roda com o banco configurado
 * (DB_URL e JWT_SECRET), para o "mvnw test" passar em qualquer máquina.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "JWT_SECRET", matches = ".{32,}")
class DireitoFacilApplicationTests {

    @Test
    void contextLoads() {
    }

}
