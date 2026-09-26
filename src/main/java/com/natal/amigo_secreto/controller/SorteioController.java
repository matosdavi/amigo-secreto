package com.natal.amigo_secreto.controller;

import com.natal.amigo_secreto.dto.Dtos.Boasvindas;
import com.natal.amigo_secreto.dto.Dtos.Evento;
import com.natal.amigo_secreto.dto.Dtos.Revelacao;
import com.natal.amigo_secreto.service.SorteioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/** Rotas públicas usadas pelo link individual de cada participante. */
@RestController
@RequestMapping("/api")
public class SorteioController {

    private final SorteioService service;
    private final String nomeEvento;

    public SorteioController(SorteioService service, @Value("${app.nome-evento:Amigo Secreto}") String nomeEvento) {
        this.service = service;
        this.nomeEvento = nomeEvento;
    }

    @GetMapping("/evento")
    public Evento evento() {
        return new Evento(nomeEvento);
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
