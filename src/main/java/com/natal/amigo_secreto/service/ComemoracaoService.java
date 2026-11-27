package com.natal.amigo_secreto.service;

import com.natal.amigo_secreto.exception.RegraException;
import com.natal.amigo_secreto.model.Comemoracao;
import com.natal.amigo_secreto.model.Ocasiao;
import com.natal.amigo_secreto.repository.ComemoracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComemoracaoService {

    private final ComemoracaoRepository repository;

    public ComemoracaoService(ComemoracaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public String nome() {
        return repository.findById(Comemoracao.ID_UNICO)
                .map(Comemoracao::getNome)
                .orElse(Comemoracao.NOME_PADRAO);
    }

    /** Nome vazio volta para o padrão "Amigo Secreto". */
    @Transactional
    public void renomear(String nome) {
        String limpo = nome == null ? "" : nome.trim().replaceAll("\\s+", " ");
        if (limpo.length() > 40) {
            throw RegraException.invalido("O nome da comemoração pode ter até 40 letras.");
        }

        Comemoracao comemoracao = carregarOuCriar();
        comemoracao.setNome(limpo.isEmpty() ? Comemoracao.NOME_PADRAO : limpo);
        repository.save(comemoracao);
    }

    /** Tema "Amigo de Uma Cor Só" ligado? Desligado por padrão. */
    @Transactional(readOnly = true)
    public boolean temaCor() {
        return repository.findById(Comemoracao.ID_UNICO)
                .map(Comemoracao::isTemaCor)
                .orElse(false);
    }

    /** Liga/desliga o tema de cor. As cores salvas ficam guardadas, só deixam de aparecer. */
    @Transactional
    public void definirTemaCor(boolean ativo) {
        Comemoracao comemoracao = carregarOuCriar();
        comemoracao.setTemaCor(ativo);
        repository.save(comemoracao);
    }

    /** Roupa visual da revelação. Natal por padrão. */
    @Transactional(readOnly = true)
    public Ocasiao ocasiao() {
        return repository.findById(Comemoracao.ID_UNICO)
                .map(Comemoracao::getOcasiao)
                .orElse(Ocasiao.NATAL);
    }

    @Transactional
    public void definirOcasiao(Ocasiao ocasiao) {
        Comemoracao comemoracao = carregarOuCriar();
        comemoracao.setOcasiao(ocasiao);
        repository.save(comemoracao);
    }

    private Comemoracao carregarOuCriar() {
        return repository.findById(Comemoracao.ID_UNICO).orElseGet(Comemoracao::new);
    }
}
