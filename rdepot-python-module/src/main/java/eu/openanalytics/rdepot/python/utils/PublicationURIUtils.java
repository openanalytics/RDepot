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
package eu.openanalytics.rdepot.python.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import lombok.NonNull;

public class PublicationURIUtils {
    public static @NonNull String resolveToRelativeURL(@NonNull final String url)
            throws MalformedURLException, URISyntaxException {
        final String hostStr = getHostFromURL(url);

        final URI uri = new URI(url);
        final URI host = new URI(hostStr);

        final String relativized = host.relativize(uri).toString();
        return relativized.isBlank() ? "/" : "/" + relativized;
    }

    private static @NonNull String getHostFromURL(@NonNull final String urlStr) throws MalformedURLException {
        final URL url = new URL(urlStr);
        return url.getProtocol() + "://" + url.getAuthority();
    }
}
