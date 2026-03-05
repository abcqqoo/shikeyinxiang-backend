package com.example.diet.observability.config;

import com.example.diet.observability.http.HttpTraceFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationRunner;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Auto-configuration for observability components.
 */
@AutoConfiguration
@EnableConfigurationProperties(ObservabilityLoggingProperties.class)
public class ObservabilityAutoConfiguration {

    @ConditionalOnClass(OncePerRequestFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean
    @Bean
    public HttpTraceFilter httpTraceFilter(ObservabilityLoggingProperties properties) {
        return new HttpTraceFilter(properties);
    }

    @ConditionalOnClass(OncePerRequestFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean(name = "httpTraceFilterRegistration")
    @Bean
    public FilterRegistrationBean<HttpTraceFilter> httpTraceFilterRegistration(HttpTraceFilter filter) {
        FilterRegistrationBean<HttpTraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setName("httpTraceFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }

    @ConditionalOnMissingBean
    @Bean
    public ApplicationRunner logModeCustomizer(ObservabilityLoggingProperties properties) {
        return new LogModeCustomizer(properties);
    }
}
