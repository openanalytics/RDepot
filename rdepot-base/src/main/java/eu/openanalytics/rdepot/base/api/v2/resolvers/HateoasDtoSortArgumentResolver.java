/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.base.api.v2.resolvers;

import eu.openanalytics.rdepot.base.api.v2.sorting.DtoToEntityPropertyMapping;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.web.util.UriComponentsBuilder;

@AllArgsConstructor
@Slf4j
public class HateoasDtoSortArgumentResolver extends HateoasSortHandlerMethodArgumentResolver {

    private final DtoToEntityPropertyMapping mapping;
    private final DtoToEntityPropertyMapping packageMapping;

    @Override
    public void enhance(@NonNull UriComponentsBuilder builder, MethodParameter parameter, Object value) {

        log.debug("Resolving sorting arguments for hateoas in response.");
        super.enhance(builder, parameter, value instanceof Sort ? replaceOrders((Sort) value) : value);
    }

    private Sort replaceOrders(Sort sort) {
        return Sort.by(sort.map(this::replaceOrderParameterIfNecessary).toList());
    }

    private Order replaceOrderParameterIfNecessary(Order order) {
        final String enhancedParam = mapping.entityToDto(order.getProperty())
                .orElse(packageMapping.entityToDto(order.getProperty()).orElse(order.getProperty()));
        return order.withProperty(enhancedParam);
    }
}
