/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
package eu.openanalytics.rdepot.api.v2.resolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class NestedIdSortArgumentResolver extends SortHandlerMethodArgumentResolver {

	private final Map<String, String> properties;
	
	public NestedIdSortArgumentResolver() {
		this.properties = new HashMap<>();
		properties.put("repositoryId", "repository.id");
		properties.put("packageName", "package");
		properties.put("userId", "user.id");
		properties.put("roleId", "role.id");
		properties.put("submissionId", "submission.id");
		
	}
	
	private Order convertToColumnName(Order order) {
		String currentProperty = order.getProperty();
		
		if(properties.keySet().contains(currentProperty)) {
			return order.withProperty(properties.get(currentProperty));
		} else {
			return order;
		}
	}
	
	private String getEndpointSuffix(String uri) {
		String tokens[] = uri.split("/");
		return tokens[tokens.length - 1];
	}
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return super.supportsParameter(parameter) 
				|| properties.containsKey(parameter.getParameterName());
	}

	@Override
	public Sort resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		Sort sort = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
		String URI = ((ServletWebRequest)webRequest).getRequest().getRequestURI();
		
		sort = Sort.by(sort.map(o -> convertToColumnName(o)).toList());
		
		List<Order> orders = new ArrayList<>();

		if(getEndpointSuffix(URI).startsWith("packages")) {
			
			for(Order o : sort) {
				if(!o.getProperty().startsWith("package.")) {
					orders.add(o.withProperty("package." + o.getProperty()));
				} else {
					orders.add(o);
				}
			}
			
			return Sort.by(orders);
		}
		
		return sort;
	}

	

}
