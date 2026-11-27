package com.natal.amigo_secreto.service;

import com.natal.amigo_secreto.exception.RegraException;
import com.natal.amigo_secreto.model.Comemoracao;
import com.natal.amigo_secreto.model.Ocasiao;
import com.natal.amigo_secreto.repository.ComemoracaoRepository;
import com.natal.amigo_secreto.repository.ParticipanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1",
        "app.admin.senha=senha-de-teste"
})
class OcasiaoTest {

    @Autowired
    SorteioService service;

    @Autowired
    ComemoracaoService comemoracao;

    @Autowired
    ComemoracaoRepository comemoracoes;

    @Autowired
    ParticipanteRepository participantes;

    @Autowired
    org.springframework.jdbc.core.JdbcTemplate jdbc;

    @BeforeEach
    void limpar() {
        service.desfazerSorteio();
        participantes.deleteAll();
        comemoracao.definirOcasiao(Ocasiao.NATAL);
        comemoracao.definirTemaCor(false);
        comemoracao.renomear("");
    }

    @Test
    void padraoENatal() {
        comemoracoes.deleteAll();
        assertEquals(Ocasiao.NATAL, comemoracao.ocasiao());
    }

    @Test
    void linhaAntigaComOcasiaoNulaViraNatal() {
        comemoracoes.deleteAll();
        comemoracoes.save(new Comemoracao());
        assertEquals(Ocasiao.NATAL, comemoracao.ocasiao());
    }

    @Test
    void ocasiaoDesconhecidaGravadaNoBancoViraNatal() {
        comemoracoes.deleteAll();
        comemoracoes.save(new Comemoracao());
        jdbc.update("update comemoracao set ocasiao = 'carnaval'");
        assertEquals(Ocasiao.NATAL, comemoracao.ocasiao());
    }

    @Test
    void aceitaMaiusculasEEspacos() {
        service.mudarOcasiao(" PASCOA ");
        assertEquals(Ocasiao.PASCOA, comemoracao.ocasiao());
        assertEquals("pascoa", comemoracao.ocasiao().valor());
    }

    @Test
    void rejeitaInvalidaOuVazia() {
        assertThrows(RegraException.class, () -> service.mudarOcasiao("carnaval"));
        assertThrows(RegraException.class, () -> service.mudarOcasiao(""));
        assertThrows(RegraException.class, () -> service.mudarOcasiao(null));
        assertEquals(Ocasiao.NATAL, comemoracao.ocasiao());
    }

    @Test
    void travaDepoisDoSorteio() {
        List.of("Ana", "Bruno", "Carla").forEach(service::adicionar);
        service.sortear();

        assertThrows(RegraException.class, () -> service.mudarOcasiao("festa"));
        assertEquals(Ocasiao.NATAL, comemoracao.ocasiao());

        service.desfazerSorteio();
        service.mudarOcasiao("festa");
        assertEquals(Ocasiao.FESTA, comemoracao.ocasiao());
    }

    @Test
    void independenteDeNomeETemaCor() {
        comemoracao.definirOcasiao(Ocasiao.TRABALHO);
        comemoracao.renomear("Time X");
        comemoracao.definirTemaCor(true);

        assertEquals(Ocasiao.TRABALHO, comemoracao.ocasiao());
        assertEquals("Time X", comemoracao.nome());
        assertTrue(comemoracao.temaCor());

        comemoracao.definirOcasiao(Ocasiao.PASCOA);
        assertEquals("Time X", comemoracao.nome());
        assertTrue(comemoracao.temaCor());
    }
}
