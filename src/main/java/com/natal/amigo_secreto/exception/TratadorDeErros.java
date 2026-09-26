package com.natal.amigo_secreto.exception;

import com.natal.amigo_secreto.dto.Dtos.Mensagem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte exceções em respostas JSON no formato {"mensagem": "..."}. */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraException.class)
    public ResponseEntity<Mensagem> regra(RegraException e) {
        return ResponseEntity.status(e.getStatus()).body(new Mensagem(e.getMessage()));
    }
}
