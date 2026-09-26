package com.natal.amigo_secreto.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguracaoDoAmbienteTest {

    private final ConfiguracaoDoAmbiente config = new ConfiguracaoDoAmbiente();

    @Test
    void converteConnectionStringDoNeonParaJdbc() {
        var c = ConfiguracaoDoAmbiente.paraJdbc(
                "postgresql://dono:s%40nha@ep-abc-123.us-east-2.aws.neon.tech/neondb?sslmode=require");

        assertEquals("jdbc:postgresql://ep-abc-123.us-east-2.aws.neon.tech:5432/neondb?sslmode=require", c.url());
        assertEquals("dono", c.usuario());
        assertEquals("s@nha", c.senha(), "caracteres especiais vêm codificados na URL");
    }

    @Test
    void aceitaPortaEUrlJdbcPronta() {
        assertEquals("jdbc:postgresql://localhost:5433/amigo_secreto",
                ConfiguracaoDoAmbiente.paraJdbc("postgres://postgres:postgres@localhost:5433/amigo_secreto").url());
        assertEquals("jdbc:h2:mem:x", ConfiguracaoDoAmbiente.paraJdbc("jdbc:h2:mem:x").url());
        assertThrows(IllegalStateException.class, () -> ConfiguracaoDoAmbiente.paraJdbc("mysql://x/y"));
    }

    @Test
    void semDatabaseUrlUsaH2Local() {
        var env = new MockEnvironment();
        config.postProcessEnvironment(env, new SpringApplication());
        assertEquals(ConfiguracaoDoAmbiente.H2_LOCAL, env.getProperty("spring.datasource.url"));
    }

    @Test
    void producaoExigeBancoESenha() {
        var env = new MockEnvironment().withProperty("APP_ENV", "production").withProperty("ADMIN_SENHA", "123");
        var erro = assertThrows(IllegalStateException.class,
                () -> config.postProcessEnvironment(env, new SpringApplication()));
        assertTrue(erro.getMessage().contains("DATABASE_URL"));
        assertTrue(erro.getMessage().contains("ADMIN_SENHA"));
    }
}
