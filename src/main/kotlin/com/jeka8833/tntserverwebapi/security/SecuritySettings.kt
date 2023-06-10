package com.jeka8833.tntserverwebapi.security

import com.jeka8833.tntserverwebapi.security.token.TokenManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.concurrent.TimeUnit
import javax.sql.DataSource


@Configuration
@EnableWebSecurity
class SecuritySettings {

    @Value("\${db.url}")
    private lateinit var databaseUrl: String

    @Value("\${db.username}")
    private lateinit var databaseUsername: String

    @Value("\${db.password}")
    private lateinit var databasePassword: String

    @Value("\${cors.url}")
    private lateinit var corsUrl: String

    @Value("\${rememberme.key}")
    private lateinit var rememberKey: String

    @Bean
    fun getDataSource(): DataSource {
        return DataSourceBuilder.create()
            .url(databaseUrl)
            .username(databaseUsername)
            .password(databasePassword)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val conf = CorsConfiguration().applyPermitDefaultValues()
        conf.allowedOrigins = listOf(corsUrl)
        conf.allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
        conf.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", conf)
        return source
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors(Customizer.withDefaults())
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/api/cape").hasAuthority("CAPE")
                    .anyRequest().authenticated()
            }
            .logout { logout ->
                logout
                    .logoutUrl("/api/logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
            }
            .sessionManagement { session ->
                session
                    .maximumSessions(1)
                    .sessionRegistry(TokenManager.sessionRegistry)
                    .expiredSessionStrategy { strategy ->
                        strategy.response.sendError(401)
                    }
            }
            .rememberMe { remember ->
                remember
                    .key(rememberKey)
                    .tokenValiditySeconds(TimeUnit.DAYS.toSeconds(7).toInt())
                    .rememberMeParameter("remember")
            }
            .httpBasic { basic ->
                basic.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.reasonPhrase)
                }
            }
            .build()
    }
}