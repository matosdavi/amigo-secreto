package com.natal.amigo_secreto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminInterceptor adminInterceptor;

    public WebConfig(AdminInterceptor adminInterceptor) {
        this.adminInterceptor = adminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor).addPathPatterns("/api/admin/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Link curto e amigável de cada participante: /a/{token} abre a página de revelação
        registry.addViewController("/a/{token}").setViewName("forward:/amigo.html");
        registry.addViewController("/organizador").setViewName("forward:/organizador.html");
    }
}
