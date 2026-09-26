package com.natal.amigo_secreto.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lê as variáveis de ambiente (ou o arquivo .env) ANTES de o Spring criar
 * qualquer bean, e:
 * <ol>
 *   <li>converte o DATABASE_URL no formato do Neon/Render
 *       ({@code postgres://usuario:senha@host/banco?sslmode=require})
 *       para as propriedades JDBC que o Spring entende;</li>
 *   <li>em produção (APP_ENV=production), recusa subir sem as variáveis obrigatórias.</li>
 * </ol>
 * Registrada em META-INF/spring.factories.
 */
public class ConfiguracaoDoAmbiente implements EnvironmentPostProcessor {

    static final String H2_LOCAL = "jdbc:h2:file:./data/amigosecreto";

    record Conexao(String url, String usuario, String senha) {
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        boolean producao = "production".equalsIgnoreCase(env.getProperty("APP_ENV", "development"));
        String databaseUrl = env.getProperty("DATABASE_URL", "").trim();
        String senhaAdmin = env.getProperty("ADMIN_SENHA", "");

        List<String> erros = new ArrayList<>();
        if (producao && databaseUrl.isEmpty()) {
            erros.add("DATABASE_URL é obrigatório em produção (o disco do Render é apagado a cada reinício)");
        }
        if (producao && senhaAdmin.length() < 8) {
            erros.add("ADMIN_SENHA é obrigatório em produção e deve ter pelo menos 8 caracteres");
        }
        if (!erros.isEmpty()) {
            throw new IllegalStateException("Configuração inválida:\n - " + String.join("\n - ", erros));
        }

        Conexao conexao = databaseUrl.isEmpty() ? new Conexao(H2_LOCAL, "sa", "") : paraJdbc(databaseUrl);

        // addLast = menor prioridade: um spring.datasource.* definido explicitamente
        // (ex.: nos testes) continua valendo.
        env.getPropertySources().addLast(new MapPropertySource("amigo-secreto-datasource", Map.of(
                "spring.datasource.url", conexao.url(),
                "spring.datasource.username", conexao.usuario(),
                "spring.datasource.password", conexao.senha())));
    }

    /** postgres://usuario:senha@host:porta/banco?params  →  jdbc:postgresql://host:porta/banco?params */
    static Conexao paraJdbc(String databaseUrl) {
        if (databaseUrl.startsWith("jdbc:")) {
            return new Conexao(databaseUrl, "", ""); // já está no formato JDBC
        }

        URI uri = URI.create(databaseUrl);
        if (!"postgres".equals(uri.getScheme()) && !"postgresql".equals(uri.getScheme())) {
            throw new IllegalStateException("DATABASE_URL deve começar com postgres:// ou postgresql://");
        }

        String usuario = "";
        String senha = "";
        if (uri.getRawUserInfo() != null) {
            String[] partes = uri.getRawUserInfo().split(":", 2);
            usuario = decodificar(partes[0]);
            senha = partes.length > 1 ? decodificar(partes[1]) : "";
        }

        int porta = uri.getPort() == -1 ? 5432 : uri.getPort();
        String url = "jdbc:postgresql://" + uri.getHost() + ":" + porta + uri.getRawPath()
                + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        return new Conexao(url, usuario, senha);
    }

    private static String decodificar(String texto) {
        return URLDecoder.decode(texto, StandardCharsets.UTF_8);
    }
}
