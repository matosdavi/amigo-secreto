package com.natal.amigo_secreto.model;

import jakarta.persistence.*;

import java.security.SecureRandom;
import java.util.Base64;

@Entity
public class Participante {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nome;

    /** Código secreto que vai no link individual de cada participante. */
    @Column(unique = true, nullable = false, length = 32)
    private String token;

    /** Quem este participante tirou no sorteio (null enquanto não houver sorteio). */
    @ManyToOne
    @JoinColumn(name = "amigo_id")
    private Participante amigo;

    /** Se o participante já abriu o link e viu quem tirou. */
    private boolean visualizou;

    protected Participante() {
        // exigido pelo JPA
    }

    public Participante(String nome) {
        this.nome = nome;
        this.token = gerarToken();
    }

    /** 16 bytes aleatórios (128 bits) em Base64 URL-safe: impossível de adivinhar. */
    private static String gerarToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public void novoToken() {
        this.token = gerarToken();
    }

    public void limparSorteio() {
        this.amigo = null;
        this.visualizou = false;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getToken() {
        return token;
    }

    public Participante getAmigo() {
        return amigo;
    }

    public void setAmigo(Participante amigo) {
        this.amigo = amigo;
    }

    public boolean isVisualizou() {
        return visualizou;
    }

    public void marcarComoVisualizado() {
        this.visualizou = true;
    }
}
