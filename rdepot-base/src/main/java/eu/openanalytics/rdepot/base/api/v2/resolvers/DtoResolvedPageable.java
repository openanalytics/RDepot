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

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
public class DtoResolvedPageable implements Pageable {
    private final Pageable basePageable;
    private final Sort resolvedEntitySort;

    public Sort getDtoSort() {
        return basePageable.getSort();
    }

    @Override
    public boolean isPaged() {
        return basePageable.isPaged();
    }

    @Override
    public boolean isUnpaged() {
        return basePageable.isUnpaged();
    }

    @Override
    public int getPageNumber() {
        return basePageable.getPageNumber();
    }

    @Override
    public int getPageSize() {
        return basePageable.getPageSize();
    }

    @Override
    public long getOffset() {
        return basePageable.getOffset();
    }

    @Override
    public @NonNull Sort getSort() {
        return resolvedEntitySort;
    }

    @Override
    public @NonNull Sort getSortOr(@NonNull Sort sort) {
        return basePageable.getSortOr(sort);
    }

    @Override
    public @NonNull Pageable next() {
        return basePageable.next();
    }

    @Override
    public @NonNull Pageable previousOrFirst() {
        return basePageable.previousOrFirst();
    }

    @Override
    public @NonNull Pageable first() {
        return basePageable.first();
    }

    @Override
    public @NonNull Pageable withPage(int pageNumber) {
        return basePageable.withPage(pageNumber);
    }

    @Override
    public boolean hasPrevious() {
        return basePageable.hasPrevious();
    }

    @Override
    public @NonNull Optional<Pageable> toOptional() {
        return basePageable.toOptional();
    }

    @Override
    public @NonNull Limit toLimit() {
        return basePageable.toLimit();
    }

    @Override
    public @NonNull OffsetScrollPosition toScrollPosition() {
        return basePageable.toScrollPosition();
    }
}
