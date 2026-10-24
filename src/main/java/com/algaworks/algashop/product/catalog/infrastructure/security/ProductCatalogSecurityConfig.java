package com.algaworks.algashop.product.catalog.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ProductCatalogSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/v1/products/*/withdraw", "/api/v1/products/*/restock")
//                            .hasAuthority(SCOPE + "products:stock:write")
//                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**")
//                            .hasAuthority(SCOPE + "products:read")
//                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**")
//                            .hasAuthority(SCOPE + "products:write")
//                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**")
//                            .hasAuthority(SCOPE + "categories:read")
//                        .requestMatchers(HttpMethod.POST, "/api/v1/categories/**")
//                            .hasAuthority(SCOPE + "categories:write")
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults()));

                return http.build();
    }
}
