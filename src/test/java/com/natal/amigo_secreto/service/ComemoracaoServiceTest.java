package com.natal.amigo_secreto.service;

import com.natal.amigo_secreto.exception.RegraException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1",
        "app.admin.senha=senha-de-teste"
})
class ComemoracaoServiceTest {

    @Autowired
    ComemoracaoService service;

    @Test
    void organizadorPersonalizaONomeEVazioVoltaAoPadrao() {
        service.renomear("  Amigo   Chocolate ");
        assertEquals("Amigo Chocolate", service.nome());

        service.renomear("Amigo Secreto Time X");
        assertEquals("Amigo Secreto Time X", service.nome());

        service.renomear("   ");
        assertEquals("Amigo Secreto", service.nome());

        assertThrows(RegraException.class, () -> service.renomear("x".repeat(41)));
    }
}
