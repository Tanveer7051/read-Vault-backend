package in.ReadVault.Security;


import in.ReadVault.Config.AppConfig;
import in.ReadVault.Filter.JwtFilter;
import in.ReadVault.GlobalExceptionHandling.CustomAccessDeniedHandler;
import in.ReadVault.GlobalExceptionHandling.JwtAuthEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final AppConfig appConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> auth

//                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                // PUBLIC
                                .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/user/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/user/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/user/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.PUT, "/api/user/**").hasAnyRole("ADMIN", "USER")

                                // BOOKS
                        .requestMatchers(HttpMethod.POST, "/api/book/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/book/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/book/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/book/**").hasAnyRole("ADMIN", "USER")

                                // BORROW
                        .requestMatchers(HttpMethod.POST, "/api/borrow/take/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/borrow/digital/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/borrow/return/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow/**").hasAnyRole("ADMIN", "USER")


                        .requestMatchers(HttpMethod.POST, "/api/reservations/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/reservations").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reservations/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.PUT, "/api/reservations/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/reservations/*/cancel").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/reservations/**").hasRole("ADMIN")

                                .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

        .authenticationProvider(
                appConfig.authenticationProvider()
        );
        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "https://read-vault-frontend.vercel.app"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}