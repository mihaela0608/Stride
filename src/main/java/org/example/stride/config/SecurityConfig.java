
package org.example.stride.config;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {


        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/health",
                                "/api/csrf",
                                "/api/users/register",
                                "/api/auth/login"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType(
                                    MediaType.APPLICATION_JSON_VALUE
                            );
                            response.setCharacterEncoding("UTF-8");

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of("message", "Входът е успешен.")
                            );
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(
                                    HttpStatus.UNAUTHORIZED.value()
                            );
                            response.setContentType(
                                    MediaType.APPLICATION_JSON_VALUE
                            );
                            response.setCharacterEncoding("UTF-8");

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of(
                                            "message",
                                            "Невалиден имейл или парола."
                                    )
                            );
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response,
                                               authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType(
                                    MediaType.APPLICATION_JSON_VALUE
                            );
                            response.setCharacterEncoding("UTF-8");

                            objectMapper.writeValue(
                                    response.getWriter(),
                                    Map.of("message", "Излезе успешно.")
                            );
                        })
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
