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
package eu.openanalytics.rdepot.base.api.v2.resolvers;

import eu.openanalytics.rdepot.base.api.v2.sorting.DtoToEntityPropertyMapping;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
@Slf4j
public abstract class PageableSortResolverImpl implements PageableSortResolver {

    private final DtoToEntityPropertyMapping mapping;

    @Override
    public DtoResolvedPageable resolve(Pageable pageable) {
        log.debug("Resolving sorting arguments for pageable.");

        final Sort dtoSort = pageable.getSort();
        final Sort resolvedEntitySort = Sort.by(dtoSort.map(
                        o -> o.withProperty(mapping.dtoToEntity(o.getProperty()).orElse(o.getProperty())))
                .toList());

        return new DtoResolvedPageable(pageable, resolvedEntitySort);
    }
}
