package com.example.member_management_system.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Configures Internationalization (i18n)
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * Configures the MessageSource bean to read from 'messages' bundle.
     * Spring Boot autoconfigures this, but explicit definition is clearer
     * and needed for the validator.
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Look for files like 'messages.properties', 'messages_vi.properties'
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }

    /**
     * Defines how the user's locale is stored (in the session).
     * Sets the default locale to US English.
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.US); // Default language is English

        return slr;
    }

    /**
     * Defines the URL parameter that allows changing the locale.
     * e.g., /admin/dashboard?lang=vi
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang"); // Parameter name is 'lang'

        return lci;
    }

    /**
     * Registers the LocaleChangeInterceptor.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * Connects the messageSource to the Validator.
     * This ensures that validation messages from DTOs (like @NotBlank)
     * are automatically translated.
     */
    @Bean
    public LocalValidatorFactoryBean getValidator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);

        return bean;
    }
}