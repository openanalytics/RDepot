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
package eu.openanalytics.rdepot.base.event;

import eu.openanalytics.rdepot.base.entities.HavingSimpleDtoRepresentation;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.technology.Technology;
import lombok.EqualsAndHashCode;

/**
 * Kind of {@link Resource} which
 * {@link eu.openanalytics.rdepot.base.entities.NewsfeedEvent Newsfeed Events}
 * can be created for (when it is edited or created).
 */
@EqualsAndHashCode(callSuper = true)
public abstract class EventableResource extends Resource implements HavingSimpleDtoRepresentation {

    protected EventableResource(Technology technology, ResourceType resourceType) {
        super(technology, resourceType);
    }

    protected EventableResource(int id, Technology technology, ResourceType resourceType) {
        super(id, technology, resourceType);
    }
}
