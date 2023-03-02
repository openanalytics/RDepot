/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.test.context;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.web.context.ConfigurableWebApplicationContext;

public class TestWebApplicationContext implements ConfigurableWebApplicationContext {

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApplicationName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getStartupDate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ApplicationContext getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsBeanDefinition(String beanName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getBeanDefinitionCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getBeanDefinitionNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getBeanNamesForType(Class<?> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
			throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getBean(String name) throws BeansException {
		if(name.equals("messageSource")) {
			ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
			messageSource.setUseCodeAsDefaultMessage(true);
			
			return messageSource;
		}
		
		return null;
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getBean(String name, Object... args) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsBean(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAliases(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BeanFactory getParentBeanFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsLocalBean(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void publishEvent(Object event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Resource[] getResources(String locationPattern) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource getResource(String location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParent(ApplicationContext parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ConfigurableEnvironment getEnvironment() {
		// TODO Auto-generated method stub
		return new StandardEnvironment();
	}

	@Override
	public void setApplicationStartup(ApplicationStartup applicationStartup) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ApplicationStartup getApplicationStartup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addApplicationListener(ApplicationListener<?> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClassLoader(ClassLoader classLoader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addProtocolResolver(ProtocolResolver resolver) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refresh() throws BeansException, IllegalStateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerShutdownHook() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNamespace(String namespace) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConfigLocation(String configLocation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfigLocations(String... configLocations) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getConfigLocations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType,
			boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
		// TODO Auto-generated method stub
		return null;
	}

}
