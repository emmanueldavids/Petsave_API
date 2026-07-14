package com.petsave.petsave.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/api/health/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/",
                    "/login**",
                    "/loginSuccess",
                    "/oauth2/**",
                    "/api/blogs/**",
                    "/api/donations/**",
                    "/api/contact",
                    "/api/adoptions/**",
                    "/api/pets/**",
                    "/api/chats/**",
                    "/uploads/**",
                    "/uploads/pet-images/**",
                    "/uploads/image/**",
                    "/api/upload/**",
                    "/api/users/count",
                    "/ws",
                    "/api/paystack/webhook",
                    "/error"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/rehomings/admin", "/api/rehomings/my-applications").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/rehomings", "/api/rehomings/*").permitAll()
                .requestMatchers("/api/rehomings/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/*").permitAll()
                .requestMatchers("/api/posts/**").authenticated()
                .requestMatchers("/api/comments/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/volunteers/my", "/api/volunteers/tasks", "/api/volunteers/admin").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/volunteers", "/api/volunteers/*").permitAll()
                .requestMatchers("/api/volunteers/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/sitters/my", "/api/sitters/admin").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/sitters", "/api/sitters/*").permitAll()
                .requestMatchers("/api/sitters/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/lost-found/my").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/lost-found", "/api/lost-found/*").permitAll()
                .requestMatchers("/api/lost-found/**").authenticated()
                .requestMatchers("/api/foster/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
