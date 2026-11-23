package com.natal.amigo_secreto.service;

import com.natal.amigo_secreto.exception.RegraException;
import com.natal.amigo_secreto.model.Participante;
import com.natal.amigo_secreto.repository.ParticipanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1",
        "app.admin.senha=senha-de-teste"
})
class TemaCorTest {

    @Autowired
    SorteioService service;

    @Autowired
    ComemoracaoService comemoracao;

    @Autowired
    ParticipanteRepository repository;

    @BeforeEach
    void limpar() {
        service.desfazerSorteio();
        repository.deleteAll();
        comemoracao.definirTemaCor(false);
        comemoracao.renomear("");
    }

    private void adicionarPessoas(int quantidade) {
        IntStream.rangeClosed(1, quantidade).forEach(i -> service.adicionar("Pessoa " + i));
    }

    @Test
    void temaVemDesativadoERenomearNaoMexeNele() {
        assertFalse(comemoracao.temaCor());

        comemoracao.definirTemaCor(true);
        comemoracao.renomear("Amigo de Uma Cor Só");
        assertTrue(comemoracao.temaCor(), "renomear não desliga o tema");
        assertEquals("Amigo de Uma Cor Só", comemoracao.nome());
    }

    @Test
    void listaTemCoresSuficientesParaTrezePessoasSemRepetir() {
        assertTrue(CoresDoTema.TODAS.size() >= 13);
        assertEquals(CoresDoTema.TODAS.size(),
                CoresDoTema.TODAS.stream().map(CoresDoTema.Cor::nome).distinct().count(), "nomes únicos");
    }

    @Test
    void comTemaLigadoCadaUmRecebeUmaCorDiferente() {
        adicionarPessoas(13);
        comemoracao.definirTemaCor(true);

        for (int rodada = 0; rodada < 30; rodada++) {
            service.sortear();
            Set<String> cores = new HashSet<>();
            for (Participante p : repository.findAll()) {
                assertNotNull(p.getCorDoPresente(), "todo mundo recebe cor");
                assertNotNull(CoresDoTema.buscar(p.getCorDoPresente()), "cor da lista oficial");
                assertTrue(cores.add(p.getCorDoPresente()), "cor não se repete");
            }
            service.desfazerSorteio();
        }
    }

    @Test
    void comOMaximoDePessoasTodasAsCoresSaoUsadas() {
        adicionarPessoas(CoresDoTema.TODAS.size());
        comemoracao.definirTemaCor(true);
        service.sortear();

        Set<String> usadas = new HashSet<>();
        repository.findAll().forEach(p -> usadas.add(p.getCorDoPresente()));
        assertEquals(CoresDoTema.TODAS.size(), usadas.size());
    }

    @Test
    void maisPessoasQueCoresBloqueiaSoComTemaLigado() {
        adicionarPessoas(CoresDoTema.TODAS.size() + 1);
        comemoracao.definirTemaCor(true);
        RegraException erro = assertThrows(RegraException.class, service::sortear);
        assertTrue(erro.getMessage().contains(String.valueOf(CoresDoTema.TODAS.size())));
        assertTrue(repository.findAll().stream().allMatch(p -> p.getAmigo() == null), "nada foi sorteado");

        comemoracao.definirTemaCor(false);
        assertDoesNotThrow(service::sortear);
    }

    @Test
    void revelacaoTrazACorDoPresenteSoComTemaLigado() {
        adicionarPessoas(4);
        comemoracao.definirTemaCor(true);
        service.sortear();

        Participante p = repository.findAll().get(0);
        var revelacao = service.revelar(p.getToken());
        assertTrue(revelacao.temaCor());
        assertEquals(p.getCorDoPresente(), revelacao.cor());
        assertEquals(CoresDoTema.buscar(p.getCorDoPresente()).hex(), revelacao.corHex());
    }

    @Test
    void comTemaDesligadoNinguemRecebeCor() {
        adicionarPessoas(4);
        service.sortear();

        assertTrue(repository.findAll().stream().allMatch(p -> p.getCorDoPresente() == null));
        var revelacao = service.revelar(repository.findAll().get(0).getToken());
        assertFalse(revelacao.temaCor());
        assertNull(revelacao.cor());
        assertNull(revelacao.corHex());
    }

    @Test
    void desfazerSorteioApagaAsCores() {
        adicionarPessoas(4);
        comemoracao.definirTemaCor(true);
        service.sortear();
        service.desfazerSorteio();

        assertTrue(repository.findAll().stream().allMatch(p -> p.getCorDoPresente() == null));
    }

    @Test
    void temaSoMudaAntesDoSorteio() {
        adicionarPessoas(4);
        comemoracao.definirTemaCor(true);
        service.sortear();

        assertThrows(RegraException.class, () -> service.mudarTemaCor(false));
        assertThrows(RegraException.class, () -> service.mudarTemaCor(true));
        assertTrue(comemoracao.temaCor());

        service.desfazerSorteio();
        service.mudarTemaCor(false);
        assertFalse(comemoracao.temaCor());
        assertEquals(List.of(), repository.findAll().stream().filter(p -> p.getCorDoPresente() != null).toList());
    }
}
