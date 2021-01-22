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
package eu.openanalytics.rdepot.test.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardService;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
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
import eu.openanalytics.rdepot.storage.BaseStorage;
import eu.openanalytics.rdepot.validation.CommonsMultipartFileValidator;
import eu.openanalytics.rdepot.validation.PackageMaintainerValidator;
import eu.openanalytics.rdepot.validation.PackageValidator;
import eu.openanalytics.rdepot.validation.RepositoryMaintainerValidator;
import eu.openanalytics.rdepot.validation.RepositoryValidator;
import eu.openanalytics.rdepot.validation.UserValidator;

@Configuration
//@ComponentScan({
//	"eu.openanalytics.rdepot.controller", 
//	"eu.openanalytics.rdepot.model",
//	"eu.openanalytics.rdepot.service",
//	"eu.openanalytics.rdepot.validation",
//	"eu.openanalytics.rdepot.messaging",
//	"eu.openanalytics.rdepot.service",
//	"eu.openanalytics.rdepot.storage",
//	"eu.openanalytics.rdepot.formatter"})
public class WebApplicationTestConfig implements WebMvcConfigurer {
	
	private static final String repositoryGenerationDir = "/tmp/rdepot_test/generated";
	
	@Rule
	TemporaryFolder packageUploadDir = new TemporaryFolder();
	
	@Bean
	public WebApplicationContext webApplicationContext() {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		StandardContext standardContext = new StandardContext();
		StandardContext standardContextParent = new StandardContext();
		
		StandardEngine standardEngine = new StandardEngine();
		standardEngine.setService(new StandardService());
		standardContextParent.setParent(standardEngine);
		standardContext.setParent(standardContextParent);
		
		context.setServletContext(new ApplicationContextFacade(new ApplicationContext(standardContext)));
		
		return context;
	}
	
	@Bean(name="packageUploadDirectory")
    public File packageUploadDirectory()
	{
		File location;
		try 
		{
			//location = new File(packageUploadDir);
			location = packageUploadDir.getRoot();
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
    
    @Override
    public void addFormatters(FormatterRegistry registry)
    {
        registry.addFormatter(roleFormatter());
        registry.addFormatter(repositoryFormatter());
        registry.addFormatter(userFormatter());
    }

	@Bean
	public ResourceBundleMessageSource messageSource() 
	{
		ResourceBundleMessageSource source = new ResourceBundleMessageSource();
		//source.setBasename(env.getRequiredProperty("message.source.basename"));
		source.setBasename("i18n/messages");
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
    
    @Bean
    public BaseStorage baseStorage() {
    	return new BaseStorage();
    }
}
