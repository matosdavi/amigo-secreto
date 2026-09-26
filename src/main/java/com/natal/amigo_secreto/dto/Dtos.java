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

    /** O que o participante vê ao revelar. */
    public record Revelacao(String nome, String amigo) {
    }

    /** Linha do painel do organizador. Nunca inclui quem tirou quem. */
    public record ParticipanteAdmin(Long id, String nome, String token, boolean visualizou) {
    }

    public record PainelAdmin(boolean sorteioRealizado, List<ParticipanteAdmin> participantes) {
    }

    public record Evento(String nome) {
    }

    public record Mensagem(String mensagem) {
    }
}
