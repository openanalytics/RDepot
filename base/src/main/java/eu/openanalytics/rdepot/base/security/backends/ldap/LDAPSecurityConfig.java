/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.security.backends.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import eu.openanalytics.rdepot.base.security.authenticators.LDAPCustomBindAuthenticator;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediatorImpl;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled=true)
@ComponentScan("eu.openanalytics.rdepot")
@ConditionalOnProperty(value = "app.authentication", havingValue = "ldap")
@Order(2)
public class LDAPSecurityConfig extends WebSecurityConfigurerAdapter {	
    
    @Resource
	private Environment env;
    
    @Autowired
    private RepositoryMaintainerService repositoryMaintainerService;
    
    @Autowired
    private PackageMaintainerService packageMaintainerService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;
    
    @Value("${app.ldap.url}")
	private String ldapUrl;
    
    @Value("${app.ldap.basedn}")
	private String ldapBasedn;
    
    @Value("${app.ldap.userou:}")
	private String ldapUserou;
    
    @Value("${app.ldap.loginfield}")
	private String ldapLoginfield;
    
    @Value("${app.ldap.searchbase:}")
	private String ldapSearchbase;
    
    @Value("${app.ldap.manager.dn:}")
	private String ldapManagerDn;
    
    @Value("${app.ldap.manager.password:}")
	private String ldapManagerPassword;
	
    private static final Logger log = LoggerFactory.getLogger(LDAPSecurityConfig.class);

	public static void validateConfiguration(String value, String name) {
		if (value == null || value.trim().isEmpty())
			throw new IllegalArgumentException("Configuration value '" + name + "' is either null or empty");
		log.info("Using value '" + value + "' for property " + name);
	}
	
	public static void validateConfiguration(List<String> values, String name) {
		if (values == null || values.isEmpty())
			throw new IllegalArgumentException("Configuration value '" + name + "' is either null or empty");
		for(String value : values)
			log.info("Using value '" + value + "' for property " + name);
	}
    	
	@Override
    public void configure(WebSecurity web) throws Exception {
		web
	      	.ignoring()
	        .antMatchers("/static/**");
	}

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http
    		.authorizeRequests()
	        	.antMatchers("/manager/**").hasAuthority("user")
	            .antMatchers("/static/**").permitAll()
	        .and()
	            .formLogin()
	                .loginPage("/login")
	                .defaultSuccessUrl("/manager")
	                .failureUrl("/loginfailed")
	                .permitAll()
	        .and()
	            .logout()
	            	.invalidateHttpSession(true)
	        .and()
	        	.exceptionHandling().accessDeniedPage("/accessdenied");
	    }
	    
	
	@Bean
    public ProviderManager ldapAuthenticationManager() {
    	List<AuthenticationProvider> authenticationProviders = new ArrayList<AuthenticationProvider>();
    	
    	authenticationProviders.add(LDAPAuthenticationProvider());
    	
    	return new ProviderManager(authenticationProviders);
    }
    
    @Bean
    public LdapAuthoritiesPopulator ldapUserService() {
    	return new SecurityMediatorImpl(repositoryMaintainerService, 
    			packageMaintainerService, userService, env, roleService);
    }
    
    @Bean
    public AbstractLdapAuthenticationProvider LDAPAuthenticationProvider() {
    	LdapAuthenticationProvider ldapAuthProvider = new LdapAuthenticationProvider(LDAPAuthenticator(), ldapUserService());
    	return ldapAuthProvider;
    }
    
    @Bean
    public AbstractLdapAuthenticator LDAPAuthenticator() {
    	validateConfiguration(ldapLoginfield, "ldap.loginfield");
    	LDAPCustomBindAuthenticator bind = new LDAPCustomBindAuthenticator(LDAPContextSource(), userService, roleService, env);
    	String userDnPattern = ldapLoginfield + "={0}";
    	if (ldapUserou != null && !ldapUserou.trim().isEmpty())
    		userDnPattern += ",ou=" + ldapUserou;
		log.info("Using value '" + ldapUserou + "' for property ldap.userou");
		log.info("Using value '" + userDnPattern + "' as the User Dn Pattern");
    	bind.setUserDnPatterns(new String[]{userDnPattern});
    	bind.setUserSearch(userSearch());
    	return bind;
    }
    
    @Bean
    public DefaultSpringSecurityContextSource LDAPContextSource() {
    	validateConfiguration(ldapUrl, "ldap.url");
    	validateConfiguration(ldapBasedn, "ldap.basedn");
    	String url = ldapUrl + "/" + ldapBasedn;
    	DefaultSpringSecurityContextSource ctxsrc = new DefaultSpringSecurityContextSource(url);
    	if (ldapManagerDn != null && !ldapManagerDn.trim().isEmpty())
    	{
    		log.info("Using value " + ldapManagerDn + " as Manager Dn");
    		ctxsrc.setUserDn(ldapManagerDn);
    	}
    	if (ldapManagerPassword != null && !ldapManagerPassword.trim().isEmpty())
    		ctxsrc.setPassword(ldapManagerPassword);
    	return ctxsrc;
    }
    
    @Bean
    public FilterBasedLdapUserSearch userSearch() {
    	validateConfiguration(ldapLoginfield, "ldap.loginfield");
    	String filter = "(" + ldapLoginfield + "={0})";
    	String searchBase = "";
    	if (ldapSearchbase != null && !ldapSearchbase.trim().isEmpty())
    		searchBase = ldapSearchbase;
    	FilterBasedLdapUserSearch uSearch = new FilterBasedLdapUserSearch(searchBase, filter, LDAPContextSource());
    	return uSearch;
    }
}
