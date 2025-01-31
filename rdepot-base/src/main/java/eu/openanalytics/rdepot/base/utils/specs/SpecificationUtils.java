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
package eu.openanalytics.rdepot.base.utils.specs;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;

/**
 * Methods declared in this class are used to build
 * {@link Specification Specifications} for {@link JpaRepository DAOs}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationUtils {

    /**
     * Makes an AND statement out of two {@link Specification Specifications}.
     * This method does not modify the existing statements but only creates a new one.
     * @param root left side of AND statement, if null then only right sight is taken into account
     * @param component right side of AND statement
     * @return "root AND component"
     */
    public static <T> Specification<T> andComponent(@Nullable Specification<T> root, Specification<T> component) {
        if (root == null) return component(component);
        else return root.and(component);
    }

    /**
     * Makes a WHERE statement out of the given component.
     * @return "where component"
     */
    public static <T> Specification<T> component(Specification<T> component) {
        return Specification.where(component);
    }

    /**
     * Makes an OR statement out of two {@link Specification Specifications}.
     * This method does not modify the existing statements but only creates a new one.
     * @param root left side of the OR statement, if null then only right side is taken into account.
     * @param component right side of the OR statement
     * @return "root OR component"
     */
    public static <T> Specification<T> orComponent(@Nullable Specification<T> root, Specification<T> component) {
        if (root == null) return component(component);
        else return root.or(component);
    }
}
