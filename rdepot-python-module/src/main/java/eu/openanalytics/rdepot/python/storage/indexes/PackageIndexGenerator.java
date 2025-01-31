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
package eu.openanalytics.rdepot.python.storage.indexes;

import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.utils.PublicationURIUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PackageIndexGenerator extends IndexesGenerator {

    private final Resource indexTemplate;

    @Autowired
    public PackageIndexGenerator(@Value("classpath:templates/index_template.html") Resource indexTemplate) {
        this.indexTemplate = indexTemplate;
    }

    protected String getPackageAnchor(PythonPackage packageBag) {
        String packageFileName = packageBag.getName() + "-" + packageBag.getVersion() + ".tar.gz";
        String anchor = "<a href=\"%s\" data-requires-python=\"%s\" data-version=\"%s\">%s</a><br>";

        String publicationUri;
        try {
            publicationUri = PublicationURIUtils.resolveToRelativeURL(
                    packageBag.getRepository().getPublicationUri());
        } catch (MalformedURLException | URISyntaxException e) {
            log.debug(e.getMessage(), e);
            publicationUri = packageBag.getRepository().getPublicationUri();
        }
        return String.format(
                anchor,
                publicationUri + separator + packageBag.getName() + separator + packageFileName,
                packageBag.getRequiresPython(),
                packageBag.getVersion(),
                packageFileName);
    }

    protected String prepareTemplateString(String template, PythonPackage packageBag) {
        return template.replace("$document_title", String.format("Links for %s", packageBag.getName()))
                .replace("$title", String.format("Links for %s", packageBag.getName()))
                .replace(
                        "$meta_name",
                        String.format(
                                "%s:repository-version",
                                packageBag.getRepository().getName()))
                .replace(
                        "$meta_content", packageBag.getRepository().getVersion().toString());
    }

    public void createIndexFile(PythonPackage packageBag, String path) throws IOException {
        String initialTemplate = getTemplateString(indexTemplate);
        String finalTemplate = prepareTemplateString(initialTemplate, packageBag);
        writeTemplateStringToFile(path, finalTemplate);
    }
}
