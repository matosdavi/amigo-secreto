package com.natal.amigo_secreto.exception;

import org.springframework.http.HttpStatus;

/**
 * Erro de regra de negócio com uma mensagem amigável que pode ser
 * mostrada diretamente na tela, e o status HTTP correspondente.
 */
public class RegraException extends RuntimeException {

    private final HttpStatus status;

    public RegraException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public static RegraException naoEncontrado(String mensagem) {
        return new RegraException(HttpStatus.NOT_FOUND, mensagem);
    }

    public static RegraException conflito(String mensagem) {
        return new RegraException(HttpStatus.CONFLICT, mensagem);
    }

    public static RegraException invalido(String mensagem) {
        return new RegraException(HttpStatus.BAD_REQUEST, mensagem);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
