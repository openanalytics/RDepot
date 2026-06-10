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
package eu.openanalytics.rdepot.python.utils;

import eu.openanalytics.rdepot.base.entities.enums.HashMethod;
import eu.openanalytics.rdepot.python.config.PythonProperties;
import eu.openanalytics.rdepot.python.mirroring.PypiMirror;
import eu.openanalytics.rdepot.python.mirroring.pojos.IndexFileParseResult;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonPackage;
import eu.openanalytics.rdepot.python.mirroring.pojos.ParseResult;
import eu.openanalytics.rdepot.python.utils.exceptions.ParseIndexFileException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Slf4j
@RequiredArgsConstructor
public class IndexFileParser {

    private final PythonProperties pythonProperties;
    private Pattern hashesPattern;

    private Pattern getHashesPattern() {
        if (hashesPattern != null) return hashesPattern;

        String hashFunctionsRegex = String.join("|", pythonProperties.getHashFunctions());
        hashesPattern = Pattern.compile(hashFunctionsRegex);
        return hashesPattern;
    }

    private void readIndexFile(
            Map<String, ParseResult> packages,
            MirroredPythonPackage mirrorPackage,
            HashMethod repoHashMethod,
            String mirrorUrl)
            throws ParseIndexFileException, MalformedURLException {

        URL subfolderUrl = new URL(mirrorUrl.concat(mirrorPackage.getNormalizedName()));
        String latestRemoteVersion = null;

        try {
            Document indexFile = Jsoup.connect(subfolderUrl.toString()).get();

            List<Element> packagesLinks = indexFile.select("a[href]").stream()
                    .filter(packageLink -> packageLink.text().endsWith(".tar.gz"))
                    .toList();

            ParseResult parseResult = new ParseResult();

            for (Element packageLink : packagesLinks) {
                String fileName = packageLink.text();
                String version = StringUtils.substringBetween(
                        fileName, StringUtils.substringBeforeLast(fileName, "-").concat("-"), ".tar.gz");

                if (mirrorPackage.getVersion() != null && !Objects.equals(version, mirrorPackage.getVersion()))
                    continue;

                if (mirrorPackage.getVersion() == null) {
                    latestRemoteVersion = version; // TODO: #36075 version comparison
                }

                String downloadUrl = packageLink.attr("href");

                URI uri = new URI(downloadUrl);
                if (uri.getScheme() == null) {
                    URI baseUrl = new URI(mirrorUrl);
                    uri = baseUrl.resolve(uri);
                }

                uri = uri.normalize();

                if (uri.getScheme() == null || uri.getHost() == null) {
                    throw new IllegalArgumentException("Invalid download URL: " + uri);
                }

                parseResult.setDownloadUrl(Optional.of(uri.toString()));
                parseResult.setParseResult(IndexFileParseResult.OK);

                String hashMethod = packageLink.attr("data-hash-method");
                String checksum = packageLink.attr("data-checksum");

                if (checksum.isEmpty()) {

                    Matcher matcher = getHashesPattern().matcher(uri.toString());

                    if (matcher.find() && matcher.group().equals(repoHashMethod.getValue())) {
                        parseResult.setHash(
                                Optional.of(StringUtils.substringAfter(uri.toString(), matcher.group() + "=")));
                    }
                } else if (hashMethod.equals(repoHashMethod.getValue())) {
                    parseResult.setHash(Optional.of(checksum));
                }
            }

            parseResult.setVersion(latestRemoteVersion);

            packages.put(mirrorPackage.toString(), parseResult);

        } catch (IOException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new ParseIndexFileException(mirrorUrl.concat(mirrorPackage.getNormalizedName()));
        } catch (URISyntaxException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new IllegalArgumentException("Invalid download URL for: " + mirrorPackage.getName());
        }
    }

    public Map<String, ParseResult> parseIndexFile(PypiMirror mirror, HashMethod hashMethod) {
        Map<String, ParseResult> packages = new HashMap<>();
        String url = mirror.getUriWithTrailingSlash();

        for (MirroredPythonPackage packageBag : mirror.getPackages()) {
            try {
                readIndexFile(packages, packageBag, hashMethod, url);
            } catch (ParseIndexFileException e) {
                packages.put(
                        packageBag.toString(),
                        new ParseResult(IndexFileParseResult.PARSE_EXCEPTION, packageBag.getVersion()));
            } catch (MalformedURLException e) {
                log.error("{} for {} in {} mirror", e.getMessage(), packageBag.getName(), mirror.getName());
                packages.put(
                        packageBag.toString(),
                        new ParseResult(IndexFileParseResult.MALFORMED_URL, packageBag.getVersion()));
            }
        }

        return packages;
    }
}
