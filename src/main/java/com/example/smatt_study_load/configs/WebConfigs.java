package com.example.smatt_study_load.configs;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.smatt_study_load.utils.JwtAuthFilter;

@Configuration
@EnableMethodSecurity
public class WebConfigs {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final String corsAllowedOrigins;

    public WebConfigs(
            JwtAuthFilter jwtAuthFilter,
            UserDetailsService userDetailsService,
            @Value("${app.cors.allowed-origins}") String corsAllowedOrigins) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/teachers", "/teachers/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/schedule/disciplines", "/schedule/teachers")
                        .authenticated()
                        .requestMatchers("/profile/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/student/**").hasAnyRole("STUDENT", "ADMIN", "GROUP_LEADER")
                        .requestMatchers("/teacher/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/schedule/{teacherId}/schedules").authenticated()
                        .requestMatchers("/schedule/{groupId}/student").hasAnyRole("STUDENT")
                        .requestMatchers("/schedule/**").hasAnyRole("GROUP_LEADER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/task/statistics")
                        .hasAnyRole("STUDENT", "GROUP_LEADER", "TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/task/detail/{taskId}")
                        .hasAnyRole("STUDENT", "GROUP_LEADER", "TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/task/current")
                        .hasAnyRole("STUDENT", "GROUP_LEADER", "TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/task/{disciplineId}")
                        .hasAnyRole("STUDENT", "GROUP_LEADER", "TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/task/add").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/task/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/task/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/umm/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/umm/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/umm/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/umm/**").hasAnyRole("TEACHER", "ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
