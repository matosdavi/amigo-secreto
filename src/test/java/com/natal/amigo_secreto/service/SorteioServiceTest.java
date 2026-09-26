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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1",
        "app.admin.senha=senha-de-teste"
})
class SorteioServiceTest {

    @Autowired
    SorteioService service;

    @Autowired
    ParticipanteRepository repository;

    @BeforeEach
    void limpar() {
        service.desfazerSorteio();
        repository.deleteAll();
    }

    @Test
    void sorteioFormaUmUnicoCicloSemNinguemTirarASiMesmo() {
        List.of("Ana", "Bruno", "Carla", "Davi", "Eva", "Fábio", "Gil").forEach(service::adicionar);

        for (int rodada = 0; rodada < 50; rodada++) {
            service.sortear();

            List<Participante> todos = repository.findAll();
            Set<String> tirados = new HashSet<>();
            for (Participante p : todos) {
                assertNotNull(p.getAmigo());
                assertNotEquals(p.getId(), p.getAmigo().getId(), "ninguém tira a si mesmo");
                assertTrue(tirados.add(p.getAmigo().getNome()), "ninguém é tirado duas vezes");
            }

            // Seguindo "quem tirou quem" a partir de qualquer pessoa, passamos por todos antes de voltar.
            Participante inicio = todos.get(0);
            Participante atual = inicio;
            int passos = 0;
            do {
                atual = atual.getAmigo();
                passos++;
            } while (!atual.getId().equals(inicio.getId()));
            assertEquals(todos.size(), passos, "ciclo único e completo");

            service.desfazerSorteio();
        }
    }

    @Test
    void exigeMinimoDeParticipantes() {
        service.adicionar("Ana");
        service.adicionar("Bruno");
        assertThrows(RegraException.class, service::sortear);
    }

    @Test
    void bloqueiaNomeRepetidoEMudancasDepoisDoSorteio() {
        List.of("Ana", "Bruno", "Carla").forEach(service::adicionar);
        assertThrows(RegraException.class, () -> service.adicionar("  ana "));

        service.sortear();
        assertThrows(RegraException.class, () -> service.adicionar("Davi"));
        assertThrows(RegraException.class, service::sortear);
    }

    @Test
    void revelarMarcaComoVisualizado() {
        List.of("Ana", "Bruno", "Carla").forEach(service::adicionar);
        String token = repository.findAll().get(0).getToken();

        assertThrows(RegraException.class, () -> service.revelar(token), "antes do sorteio");
        service.sortear();

        var revelacao = service.revelar(token);
        assertNotEquals(revelacao.nome(), revelacao.amigo());
        assertTrue(repository.findByToken(token).orElseThrow().isVisualizou());
        assertThrows(RegraException.class, () -> service.revelar("token-que-nao-existe"));
    }
}
