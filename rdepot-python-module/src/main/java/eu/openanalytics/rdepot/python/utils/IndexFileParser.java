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
package eu.openanalytics.rdepot.python.utils;

import eu.openanalytics.rdepot.python.config.PythonProperties;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.utils.exceptions.ParseIndexFileException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Slf4j
@RequiredArgsConstructor
public class IndexFileParser {

    private final PythonProperties pythonProperties;

    public Map<PythonPackage, String> parseIndexFile(List<String> subfoldersUris) throws ParseIndexFileException {
        Map<PythonPackage, String> packages = new HashMap<>();
        String hashFunctionsRegex = String.join("|", pythonProperties.getHashFunctions());
        Pattern hashesPattern = Pattern.compile(hashFunctionsRegex);

        for (String url : subfoldersUris) {
            try {
                Document indexFile = Jsoup.connect(url).get();

                Elements packagesLinks = indexFile.select("a[href]");

                for (Element packageLink : packagesLinks) {
                    String fileName = packageLink.text();
                    if (!fileName.endsWith(".tar.gz")) continue;
                    PythonPackage packageBag = new PythonPackage();
                    String packageVersion =
                            StringUtils.substringBefore(StringUtils.substringAfterLast(fileName, "-"), ".tar.gz");
                    String downloadUrl = packageLink.attr("href");

                    packageBag.setName(StringUtils.substringBeforeLast(fileName, "-"));
                    packageBag.setNormalizedName(packageBag.getName());
                    packageBag.setVersion(packageVersion);

                    Matcher matcher = hashesPattern.matcher(downloadUrl);

                    if (matcher.find()) {
                        packageBag.setHash(StringUtils.substringAfter(downloadUrl, matcher.group() + "="));
                    }
                    packages.put(packageBag, downloadUrl);
                }

            } catch (IOException e) {
                log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
                throw new ParseIndexFileException(" repository");
            }
        }

        return packages;
    }
}
