/**
 * R Depot
 *
 * Copyright (C) 2012-2018 Open Analytics NV
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
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
import eu.openanalytics.rdepot.validation.CommonsMultipartFileValidator;
import eu.openanalytics.rdepot.validation.PackageMaintainerValidator;
import eu.openanalytics.rdepot.validation.PackageValidator;
import eu.openanalytics.rdepot.validation.RepositoryMaintainerValidator;
import eu.openanalytics.rdepot.validation.RepositoryValidator;
import eu.openanalytics.rdepot.validation.UserValidator;

@Configuration
@ComponentScan("eu.openanalytics.rdepot")
@EnableJpaRepositories("eu.openanalytics.rdepot.repository")
public class WebApplicationConfig implements WebMvcConfigurer
{

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
		// entityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistence.class);
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
    	return new LocalValidatorFactoryBean();
    }
    
    @Bean(name="packageUploadDirectory")
    public File packageUploadDirectory()
	{
		File location;
		try 
		{
			location = new File(packageUploadDir);
			if(!location.exists() || !location.canRead() || !location.canWrite())
			{
				location = new File("");
			}
		} 
		catch (Exception e) 
		{
			location = new File("");
		}
		return location;
	}
    
    @Bean(name="repositoryGenerationDirectory")
    public File repositoryGenerationDirectory()
	{
		File location;
		try 
		{
			location = new File(repositoryGenerationDir);
			if(!location.exists() || !location.canRead() || !location.canWrite())
			{
				location = new File("");
			}
		} 
		catch (Exception e) 
		{
			location = new File("");
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

}
