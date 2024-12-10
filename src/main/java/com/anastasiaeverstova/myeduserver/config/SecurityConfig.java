package com.anastasiaeverstova.myeduserver.config;


import com.anastasiaeverstova.myeduserver.models.UserRole;
import com.anastasiaeverstova.myeduserver.service.CustomOAuthUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Autowired
    private CustomOAuthUserService googleOauthService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthSuccessHandler successHandler;

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/index.html", "/", "/auth/**", "/favicon.ico", "/login/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/courses/**", "/objectives/**", "/lessons/**", "/reviews/**").permitAll()
                        .requestMatchers("/profile/**", "/user/**").hasAuthority(UserRole.ROLE_STUDENT.name())
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/admin/users/**").hasAuthority(UserRole.ROLE_ADMIN.name())
                        .requestMatchers("/admin/**").hasAuthority(UserRole.ROLE_ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/courses/full").hasAnyAuthority(UserRole.ROLE_ADMIN.name(), UserRole.ROLE_TEACHER.name())
                        .requestMatchers(HttpMethod.POST, "/courses/uploadVideo").hasAnyAuthority(UserRole.ROLE_ADMIN.name(), UserRole.ROLE_TEACHER.name())
                        .requestMatchers("/videos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reviews/**").permitAll()
                        .requestMatchers("/teacher/**").hasAuthority(UserRole.ROLE_TEACHER.name())
                        .requestMatchers(HttpMethod.GET, "/enroll/status/c/**").hasAnyAuthority(UserRole.ROLE_STUDENT.name(), UserRole.ROLE_ADMIN.name())
                        .requestMatchers("/checkout/webhook").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .securityContext(context -> context
                        .requireExplicitSave(false))
                .sessionManagement((sessions) -> sessions
                        .sessionConcurrency((concurrency) ->
                                concurrency.maximumSessions(2)));

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
