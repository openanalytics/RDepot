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
package eu.openanalytics.rdepot.base.mediator.deletion.exceptions;

import eu.openanalytics.rdepot.base.exception.LocalizedException;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import java.io.Serial;

/**
 * Thrown when there is no
 * {@link eu.openanalytics.rdepot.base.entities.User user}
 * that could maintain
 * a {@link eu.openanalytics.rdepot.base.entities.Package package}.
 */
public class NoSuitableMaintainerFound extends LocalizedException {

    @Serial
    private static final long serialVersionUID = -1987352336412574307L;

    public NoSuitableMaintainerFound() {
        super(MessageCodes.NO_SUITABLE_MAINTAINER_FOUND);
    }
}
