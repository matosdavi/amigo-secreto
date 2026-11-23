package com.natal.amigo_secreto.service;

import java.util.List;

/**
 * Cores do tema "Amigo de Uma Cor Só": só cores fáceis de achar em qualquer loja.
 * A quantidade de cores é o limite de participantes com o tema ligado
 * (cada pessoa recebe uma cor diferente).
 */
public final class CoresDoTema {

    /** Nome mostrado para o participante e cor da amostra na tela. */
    public record Cor(String nome, String hex) {
    }

    public static final List<Cor> TODAS = List.of(
            new Cor("Vermelho", "#d32f2f"),
            new Cor("Azul", "#1e63d6"),
            new Cor("Verde", "#2e8b57"),
            new Cor("Amarelo", "#f5c400"),
            new Cor("Rosa", "#f06292"),
            new Cor("Roxo", "#7b3fa0"),
            new Cor("Laranja", "#f57c00"),
            new Cor("Preto", "#222222"),
            new Cor("Branco", "#ffffff"),
            new Cor("Cinza", "#9e9e9e"),
            new Cor("Marrom", "#7b4a2a"),
            new Cor("Dourado", "#d4a017"),
            new Cor("Prateado", "#c0c0c0"),
            new Cor("Bege", "#e8d5b5"));

    private CoresDoTema() {
    }

    /** Cor da lista pelo nome, ou null se não existir. */
    public static Cor buscar(String nome) {
        return TODAS.stream().filter(c -> c.nome().equals(nome)).findFirst().orElse(null);
    }
}
