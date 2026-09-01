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

import eu.openanalytics.rdepot.base.entities.enums.HashMethod;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.python.messaging.PythonMessageCodes;
import eu.openanalytics.rdepot.python.mirroring.exceptions.IndexDownloadException;
import eu.openanalytics.rdepot.python.mirroring.pojos.RemotePythonPackage;
import eu.openanalytics.rdepot.python.mirroring.pojos.RemotePythonPackageParseResult;
import eu.openanalytics.rdepot.python.utils.exceptions.ParseRepositoryIndexFileException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Slf4j
public class IndexFileParser {

    private final IndexDownloader indexDownloader;
    private final HashMethod hashMethod;
    private final PypiMirror mirror;
    private final Pattern hashPattern;
    private final List<String> packageNames;

    public IndexFileParser(HashMethod hashMethod, PypiMirror mirror, Pattern hashPattern, int maxIndexSize) {
        this.mirror = mirror;
        this.hashMethod = hashMethod;
        this.hashPattern = hashPattern;
        this.packageNames = List.of();
        indexDownloader = new IndexDownloader(maxIndexSize);
    }

    public IndexFileParser(
            HashMethod hashMethod,
            PypiMirror mirror,
            Pattern hashPattern,
            List<String> packageNames,
            int maxIndexSize) {
        this.mirror = mirror;
        this.hashMethod = hashMethod;
        this.hashPattern = hashPattern;
        this.packageNames = packageNames;
        indexDownloader = new IndexDownloader(maxIndexSize);
    }

    public List<RemotePythonPackageParseResult> parseRepoIndexFile() throws ParseRepositoryIndexFileException {
        try {
            final Document indexFile = indexDownloader.getIndexForMirror(mirror);
            final List<RemotePythonPackageParseResult> remotePythonPackages = new ArrayList<>();

            final List<Element> packageFolderLinks = indexFile.select("a[href]");
            for (Element packageFolderLink : packageFolderLinks) {
                final String packageName = getPackageNameFromLink(packageFolderLink.attr("href"));
                if (packageNames.isEmpty() || packageNames.contains(packageName))
                    remotePythonPackages.addAll(parsePackageIndexFile(packageFolderLink));
            }

            return remotePythonPackages;
        } catch (IndexDownloadException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new ParseRepositoryIndexFileException(mirror.getName());
        }
    }

    private String getPackageNameFromLink(String link) throws ParseRepositoryIndexFileException {
        final String[] tokens = link.split("/");
        if (tokens.length < 3 || tokens[tokens.length - 1].isBlank()) {
            log.error("Could not extract package name from url: {}", link);
            throw new ParseRepositoryIndexFileException(mirror.getName());
        }

        return tokens[tokens.length - 1];
    }

    private List<RemotePythonPackageParseResult> parsePackageIndexFile(Element packageFolderLink)
            throws IndexDownloadException {
        final List<RemotePythonPackageParseResult> remotePythonPackages = new ArrayList<>();
        final Document indexFile = indexDownloader.getIndexForUrl(packageFolderLink.absUrl("href"));

        final List<Element> packageLinks = indexFile.select("a[href]").stream()
                .filter(link -> link.text().endsWith(".tar.gz"))
                .toList();
        for (Element packageLink : packageLinks) {
            remotePythonPackages.add(parseSinglePackage(packageFolderLink.text(), packageLink));
        }
        return remotePythonPackages;
    }

    private RemotePythonPackageParseResult parseSinglePackage(String packageName, Element packageLink) {
        final String filename = packageLink.text();
        final String version = parseVersion(filename);
        final String downloadUrl;
        try {
            downloadUrl = parseDownloadUrl(packageLink);
        } catch (URISyntaxException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            return RemotePythonPackageParseResult.createUrlErrorResult(
                    packageName,
                    StaticMessageResolver.getMessage(PythonMessageCodes.COULD_NOT_PARSE_PACKAGE_INDEX_FILE));
        }
        final String checksum = parseChecksum(packageLink, downloadUrl);

        return RemotePythonPackageParseResult.createOkResult(
                new RemotePythonPackage(packageName, version, downloadUrl, checksum));
    }

    private String parseChecksum(Element packageLink, String url) {
        final String packageHashMethod = packageLink.attr("data-hash-method");
        final String checksum = packageLink.attr("data-checksum");

        if (checksum.isEmpty()) {
            final Matcher matcher = hashPattern.matcher(url);
            if (matcher.find() && matcher.group().equals(hashMethod.getValue())) {
                return StringUtils.substringAfter(url, matcher.group() + "=");
            }
        } else if (Objects.equals(packageHashMethod, hashMethod.getValue())) {
            return checksum;
        }
        return "";
    }

    private String parseDownloadUrl(Element packageLink) throws URISyntaxException {
        final String downloadUrl = packageLink.attr("href");
        URI uri = new URI(downloadUrl);
        if (Objects.isNull(uri.getScheme())) {
            final URI baseUrl = new URI(mirror.getUriWithTrailingSlash());
            uri = baseUrl.resolve(uri);
        }

        uri = uri.normalize();
        if (Objects.isNull(uri.getScheme()) || Objects.isNull(uri.getHost())) {
            throw new URISyntaxException(downloadUrl, "Invalid download URL.");
        }
        return uri.toString();
    }

    private String parseVersion(String filename) {
        return StringUtils.substringBetween(
                filename, StringUtils.substringBeforeLast(filename, "-").concat("-"), ".tar.gz");
    }
}
