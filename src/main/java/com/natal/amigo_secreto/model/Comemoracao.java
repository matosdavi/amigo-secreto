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

    /**
     * Tema "Amigo de Uma Cor Só": cada participante tem uma cor favorita,
     * mostrada na revelação. Boolean (e não boolean) para a coluna nova
     * aceitar null nas linhas que já existem no banco; null = desligado.
     */
    private Boolean temaCor;

    /** Roupa visual (ver {@link Ocasiao}). Coluna anulável pelo mesmo motivo; null = Natal. */
    @Column(length = 20)
    private String ocasiao;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isTemaCor() {
        return Boolean.TRUE.equals(temaCor);
    }

    public void setTemaCor(boolean temaCor) {
        this.temaCor = temaCor;
    }

    public Ocasiao getOcasiao() {
        // Leitura tolerante: valor vazio ou desconhecido no banco não pode derrubar
        // a página; vira Natal. (A entrada pela API continua estrita, em deTexto.)
        for (Ocasiao o : Ocasiao.values()) {
            if (o.valor().equals(ocasiao)) {
                return o;
            }
        }
        return Ocasiao.NATAL;
    }

    public void setOcasiao(Ocasiao ocasiao) {
        this.ocasiao = ocasiao.valor();
    }
}
