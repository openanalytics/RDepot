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
package eu.openanalytics.rdepot.python.mirroring;

import eu.openanalytics.rdepot.python.mirroring.exceptions.IndexDownloadException;
import java.io.IOException;
import java.net.URL;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Slf4j
@AllArgsConstructor
class IndexDownloader {

    private final int maxIndexSize;

    public Document getIndexForMirror(PypiMirror mirror) throws IndexDownloadException {
        return getIndexForUrl(mirror.getUriWithTrailingSlash());
    }

    public Document getIndexForUrl(String mirrorUrl) throws IndexDownloadException {
        log.debug("Downloading index from {}", mirrorUrl);
        try {
            final URL repoUrl = new URL(mirrorUrl);
            return Jsoup.connect(repoUrl.toString()).maxBodySize(maxIndexSize).get();
        } catch (IOException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new IndexDownloadException(mirrorUrl);
        }
    }
}
