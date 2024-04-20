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
package eu.openanalytics.rdepot.base.config;

import java.io.IOException;
import java.util.UUID;

import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestFilterMDC extends OncePerRequestFilter {

	public static String MDC_UUID_KEY = "traceIdMDC";
	public static String MDC_REQUEST_URI_KEY = "requestURI";
	public static String MDC_QUERY_PARAMS_KEY = "queryParams";
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
									@NonNull HttpServletResponse response,
									FilterChain filterChain)
			throws ServletException, IOException {        
		MDC.clear();
		MDC.put(MDC_UUID_KEY, UUID.randomUUID().toString());
		MDC.put(MDC_REQUEST_URI_KEY, request.getRequestURI());
		MDC.put(MDC_QUERY_PARAMS_KEY, request.getQueryString());
        filterChain.doFilter(request, response);
	}

}