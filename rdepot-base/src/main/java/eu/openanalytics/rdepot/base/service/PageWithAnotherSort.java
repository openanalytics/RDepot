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
package eu.openanalytics.rdepot.base.service;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Used to create a {@link Page} with another {@link Sort}.
 * It is used to preserve the original HATEOAS linking in case of situation
 * described over {@link SpringDataJpaCapableRetriever#fixPageable(Pageable)}.
 */
@AllArgsConstructor
public class PageWithAnotherSort<T> implements Page<T> {
    private final Page<T> basePage;
    private final Pageable newPageable;

    @Override
    public int getTotalPages() {
        return basePage.getTotalPages();
    }

    @Override
    public long getTotalElements() {
        return basePage.getTotalElements();
    }

    @Override
    public int getNumber() {
        return basePage.getNumber();
    }

    @Override
    public int getSize() {
        return basePage.getSize();
    }

    @Override
    public int getNumberOfElements() {
        return basePage.getNumberOfElements();
    }

    @Override
    public List<T> getContent() {
        return basePage.getContent();
    }

    @Override
    public boolean hasContent() {
        return basePage.hasContent();
    }

    @Override
    public Sort getSort() {
        return newPageable.getSort();
    }

    @Override
    public boolean isFirst() {
        return basePage.isFirst();
    }

    @Override
    public boolean isLast() {
        return basePage.isLast();
    }

    @Override
    public boolean hasNext() {
        return basePage.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return basePage.hasPrevious();
    }

    @Override
    public Pageable nextPageable() {
        return newPageable.next();
    }

    @Override
    public Pageable previousPageable() {
        return newPageable.previousOrFirst();
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return basePage.map(converter);
    }

    @Override
    public Iterator<T> iterator() {
        return basePage.iterator();
    }
}
