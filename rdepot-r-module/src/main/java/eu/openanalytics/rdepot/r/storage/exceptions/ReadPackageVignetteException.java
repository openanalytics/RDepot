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
package eu.openanalytics.rdepot.r.storage.exceptions;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.r.messaging.RMessageCodes;
import java.io.Serial;
import lombok.Getter;

/**
 * Thrown when {@link Package Package's}
 * vignette file is invalid or cannot be read for another reason.
 */
@Getter
public class ReadPackageVignetteException extends Exception {

    @Serial
    private static final long serialVersionUID = 620016799696468192L;

    private final Exception reason;

    public ReadPackageVignetteException(Exception reason) {
        super(StaticMessageResolver.getMessage(RMessageCodes.COULD_NOT_GET_VIGNETTE) + ": " + reason.getMessage());
        this.reason = reason;
    }
}
