package fr.spoutnik87.musicbot_rest.security;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import springfox.documentation.builders.ApiListingBuilder;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.Operation;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;
import springfox.documentation.spring.web.readers.operation.CachingOperationNameGenerator;
import springfox.documentation.spring.web.scanners.ApiDescriptionReader;
import springfox.documentation.spring.web.scanners.ApiListingScanner;
import springfox.documentation.spring.web.scanners.ApiListingScanningContext;
import springfox.documentation.spring.web.scanners.ApiModelReader;

import java.util.*;

public class FormLoginOperations extends ApiListingScanner {
  @Autowired private TypeResolver typeResolver;

  @Autowired
  public FormLoginOperations(
      ApiDescriptionReader apiDescriptionReader,
      ApiModelReader apiModelReader,
      DocumentationPluginsManager pluginsManager) {
    super(apiDescriptionReader, apiModelReader, pluginsManager);
  }

  @Override
  public Multimap<String, ApiListing> scan(ApiListingScanningContext context) {
    final Multimap<String, ApiListing> def = super.scan(context);

    final List<ApiDescription> apis = new LinkedList<>();

    final List<Operation> operations = new ArrayList<>();
    operations.add(
        new OperationBuilder(new CachingOperationNameGenerator())
            .method(HttpMethod.POST)
            .uniqueId("login")
            .parameters(
                Arrays.asList(
                    new ParameterBuilder()
                        .name("body")
                        .required(true)
                        .description("The body of request")
                        .parameterType("body")
                        .type(typeResolver.resolve(String.class))
                        .modelRef(new ModelRef("string"))
                        .defaultValue("{\"email\":\"\",\"password\":\"\"}")
                        .build()))
            .responseMessages(
                new HashSet<>(
                    Arrays.asList(
                        new ResponseMessageBuilder()
                            .code(200)
                            .message("OK")
                            .responseModel(new ModelRef("string"))
                            .build())))
            .summary("Log in") //
            .notes("Here you can log in. Default body : {\"email\":\"\",\"password\":\"\"}")
            .build());
    apis.add(
        new ApiDescription("Default", "/login", "Authentication documentation", operations, false));

    def.put(
        "authentication",
        new ApiListingBuilder(context.getDocumentationContext().getApiDescriptionOrdering())
            .apis(apis)
            .description("Custom authentication")
            .build());

    return def;
  }
}
