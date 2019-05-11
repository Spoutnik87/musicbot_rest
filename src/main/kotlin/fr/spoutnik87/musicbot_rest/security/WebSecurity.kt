package fr.spoutnik87.musicbot_rest.security

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
class WebSecurity(
        val userDetailsService: UserDetailsServiceImpl,
        val bCryptPasswordEncoder: BCryptPasswordEncoder,
        val securityConfiguration: SecurityConfiguration
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, securityConfiguration.signUpUrl)
                .permitAll()
                .antMatchers(*AUTH_WHITELIST)
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .addFilter(JWTAuthenticationFilter(userDetailsService, authenticationManager(), securityConfiguration))
                .addFilter(JWTAuthorizationFilter(userDetailsService, authenticationManager(), securityConfiguration))
                // this disables session creation on Spring Security
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowCredentials = true
        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf(
                "X-Auth-Token",
                "Authorization",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials")
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    companion object {
        private val AUTH_WHITELIST = arrayOf(
                // -- swagger ui
                "/swagger-resources/**", "/swagger-ui.html", "/v2/api-docs", "/webjars/**")
    }
}