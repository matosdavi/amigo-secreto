package com.natal.amigo_secreto.model;

import com.natal.amigo_secreto.exception.RegraException;

import java.util.Locale;

/** "Roupa" visual da revelação: muda embrulho, cores e confete. */
public enum Ocasiao {
    NATAL, PASCOA, TRABALHO, FESTA;

    /** Como vai no JSON e no HTML (data-ocasiao): "natal", "pascoa"... */
    public String valor() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Aceita maiúsculas e espaços ("  PASCOA "). Vazio ou desconhecido é erro 400. */
    public static Ocasiao deTexto(String texto) {
        String limpo = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        for (Ocasiao o : values()) {
            if (o.valor().equals(limpo)) {
                return o;
            }
        }
        throw RegraException.invalido("Escolha uma ocasião válida: natal, pascoa, trabalho ou festa.");
    }
}
