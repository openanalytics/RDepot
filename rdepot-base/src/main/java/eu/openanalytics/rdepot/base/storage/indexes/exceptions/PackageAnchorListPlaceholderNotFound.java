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
package eu.openanalytics.rdepot.base.storage.indexes.exceptions;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import java.io.Serial;

public class PackageAnchorListPlaceholderNotFound extends InvalidIndexTemplate {

    @Serial
    private static final long serialVersionUID = 8594318723055963258L;

    public PackageAnchorListPlaceholderNotFound() {
        super(MessageCodes.PACKAGE_ANCHOR_LIST_PLACEHOLDER_NOT_FOUND);
    }
}
