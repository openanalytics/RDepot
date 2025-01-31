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
package eu.openanalytics.rdepot.base.utils;

import eu.openanalytics.rdepot.base.technology.Technology;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Used to format {@link Technology} objects
 * for building {@link Specification Specifications}.
 */
@Component
@NoArgsConstructor
public class TechnologyResolver {
    public List<String> getTechnologies(List<String> technologies) {
        return technologies.stream()
                .map(technology -> technology.substring(0, 1).toUpperCase()
                        + technology.substring(1).toLowerCase())
                .collect(Collectors.toList());
    }
}
