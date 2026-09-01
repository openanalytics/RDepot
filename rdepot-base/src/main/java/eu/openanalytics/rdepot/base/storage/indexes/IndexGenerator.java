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
import eu.openanalytics.rdepot.base.storage.indexes.exceptions.PackageAnchorListPlaceholderNotFound;
import eu.openanalytics.rdepot.base.storage.indexes.utils.PackagePublicationURIResolver;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Generates index.html file that depicts the content of the repository.
 * Such a file is uploaded to repo and becomes publicly accessible
 * to external users.
 * There are different kinds of indexes - repository-wide
 * or dedicated to multiple versions of one package.
 * @param <T> what the index is generated for, usually
 *           a {@link Repository Repository}
 *           or a {@link Package}
 * @param <P> Specific type of indexed {@link Package Packages}
 */
@Slf4j
public abstract class IndexGenerator<T extends HavingHashMethod, P extends Package> {
    protected final String headerTemplate;
    protected final String anchorTemplate;
    protected final LocalStorage<P> localStorage;
    protected final String PACKAGE_ANCHOR_LIST_PLACEHOLDER = "</tbody>";
    protected final PackagePublicationURIResolver<P> packagePublicationURIResolver;

    protected IndexGenerator(
            String headerTemplate,
            String anchorTemplate,
            LocalStorage<P> localStorage,
            PackagePublicationURIResolver<P> packagePublicationURIResolver) {
        this.headerTemplate = headerTemplate;
        this.anchorTemplate = anchorTemplate;
        this.localStorage = localStorage;
        this.packagePublicationURIResolver = packagePublicationURIResolver;
    }

    /**
     * Creates a block (e.g. a div) with a single package item to add to the index.
     * There is a number of package properties it supports by default.
     * If that does not suffice, this method can be overridden and more
     * {@link String#replace(CharSequence, CharSequence) replacements} can be added.
     * The replacements are usually represented by
     * "$package_property_name" variable in the template file,
     * e.g. ("$package_version").
     * @param packageBag concrete package which will be indexed
     * @return generated block to be attached to index
     */
    protected String generatePackageAnchor(P packageBag) {
        return anchorTemplate
                .replace("$package_id", String.valueOf(packageBag.getId()))
                .replace("$package_name", packageBag.getName())
                .replace("$package_title", Objects.requireNonNullElse(packageBag.getTitle(), packageBag.getName()))
                .replace("$package_version", packageBag.getVersion())
                .replace("$package_description", Objects.requireNonNullElse(packageBag.getDescription(), ""))
                .replace("$package_publication_uri", packagePublicationURIResolver.resolvePackageUri(packageBag));
    }

    /**
     * Finds the string that is located right after the last package anchor.
     * It is used to locate where new packages should be inserted.
     */
    protected String getPackageListEnding() {
        final int placeholderPos = headerTemplate.indexOf(PACKAGE_ANCHOR_LIST_PLACEHOLDER);
        if (placeholderPos < 0) {
            throw new PackageAnchorListPlaceholderNotFound();
        }
        return headerTemplate.substring(placeholderPos);
    }

    /**
     * Adds packages to index.
     * This method writes directly to the resource.
     * @param packages packages to index
     * @param parentItem the item that will provide information about used
     * {@link eu.openanalytics.rdepot.base.entities.enums.HashMethod HashMethod}
     * @param indexPath resource path
     * @return checksum for generated index
     */
    public String addPackagesToList(List<P> packages, HavingHashMethod parentItem, String indexPath)
            throws ContentEditException {
        addPackagesToListNoChecksum(packages, indexPath);
        try {
            return calculateChecksum(parentItem, indexPath);
        } catch (CheckSumCalculationException e) {
            log.error(e.getMessage(), e);
            throw new ContentEditException(indexPath);
        }
    }

    protected void addPackagesToListNoChecksum(List<P> packages, String indexPath) throws ContentEditException {
        final String ending = getPackageListEnding();
        localStorage.removeContentFromEnd(ending, indexPath);
        final StringBuilder anchor = new StringBuilder();
        for (P packageBag : packages) {
            anchor.append(generatePackageAnchor(packageBag));
        }
        localStorage.appendText(anchor.toString(), indexPath);
        localStorage.appendText(ending, indexPath);
    }

    /**
     * The {@link #addPackagesToList(List, HavingHashMethod, String)} method's version for a single package.
     * Should only be used when the full list of packages is not known up-front.
     * Otherwise, for performance reasons, full list of packages should be supplied to
     * {@link #addPackagesToList(List, HavingHashMethod, String)}
     * @param packageBag the package to index
     * @param indexPath index resource path
     * @return checksum for generated index
     */
    public String addPackageToList(P packageBag, String indexPath) throws ContentEditException {
        addPackagesToListNoChecksum(List.of(packageBag), indexPath);
        try {
            return calculateChecksum(packageBag.getRepository(), indexPath);
        } catch (CheckSumCalculationException e) {
            log.error(e.getMessage(), e);
            throw new ContentEditException(indexPath);
        }
    }

    /**
     * Should generate the index in the provided location.
     * This is an abstract method used for any kind of index.
     * @param item item for which the index should be generated (e.g. repository or package name)
     * @param packages packages to be indexed
     * @param path location of the index resource (it is localStorage-independent
     *             thus can be a file system path or remote location URI)
     * @return checksum for generated index
     */
    public abstract String generateIndex(T item, List<P> packages, String path) throws ContentEditException;

    /**
     * {@link #generateIndex(T, List, String) Generates}
     * an empty index only if it was not generated before.
     * @param item item for which the index should be generated (e.g. repository or package)
     * @param path location of the index resource (it is localStorage-independent
     *             thus can be a file system path or remote location URI)
     * @return checksum for generated index
     */
    public String generateIndexIfNotExists(T item, String path) throws ContentEditException {
        if (!localStorage.exists(path)) return generateIndex(item, List.of(), path);
        else {
            try {
                return calculateChecksum(item, path);
            } catch (CheckSumCalculationException e) {
                log.error(e.getMessage(), e);
                throw new ContentEditException(path);
            }
        }
    }

    protected abstract String calculateChecksum(HavingHashMethod item, String path) throws CheckSumCalculationException;
}
