package com.natal.amigo_secreto.controller;

import com.natal.amigo_secreto.dto.Dtos.NovoParticipante;
import com.natal.amigo_secreto.dto.Dtos.PainelAdmin;
import com.natal.amigo_secreto.service.SorteioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Rotas do organizador. Todas passam pelo AdminInterceptor (senha). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SorteioService service;

    public AdminController(SorteioService service) {
        this.service = service;
    }

    @GetMapping
    public PainelAdmin painel() {
        return service.painel();
    }

    @PostMapping("/participantes")
    @ResponseStatus(HttpStatus.CREATED)
    public void adicionar(@RequestBody NovoParticipante novo) {
        service.adicionar(novo.nome());
    }

    @DeleteMapping("/participantes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }

    @PostMapping("/participantes/{id}/novo-link")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void novoLink(@PathVariable Long id) {
        service.gerarNovoLink(id);
    }

    @PostMapping("/sorteio")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sortear() {
        service.sortear();
    }

    @DeleteMapping("/sorteio")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desfazer() {
        service.desfazerSorteio();
    }
}
