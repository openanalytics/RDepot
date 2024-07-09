/*
 * RDepot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2ReadingController;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.formatters.StringToSubmissionStateConverter;
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.python.api.v2.controllers.PythonPackageController;
import eu.openanalytics.rdepot.python.api.v2.controllers.PythonRepositoryController;
import eu.openanalytics.rdepot.python.api.v2.controllers.PythonSubmissionController;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import eu.openanalytics.rdepot.r.api.v2.controllers.RPackageController;
import eu.openanalytics.rdepot.r.api.v2.controllers.RRepositoryController;
import eu.openanalytics.rdepot.r.api.v2.controllers.RSubmissionController;
import eu.openanalytics.rdepot.r.technology.RLanguage;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Main configuration of RDepot.
 */
@Configuration
@EnableAsync
@ComponentScan("eu.openanalytics.rdepot")
public class WebApplicationConfig implements WebMvcConfigurer, ApplicationContextAware {

    private static final String[] EXTENSIONS = {"r", "common", "python"};

    private static final Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> packageControllerClasses =
            Map.of(
                    RLanguage.instance, RPackageController.class,
                    PythonLanguage.instance, PythonPackageController.class);

    private static final Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> repositoryControllerClasses =
            Map.of(
                    RLanguage.instance, RRepositoryController.class,
                    PythonLanguage.instance, PythonRepositoryController.class);

    private static final Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> submissionControllerClasses =
            Map.of(
                    RLanguage.instance, RSubmissionController.class,
                    PythonLanguage.instance, PythonSubmissionController.class);

    @Value("${db.driver}")
    private String databaseDriver;

    @Value("${db.password}")
    private String databasePassword;

    @Value("${db.url}")
    private String databaseUrl;

    @Value("${db.username}")
    private String databaseUsername;

    @Value("${hibernate.dialect}")
    private String hibernateDialect;

    @Value("${hibernate.show_sql}")
    private String hibernateShowSql;

    @Value("${package.upload.dir}")
    private String packageUploadDir;

    @Value("${repository.generation.dir}")
    private String repositoryGenerationDir;

    @Resource
    private Environment env;

    @Getter
    private ApplicationContext context;

    final Logger logger = LoggerFactory.getLogger(WebApplicationConfig.class);

    @Bean
    RestTemplate rest() {
        return new RestTemplate();
    }

    @Bean
    DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(databaseDriver);
        dataSource.setUrl(databaseUrl);
        dataSource.setUsername(databaseUsername);
        dataSource.setPassword(databasePassword);

        return dataSource;
    }

    @Bean
    Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> packageControllerClassesByTechnology() {
        return packageControllerClasses;
    }

    @Bean
    Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> repositoryControllerClassesByTechnology() {
        return repositoryControllerClasses;
    }

    @Bean
    Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> submissionControllerClassesByTechnology() {
        return submissionControllerClasses;
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        List<String> packagesToScanProp = new ArrayList<>();
        for (int i = 0; ; i++) {
            String packageToScan = env.getProperty(String.format("entitymanager.packages.to.scan[%d]", i));
            if (packageToScan == null) break;
            else packagesToScanProp.add(packageToScan);
        }

        String[] packagesToScan = new String[packagesToScanProp.size()];
        for (int i = 0; i < packagesToScanProp.size(); i++) {
            packagesToScan[i] = packagesToScanProp.get(i);
        }
        entityManagerFactoryBean.setPackagesToScan(packagesToScan);
        entityManagerFactoryBean.setJpaProperties(hibProperties());

        return entityManagerFactoryBean;
    }

    private Properties hibProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", hibernateDialect);
        properties.put("hibernate.show_sql", hibernateShowSql);
        return properties;
    }

    @Bean
    JpaTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(submissionStateConverter());
    }

    @Bean
    Converter<String, SubmissionState> submissionStateConverter() {
        return new StringToSubmissionStateConverter();
    }

    @Bean
    MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();

        final String basename = "i18n/messages";
        List<String> basenames = new ArrayList<>();
        for (String ext : EXTENSIONS) {
            basenames.add(basename + "-" + ext);
        }

        source.setBasenames(basenames.toArray(new String[0]));
        source.setUseCodeAsDefaultMessage(true);

        return source;
    }

    @Bean
    CookieLocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    @Bean
    LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("/WEB-INF/static/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("/webjars/");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jsonConverter());
        converters.add(byteConverter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean
    Gson jsonParser() {
        return new Gson();
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.ignoreAcceptHeader(false).defaultContentType(MediaType.APPLICATION_JSON);
    }

    @Bean
    MappingJackson2HttpMessageConverter jsonConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        mediaTypes.add(MediaType.valueOf("application/json-patch+json"));
        converter.setSupportedMediaTypes(mediaTypes);
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    @Bean
    ByteArrayHttpMessageConverter byteConverter() {
        ByteArrayHttpMessageConverter converter = new ByteArrayHttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.valueOf("application/gzip"));
        mediaTypes.add(MediaType.valueOf("application/pdf"));
        converter.setSupportedMediaTypes(mediaTypes);
        return converter;
    }

    @Bean
    LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    @Override
    public MessageCodesResolver getMessageCodesResolver() {
        return new DefaultMessageCodesResolver() {

            private static final long serialVersionUID = 4328458877485113449L;

            @Override
            public String[] resolveMessageCodes(String errorCode, String objectName) {
                return new String[] {errorCode};
            }
        };
    }

    @Bean(name = "packageUploadDirectory")
    File packageUploadDirectory() {
        File location = new File(packageUploadDir);

        if (!location.exists()) {
            try {
                Files.createDirectory(location.toPath());
            } catch (IOException e) {
                throw new BeanCreationException("Cannot create package upload directory.");
            }
        } else if (!(location.canRead() && location.canWrite())) {
            throw new BeanCreationException("Cannot access package upload directory.");
        }

        return location;
    }

    @Bean(name = "repositoryGenerationDirectory")
    File repositoryGenerationDirectory() {
        File location = new File(repositoryGenerationDir);

        if (!location.exists()) {
            try {
                Files.createDirectory(location.toPath());
            } catch (IOException e) {
                throw new BeanCreationException("Cannot create repository generation directory.");
            }
        } else if (!(location.canRead() && location.canWrite())) {
            throw new BeanCreationException("Cannot access repository generation directory.");
        }

        return location;
    }

    @Bean
    ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
    }

    @Bean
    CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(true);
        return loggingFilter;
    }

    @Bean
    ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        threadPoolTaskScheduler.setPoolSize(100); // TODO: #32967 fetch from the configuration
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");

        return threadPoolTaskScheduler;
    }

    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return objectMapper;
    }

    @Bean
    PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
