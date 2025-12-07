package com.amyway.luckydraw.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.Map;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import io.swagger.v3.oas.models.media.Schema;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI luckyDrawOpenAPI() {
                return new OpenAPI()
                                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                                .components(new Components()
                                                .addSecuritySchemes("BearerAuth",
                                                                new SecurityScheme()
                                                                                .name("BearerAuth")
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")))
                                .info(new Info()
                                                .title("Lucky Draw System API")
                                                .description("抽獎系統 RESTful API 文件 - 提供抽獎活動管理、獎品配置和用戶抽獎功能")
                                                .version("1.0")
                                                .contact(new Contact()
                                                                .name("Amyway")
                                                                .email("support@amyway.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
        }

        @Bean
        public OpenApiCustomizer initialiseOpenApiCustomizer() {
                return openApi -> {
                        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                                return;
                        }
                        Map<String, Schema> schemas = openApi.getComponents().getSchemas();
                        Schema<?> schema = schemas.get("CreateActivityRequest");
                        if (schema != null && schema.getProperties() != null) {
                                DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                                OffsetDateTime now = OffsetDateTime.now();

                                Map<String, Schema> properties = schema.getProperties();

                                Schema<?> startTimeSchema = properties.get("startTime");
                                if (startTimeSchema != null) {
                                        startTimeSchema.setExample(now.plusMinutes(10).format(formatter));
                                }

                                Schema<?> endTimeSchema = properties.get("endTime");
                                if (endTimeSchema != null) {
                                        endTimeSchema.setExample(now.plusHours(2).format(formatter));
                                }
                        }
                };
        }
}
