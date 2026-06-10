/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.base.storage.indexes.utils;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.utils.PublicationURIUtils;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PackagePublicationURIResolver<T extends Package> {

    protected String resolveRepositoryPublicationUri(T packageBag) {
        String publicationUri;
        try {
            publicationUri = PublicationURIUtils.resolveToRelativeURL(
                    packageBag.getRepository().getPublicationUri());
        } catch (MalformedURLException | URISyntaxException e) {
            log.debug(e.getMessage(), e);
            publicationUri = packageBag.getRepository().getPublicationUri();
        }
        return publicationUri;
    }

    protected abstract String resolveDirectory(T packageBag);

    public String resolvePackageUri(T packageBag) {
        return resolveDirectory(packageBag) + "/" + packageBag.getName();
    }
}
