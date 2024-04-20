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
package eu.openanalytics.rdepot.config;

import eu.openanalytics.rdepot.base.api.v2.resolvers.HateoasDtoSortArgumentResolver;
import eu.openanalytics.rdepot.base.api.v2.sorting.DtoToEntityPropertyMapping;
import eu.openanalytics.rdepot.base.api.v2.sorting.PackageDtoToEntityPropertyMapping;
import lombok.NonNull;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration;

@Configuration
@ComponentScan("eu.openanalytics.rdepot")
public class HateoasConfig extends HateoasAwareSpringDataWebConfiguration {


    /**
     * @param context           must not be {@literal null}.
     * @param conversionService must not be {@literal null}.
     */
    public HateoasConfig(ApplicationContext context, ObjectFactory<ConversionService> conversionService) {
        super(context, conversionService);
    }

    @Bean
    @Override
    public @NonNull HateoasSortHandlerMethodArgumentResolver sortResolver() {
        return new HateoasDtoSortArgumentResolver(dtoToEntityPropertyMapping());
    }

    @Bean
    public DtoToEntityPropertyMapping dtoToEntityPropertyMapping() {
        return new DtoToEntityPropertyMapping();
    }
    
    @Bean
    public PackageDtoToEntityPropertyMapping packageDtoToEntityPropertyMapping() {
        return new PackageDtoToEntityPropertyMapping();
    }
}
