package fr.spoutnik87.musicbot_rest.security

import com.fasterxml.classmate.TypeResolver
import com.google.common.collect.Multimap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import springfox.documentation.builders.ApiListingBuilder
import springfox.documentation.builders.OperationBuilder
import springfox.documentation.builders.ParameterBuilder
import springfox.documentation.builders.ResponseMessageBuilder
import springfox.documentation.schema.ModelRef
import springfox.documentation.service.ApiDescription
import springfox.documentation.service.ApiListing
import springfox.documentation.service.Operation
import springfox.documentation.service.ResponseMessage
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager
import springfox.documentation.spring.web.readers.operation.CachingOperationNameGenerator
import springfox.documentation.spring.web.scanners.ApiDescriptionReader
import springfox.documentation.spring.web.scanners.ApiListingScanner
import springfox.documentation.spring.web.scanners.ApiListingScanningContext
import springfox.documentation.spring.web.scanners.ApiModelReader
import java.util.*

class FormLoginOperations
@Autowired
constructor(
        apiDescriptionReader: ApiDescriptionReader,
        apiModelReader: ApiModelReader,
        pluginsManager: DocumentationPluginsManager
) : ApiListingScanner(apiDescriptionReader, apiModelReader, pluginsManager) {

    @Autowired
    private lateinit var typeResolver: TypeResolver

    override fun scan(context: ApiListingScanningContext): Multimap<String, ApiListing> {
        val def = super.scan(context)

        val apis = LinkedList<ApiDescription>()

        val operations = ArrayList<Operation>()
        operations.add(
                OperationBuilder(CachingOperationNameGenerator())
                        .method(HttpMethod.POST)
                        .uniqueId("login")
                        .parameters(
                                listOf(
                                        ParameterBuilder()
                                                .name("body")
                                                .required(true)
                                                .description("The body of request")
                                                .parameterType("body")
                                                .type(typeResolver.resolve(String::class.java))
                                                .modelRef(ModelRef("string"))
                                                .defaultValue("{\"email\":\"\",\"password\":\"\"}")
                                                .build()))
                        .responseMessages(
                                HashSet<ResponseMessage>(
                                        listOf(
                                                ResponseMessageBuilder()
                                                        .code(200)
                                                        .message("OK")
                                                        .responseModel(ModelRef("string"))
                                                        .build())))
                        .summary("Log in") //
                        .notes("Here you can log in. Default body : {\"email\":\"\",\"password\":\"\"}")
                        .build())
        apis.add(
                ApiDescription("Default", "/login", "Authentication documentation", operations, false))

        def.put(
                "authentication",
                ApiListingBuilder(context.documentationContext.apiDescriptionOrdering)
                        .apis(apis)
                        .description("Custom authentication")
                        .build())

        return def
    }
}
