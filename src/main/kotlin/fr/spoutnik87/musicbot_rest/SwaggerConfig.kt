package fr.spoutnik87.musicbot_rest

import com.google.common.collect.Lists
import fr.spoutnik87.musicbot_rest.security.FormLoginOperations
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.ResponseEntity
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager
import springfox.documentation.spring.web.scanners.ApiDescriptionReader
import springfox.documentation.spring.web.scanners.ApiListingScanner
import springfox.documentation.spring.web.scanners.ApiModelReader
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.*

@Configuration
@EnableSwagger2
class SwaggerConfig {

    private val apiKey = ApiKey("JWT", AUTHORIZATION_HEADER, "header")

    @Bean
    fun swaggerSpringfoxDocket() = Docket(DocumentationType.SWAGGER_2)
            .pathMapping("/")
            .apiInfo(ApiInfo.DEFAULT)
            .forCodeGeneration(true)
            .genericModelSubstitutes(ResponseEntity::class.java)
            .ignoredParameterTypes(SpringDataWebProperties.Pageable::class.java)
            .ignoredParameterTypes(java.sql.Date::class.java)
            .directModelSubstitute(java.time.LocalDate::class.java, java.sql.Date::class.java)
            .directModelSubstitute(java.time.ZonedDateTime::class.java, Date::class.java)
            .directModelSubstitute(java.time.LocalDateTime::class.java, Date::class.java)
            .securityContexts(Lists.newArrayList(securityContext()))
            .securitySchemes(Lists.newArrayList(apiKey))
            .useDefaultResponseMessages(false)
            .select()
            .paths(regex(DEFAULT_INCLUDE_PATTERN))
            .build()

    fun securityContext(): SecurityContext = SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex(DEFAULT_INCLUDE_PATTERN)).build()

    fun defaultAuth(): List<SecurityReference> = listOf(SecurityReference("JWT", arrayOf(AuthorizationScope("global", "accessEverything"))))

    @Bean
    @Primary
    fun addExtraOperations(
            apiDescriptionReader: ApiDescriptionReader,
            apiModelReader: ApiModelReader,
            documentationPluginsManager: DocumentationPluginsManager
    ): ApiListingScanner = FormLoginOperations(apiDescriptionReader, apiModelReader, documentationPluginsManager)

    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val DEFAULT_INCLUDE_PATTERN = "/.*"
    }
}
