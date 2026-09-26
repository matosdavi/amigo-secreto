package com.natal.amigo_secreto.service;

import com.natal.amigo_secreto.dto.Dtos.Boasvindas;
import com.natal.amigo_secreto.dto.Dtos.PainelAdmin;
import com.natal.amigo_secreto.dto.Dtos.ParticipanteAdmin;
import com.natal.amigo_secreto.dto.Dtos.Revelacao;
import com.natal.amigo_secreto.exception.RegraException;
import com.natal.amigo_secreto.model.Participante;
import com.natal.amigo_secreto.repository.ParticipanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SorteioService {

    static final int MINIMO_PARTICIPANTES = 3;

    private final ParticipanteRepository repository;
    private final SecureRandom random = new SecureRandom();

    public SorteioService(ParticipanteRepository repository) {
        this.repository = repository;
    }

    // ---------- Organizador ----------

    @Transactional(readOnly = true)
    public PainelAdmin painel() {
        List<ParticipanteAdmin> lista = repository.findAllByOrderByNomeAsc().stream()
                .map(p -> new ParticipanteAdmin(p.getId(), p.getNome(), p.getToken(), p.isVisualizou()))
                .toList();
        return new PainelAdmin(repository.existsByAmigoIsNotNull(), lista);
    }

    @Transactional
    public void adicionar(String nome) {
        String nomeLimpo = nome == null ? "" : nome.trim().replaceAll("\\s+", " ");
        if (nomeLimpo.isEmpty() || nomeLimpo.length() > 60) {
            throw RegraException.invalido("Digite um nome válido (até 60 letras).");
        }
        exigirSorteioNaoRealizado();
        if (repository.existsByNomeIgnoreCase(nomeLimpo)) {
            throw RegraException.conflito("Já existe alguém chamado " + nomeLimpo + ".");
        }
        repository.save(new Participante(nomeLimpo));
    }

    @Transactional
    public void remover(Long id) {
        exigirSorteioNaoRealizado();
        repository.delete(buscarPorId(id));
    }

    /** Gera um link novo para alguém (ex.: o link antigo vazou ou foi enviado errado). */
    @Transactional
    public void gerarNovoLink(Long id) {
        buscarPorId(id).novoToken();
    }

    /**
     * Sorteia TODOS de uma vez só.
     * <p>
     * Embaralha a lista e liga cada pessoa à próxima, e a última à primeira:
     * A → B → C → ... → A. Isso garante, por construção, que:
     * <ul>
     *   <li>ninguém tira a si mesmo;</li>
     *   <li>todo mundo tira exatamente uma pessoa e é tirado exatamente uma vez;</li>
     *   <li>forma um único ciclo completo (não aparecem "grupinhos" fechados).</li>
     * </ul>
     */
    @Transactional
    public void sortear() {
        exigirSorteioNaoRealizado();

        List<Participante> participantes = new ArrayList<>(repository.findAll());
        if (participantes.size() < MINIMO_PARTICIPANTES) {
            throw RegraException.invalido(
                    "São necessárias pelo menos " + MINIMO_PARTICIPANTES + " pessoas para sortear.");
        }

        Collections.shuffle(participantes, random);
        for (int i = 0; i < participantes.size(); i++) {
            Participante atual = participantes.get(i);
            Participante proximo = participantes.get((i + 1) % participantes.size());
            atual.setAmigo(proximo);
        }
        // Sem save(): dentro de @Transactional o JPA detecta as mudanças
        // nas entidades carregadas e grava tudo no commit (dirty checking).
    }

    @Transactional
    public void desfazerSorteio() {
        repository.findAll().forEach(Participante::limparSorteio);
    }

    // ---------- Participante ----------

    @Transactional(readOnly = true)
    public Boasvindas boasvindas(String token) {
        Participante p = buscarPorToken(token);
        return new Boasvindas(p.getNome(), p.getAmigo() != null);
    }

    @Transactional
    public Revelacao revelar(String token) {
        Participante p = buscarPorToken(token);
        if (p.getAmigo() == null) {
            throw RegraException.conflito("O sorteio ainda não foi feito. Volte mais tarde!");
        }
        p.marcarComoVisualizado();
        return new Revelacao(p.getNome(), p.getAmigo().getNome());
    }

    // ---------- Auxiliares ----------

    private void exigirSorteioNaoRealizado() {
        if (repository.existsByAmigoIsNotNull()) {
            throw RegraException.conflito(
                    "O sorteio já foi feito. Para mudar a lista, desfaça o sorteio primeiro.");
        }
    }

    private Participante buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> RegraException.naoEncontrado("Participante não encontrado."));
    }

    private Participante buscarPorToken(String token) {
        return repository.findByToken(token)
                .orElseThrow(() -> RegraException.naoEncontrado(
                        "Link inválido. Peça ao organizador para enviar seu link novamente."));
    }
}
