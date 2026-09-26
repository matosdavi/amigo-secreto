package com.natal.amigo_secreto.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Protege as rotas /api/admin/** com a senha do organizador,
 * enviada pelo navegador no cabeçalho "X-Senha-Admin".
 * Se nenhuma senha for configurada, uma é gerada e mostrada no console.
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminInterceptor.class);
    public static final String CABECALHO = "X-Senha-Admin";

    private final byte[] senha;

    public AdminInterceptor(@Value("${app.admin.senha:}") String senhaConfigurada) {
        String efetiva = senhaConfigurada.isBlank() ? gerarSenha() : senhaConfigurada;
        if (senhaConfigurada.isBlank()) {
            log.warn("Nenhuma ADMIN_SENHA configurada. Senha temporária do organizador: {}", efetiva);
        }
        this.senha = efetiva.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String recebida = request.getHeader(CABECALHO);
        // Comparação em tempo constante: não "vaza" quantos caracteres estavam certos.
        if (recebida != null && MessageDigest.isEqual(senha, recebida.getBytes(StandardCharsets.UTF_8))) {
            return true;
        }
        Thread.sleep(500); // atrasa tentativas de adivinhar a senha
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"mensagem\":\"Senha do organizador incorreta.\"}");
        return false;
    }

    private static String gerarSenha() {
        SecureRandom random = new SecureRandom();
        String letras = "abcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(letras.charAt(random.nextInt(letras.length())));
        }
        return sb.toString();
    }
}
