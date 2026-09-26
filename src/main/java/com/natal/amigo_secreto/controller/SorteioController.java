package com.natal.amigo_secreto.controller;

import com.natal.amigo_secreto.dto.Dtos.Boasvindas;
import com.natal.amigo_secreto.dto.Dtos.Evento;
import com.natal.amigo_secreto.dto.Dtos.Revelacao;
import com.natal.amigo_secreto.service.ComemoracaoService;
import com.natal.amigo_secreto.service.SorteioService;
import org.springframework.web.bind.annotation.*;

/** Rotas públicas usadas pelo link individual de cada participante. */
@RestController
@RequestMapping("/api")
public class SorteioController {

    private final SorteioService service;
    private final ComemoracaoService comemoracao;

    public SorteioController(SorteioService service, ComemoracaoService comemoracao) {
        this.service = service;
        this.comemoracao = comemoracao;
    }

    @GetMapping("/evento")
    public Evento evento() {
        return new Evento(comemoracao.nome());
    }

    @GetMapping("/participante/{token}")
    public Boasvindas boasvindas(@PathVariable String token) {
        return service.boasvindas(token);
    }

    @PostMapping("/participante/{token}/revelar")
    public Revelacao revelar(@PathVariable String token) {
        return service.revelar(token);
    }
}
