/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.format.Formatter;
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
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import com.google.gson.Gson;
import eu.openanalytics.rdepot.formatter.RepositoryFormatter;
import eu.openanalytics.rdepot.formatter.RoleFormatter;
import eu.openanalytics.rdepot.formatter.UserFormatter;
import eu.openanalytics.rdepot.mapper.HibernateAwareObjectMapper;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.storage.PackageStorage;
import eu.openanalytics.rdepot.storage.PackageStorageLocalImpl;
import eu.openanalytics.rdepot.storage.RepositoryStorage;
import eu.openanalytics.rdepot.storage.RepositoryStorageLocalImpl;
import eu.openanalytics.rdepot.validation.CommonsMultipartFileValidator;
import eu.openanalytics.rdepot.validation.PackageMaintainerValidator;
import eu.openanalytics.rdepot.validation.PackageValidator;
import eu.openanalytics.rdepot.validation.RepositoryMaintainerValidator;
import eu.openanalytics.rdepot.validation.RepositoryValidator;
import eu.openanalytics.rdepot.validation.UserValidator;

@Configuration
@EnableAsync
@ComponentScan("eu.openanalytics.rdepot")
@EnableJpaRepositories("eu.openanalytics.rdepot.repository")
public class WebApplicationConfig implements WebMvcConfigurer {

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
	
	private static final String PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN = "entitymanager.packages.to.scan";

	@Resource
	private Environment env;
	
	@Bean
	public DataSource dataSource() 
	{
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		//DataSource dataSource = new DataSourceTransactionManager().getDataSource();
		dataSource.setDriverClassName(databaseDriver);
		dataSource.setUrl(databaseUrl);
		dataSource.setUsername(databaseUsername);
		dataSource.setPassword(databasePassword);
		
		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() 
	{
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(dataSource());
		//entityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
		entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		entityManagerFactoryBean.setPackagesToScan(env.getRequiredProperty(PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN));
		
		entityManagerFactoryBean.setJpaProperties(hibProperties());
		
		return entityManagerFactoryBean;
	}

	private Properties hibProperties() 
	{
		Properties properties = new Properties();
		properties.put("hibernate.dialect",	hibernateDialect);
		properties.put("hibernate.show_sql", hibernateShowSql);
		return properties;
	}

	@Bean
	public JpaTransactionManager transactionManager() 
	{
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
		return transactionManager;
	}
	
    @Override
    public void addFormatters(FormatterRegistry registry)
    {
        registry.addFormatter(roleFormatter());
        registry.addFormatter(repositoryFormatter());
        registry.addFormatter(userFormatter());
    }

//	@Bean
//	public ViewResolver jspViewResolver() 
//	{
//		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
//		resolver.setPrefix("/pages/");
//		resolver.setSuffix(".jsp");
//		resolver.setViewClass(JstlView.class);
//		return resolver;
//	}
	
	@Bean
	public ResourceBundleMessageSource messageSource() 
	{
		ResourceBundleMessageSource source = new ResourceBundleMessageSource();
		source.setBasename(env.getRequiredProperty("message.source.basename"));
		source.setUseCodeAsDefaultMessage(true);
		return source;
	}
	
	@Bean
	public CookieLocaleResolver localeResolver() 
	{
		CookieLocaleResolver localeResolver = new CookieLocaleResolver();
		localeResolver.setDefaultLocale(Locale.ENGLISH);
		return localeResolver;
	}
	
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() 
	{
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");
		return localeChangeInterceptor;
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) 
	{
		registry.addResourceHandler("/static/**").addResourceLocations("/WEB-INF/static/");
	    registry.addResourceHandler("/webjars/**").addResourceLocations("/webjars/");

	}
	
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
    {
    	converters.add(jsonConverter());
    	converters.add(byteConverter());
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) 
	{		
	    registry.addInterceptor(localeChangeInterceptor());
	}
	
//	@Bean
//	public ViewResolver jsonViewResolver()
//	{
//		return new JsonViewResolver();
//	}
	
	@Bean
	public Gson jsonParser()
	{
		return new Gson();
	}
	
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) 
	{
	      configurer.ignoreAcceptHeader(false)
	                .defaultContentType(MediaType.TEXT_HTML);
	}
	 
//	@Bean
//	public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager)
//	{	 
//	    List<ViewResolver> resolvers = new ArrayList<ViewResolver>();
//
//	    resolvers.add(jspViewResolver());
//	    resolvers.add(jsonViewResolver());
//	 
//	    // Create the CNVR plugging in the resolvers and the content-negotiation manager
//	    ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
//	    resolver.setViewResolvers(resolvers);
//	    resolver.setContentNegotiationManager(manager);
//	    return resolver;
//	 }
	
    @Bean
    public MappingJackson2HttpMessageConverter jsonConverter()
    {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new HibernateAwareObjectMapper());
        return converter;
    }
    
    @Bean
    public ByteArrayHttpMessageConverter byteConverter()
    {
    	ByteArrayHttpMessageConverter converter = new ByteArrayHttpMessageConverter();
    	List<MediaType> mediaTypes = new ArrayList<MediaType>();
    	mediaTypes.add(MediaType.valueOf("application/gzip"));
    	mediaTypes.add(MediaType.valueOf("application/pdf"));
    	converter.setSupportedMediaTypes(mediaTypes);
    	return converter;
    }
    
    @Bean
    public Formatter<Role> roleFormatter()
    {
    	return new RoleFormatter();
    }
    
    @Bean
    public Formatter<User> userFormatter()
    {
    	return new UserFormatter();
    }
    
    @Bean
    public UserValidator userValidator()
    {
    	return new UserValidator();
    }
    
    @Bean
    public Formatter<Repository> repositoryFormatter()
    {
    	return new RepositoryFormatter();
    }
    
    @Bean
    public RepositoryValidator repositoryValidator()
    {
    	return new RepositoryValidator();
    }
    
    @Bean
    public RepositoryMaintainerValidator repositoryMaintainerValidator()
    {
    	return new RepositoryMaintainerValidator();
    }
    
    @Bean
    public CommonsMultipartFileValidator commonsMultipartFileValidator()
    {
    	return new CommonsMultipartFileValidator();
    }
    
    @Bean
    public PackageValidator packageValidator()
    {
    	return new PackageValidator();
    }
    
    @Bean
    public PackageMaintainerValidator packageMaintainerValidator()
    {
    	return new PackageMaintainerValidator();
    }
    
    @Bean
    public LocalValidatorFactoryBean validator()
    {
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
    			return new String[]{errorCode};
    		}
    	};
    }
    
    @Bean(name="packageUploadDirectory")
    public File packageUploadDirectory() {
    	File location = new File(packageUploadDir);
    	
    	if(!location.exists()) {
    		try {
				Files.createDirectory(location.toPath());
			} catch (IOException e) {
				throw new BeanCreationException("Cannot create package upload directory.");
			}
    	} else if(!(location.canRead() && location.canWrite())) {
    		throw new BeanCreationException("Cannot access package upload directory.");
    	}
    	
    	return location;
    }

    @Bean(name="repositoryGenerationDirectory")
    public File repositoryGenerationDirectory() {
    	File location = new File(repositoryGenerationDir);
    	
    	if(!location.exists()) {
    		try {
				Files.createDirectory(location.toPath());
			} catch (IOException e) {
				throw new BeanCreationException("Cannot create repository generation directory.");
			}
    	} else if(!(location.canRead() && location.canWrite())) {
    		throw new BeanCreationException("Cannot access repository generation directory.");
    	}
    	
    	return location;
    }
    
    @Bean
    public CommonsMultipartResolver multipartResolver()
    {
    	CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    	multipartResolver.setMaxUploadSize(100000000);
    	return multipartResolver;
    }
        
    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
    } 

    @Bean
    public PackageStorage packageStorage() {
    	String implementation = env.getRequiredProperty("storage.implementation");
    	
    	if(implementation.equals("local")) {
    		return new PackageStorageLocalImpl();
    	} else {
    		throw new BeanCreationException("Unknown storage type");
    	}
    }
    
    @Bean
    public RepositoryStorage repositoryStorage() {
    	String implementation = env.getRequiredProperty("storage.implementation");
    	
    	if(implementation.equals("local")) {
    		return new RepositoryStorageLocalImpl();
    	} else {
    		throw new BeanCreationException("Unknown storage type");
    	}
    }
    
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter()
    {
    	CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    	loggingFilter.setIncludeClientInfo(true);
    	loggingFilter.setIncludeQueryString(true);
    	loggingFilter.setIncludePayload(true);
    	loggingFilter.setIncludeHeaders(true);
    	return loggingFilter;
    }
    
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    	ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    	
    	threadPoolTaskScheduler.setPoolSize(100); //TODO: fetch from the configuration
    	threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
    	
    	return threadPoolTaskScheduler;
    }
}
