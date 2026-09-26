package com.natal.amigo_secreto.service;

import com.natal.amigo_secreto.exception.RegraException;
import com.natal.amigo_secreto.model.Comemoracao;
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

        Comemoracao comemoracao = repository.findById(Comemoracao.ID_UNICO).orElseGet(Comemoracao::new);
        comemoracao.setNome(limpo.isEmpty() ? Comemoracao.NOME_PADRAO : limpo);
        repository.save(comemoracao);
    }
}
