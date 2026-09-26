package com.natal.amigo_secreto.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

/** Usado pelo health check do Render: diz se a aplicação e o banco estão respondendo. */
@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        boolean bancoOk;
        // try-with-resources: a conexão volta para o pool mesmo se der erro
        try (Connection conexao = dataSource.getConnection()) {
            bancoOk = conexao.isValid(2);
        } catch (SQLException e) {
            bancoOk = false;
        }

        HttpStatus status = bancoOk ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(Map.of(
                "status", bancoOk ? "ok" : "degraded",
                "database", bancoOk ? "up" : "down",
                "timestamp", Instant.now().toString()));
    }
}
