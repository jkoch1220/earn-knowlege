package com.buissnes.earnkowlege.security

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.boot.actuate.info.InfoEndpoint
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
class WebSecurityConfig : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        val unsecuredEndpoints: EndpointRequest.EndpointRequestMatcher = EndpointRequest.to(
                HealthEndpoint::class.java,
                InfoEndpoint::class.java,
                PrometheusScrapeEndpoint::class.java
        )

        http
                .csrf().disable()
                .authorizeRequests { a ->
                    a
                            // delete me, just for demo purposes:
                            .antMatchers("/demo/**").permitAll()

                            .antMatchers("/**").permitAll()
                            .requestMatchers(unsecuredEndpoints).permitAll()

                            .anyRequest().authenticated()
                }
    }
}
