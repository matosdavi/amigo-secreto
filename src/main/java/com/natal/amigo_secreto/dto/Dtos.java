package com.natal.amigo_secreto.dto;

import java.util.List;

/**
 * Objetos de entrada/saída da API. Records são imutáveis e o Jackson
 * os converte para JSON automaticamente.
 */
public final class Dtos {

    private Dtos() {
    }

    public record NovoParticipante(String nome) {
    }

    /** O que o participante vê antes de revelar. */
    public record Boasvindas(String nome, boolean sorteioRealizado) {
    }

    /**
     * O que o participante vê ao revelar. Com o tema de cor ligado, "cor" é a cor
     * sorteada para o presente e "corHex" a amostra; senão os dois vêm null.
     */
    public record Revelacao(String nome, String amigo, boolean temaCor, String cor, String corHex) {
    }

    /** Linha do painel do organizador. Nunca inclui quem tirou quem. */
    public record ParticipanteAdmin(Long id, String nome, String token, boolean visualizou) {
    }

    public record PainelAdmin(boolean sorteioRealizado, List<ParticipanteAdmin> participantes,
                              List<String> coresDoTema) {
    }

    public record Evento(String nome, boolean temaCor) {
    }

    public record NovoNome(String nome) {
    }

    public record TemaCor(boolean ativo) {
    }

    public record Mensagem(String mensagem) {
    }
}
