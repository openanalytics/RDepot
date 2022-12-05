/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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

import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
//import eu.openanalytics.rdepot.config.HibernateAwareObjectMapper;

@Configuration
public class WebApplicationTestConfig implements WebMvcConfigurer {
	
	private static final String repositoryGenerationDir = "/tmp/rdepot_test/generated";
	
	@TempDir
	File packageUploadDir;
	
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
		File location = packageUploadDir;
		try 
		{
			//location = new File(packageUploadDir);
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
//        converter.setObjectMapper(new HibernateAwareObjectMapper());
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
    
//    @Override
//    public void addFormatters(FormatterRegistry registry)
//    {
//        registry.addFormatter(roleFormatter());
//        registry.addFormatter(repositoryFormatter());
//        registry.addFormatter(userFormatter());
//    }

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
	public StaticMessageResolver staticMessageResolver()
	{
		return new StaticMessageResolver(messageSource());
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
}
