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
package eu.openanalytics.rdepot.base.storage.indexes;

import eu.openanalytics.rdepot.base.entities.HavingHashMethod;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.ContentEditException;
import eu.openanalytics.rdepot.base.storage.exceptions.Md5SumCalculationException;
import eu.openanalytics.rdepot.base.storage.indexes.utils.PackagePublicationURIResolver;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Entities;

@Slf4j
public abstract class RepositoryIndexGenerator<R extends Repository, P extends Package> extends IndexGenerator<R, P> {
    public RepositoryIndexGenerator(
            String headerTemplate,
            String anchorTemplate,
            LocalStorage<P> localStorage,
            PackagePublicationURIResolver<P> packagePublicationURIResolver) {
        super(headerTemplate, anchorTemplate, localStorage, packagePublicationURIResolver);
    }

    protected String generateHeader(R repository) {
        return headerTemplate
                .replace("$document_title", repository.getName())
                .replace("$title", repository.getName())
                .replace("$meta_name", String.format("%s:repository-version", Entities.escape(repository.getName())))
                .replace(
                        "$meta_content", Entities.escape(repository.getVersion().toString()));
    }

    @Override
    public String generateIndex(R repository, List<P> packages, String path) throws ContentEditException {
        generateEmptyIndex(repository, path);
        return addPackagesToList(packages, repository, path);
    }

    public void generateEmptyIndex(R repository, String path) throws ContentEditException {
        final String withHeaderResolved = generateHeader(repository);
        localStorage.appendText(withHeaderResolved, path);
    }

    @Override
    protected String calculateChecksum(HavingHashMethod item, String path) throws CheckSumCalculationException {
        try {
            return localStorage.calculateMd5Sum(path);
        } catch (Md5SumCalculationException e) {
            log.error(e.getMessage(), e);
            throw new CheckSumCalculationException();
        }
    }
}
