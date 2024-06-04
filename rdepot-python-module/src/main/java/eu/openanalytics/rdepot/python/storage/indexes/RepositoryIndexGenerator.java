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
package eu.openanalytics.rdepot.python.storage.indexes;

import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
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
public class RepositoryIndexGenerator extends IndexesGenerator {

    private final Resource indexTemplate;

    @Autowired
    public RepositoryIndexGenerator(@Value("classpath:templates/index_template.html") Resource indexTemplate) {
        this.indexTemplate = indexTemplate;
    }

    protected String getPackageAnchor(PythonPackage packageBag) {
        String anchor = "<a href=\"%s\">%s</a> ";
        String publicationUri;
        try {
            publicationUri = PublicationURIUtils.resolveToRelativeURL(
                    packageBag.getRepository().getPublicationUri());
        } catch (MalformedURLException | URISyntaxException e) {
            log.debug(e.getMessage(), e);
            publicationUri = packageBag.getRepository().getPublicationUri();
        }

        return String.format(anchor, publicationUri + separator + packageBag.getName(), packageBag.getName());
    }

    protected String prepareTemplateString(String template, PythonRepository repository) {
        return template.replace("$document_title", repository.getName())
                .replace("$title", "")
                .replace("$meta_name", String.format("%s:repository-version", repository.getName()))
                .replace("$meta_content", repository.getVersion().toString());
    }

    public void createIndexFile(PythonRepository repository, String path) throws IOException {
        String initialTemplate = getTemplateString(indexTemplate);
        String finalTemplate = prepareTemplateString(initialTemplate, repository);
        writeTemplateStringToFile(path, finalTemplate);
    }
}
