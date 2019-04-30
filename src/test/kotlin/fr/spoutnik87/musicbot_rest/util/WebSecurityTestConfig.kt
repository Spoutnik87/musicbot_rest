package fr.spoutnik87.musicbot_rest.util

import fr.spoutnik87.musicbot_rest.constant.RoleEnum
import fr.spoutnik87.musicbot_rest.model.Role
import fr.spoutnik87.musicbot_rest.model.User
import fr.spoutnik87.musicbot_rest.security.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.*

@Configuration
@EnableWebSecurity
class WebSecurityTestConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @Autowired
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @Autowired
    private lateinit var securityConfiguration: SecurityConfiguration

    @Bean
    fun securityConfiguration() = SecurityConfiguration()

    @Bean
    public override fun userDetailsService(): UserDetailsServiceImpl {
        val roleUser = Role("token", "USER", 2)
        val user = User(
                "token",
                "user@test.com",
                "Nickname",
                "Firstname",
                "Lastname",
                bCryptPasswordEncoder.encode("password"),
                roleUser)
        val basicUser = UserDetails(user, Arrays.asList(SimpleGrantedAuthority(RoleEnum.USER.value)))

        val roleAdmin = Role("token2", "ADMIN", 1)
        val userAdmin = User(
                "token2",
                "admin@test.com",
                "Nickname",
                "Firstname",
                "Lastname",
                bCryptPasswordEncoder.encode("password"),
                roleAdmin)
        val adminUser = UserDetails(
                userAdmin, Arrays.asList(SimpleGrantedAuthority(RoleEnum.ADMIN.value)))
        return InMemoryUserDetailsManager(Arrays.asList(basicUser, adminUser))
    }

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