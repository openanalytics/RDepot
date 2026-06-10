/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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

import jakarta.servlet.*;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.UrlHandlerFilter;

@Component
public class TrailingSlashFilter implements Filter {

    private final Filter actualFilter =
            UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        this.actualFilter.doFilter(request, response, chain);
    }
}
