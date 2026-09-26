package com.natal.amigo_secreto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Configuração da comemoração (ex.: "Amigo Chocolate", "Amigo Secreto Time X").
 * Existe no máximo uma linha, sempre com o mesmo id.
 */
@Entity
public class Comemoracao {

    public static final Long ID_UNICO = 1L;
    public static final String NOME_PADRAO = "Amigo Secreto";

    @Id
    private Long id = ID_UNICO;

    @Column(nullable = false, length = 40)
    private String nome = NOME_PADRAO;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
