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

import eu.openanalytics.rdepot.python.entities.PythonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.python.entities.PythonPackage;

import java.io.IOException;

@Component
public class RepositoryIndexGenerator extends IndexesGenerator {

	private final Resource indexTemplate;

	@Autowired
	public RepositoryIndexGenerator(@Value("classpath:templates/index_template.html") Resource indexTemplate) {
		this.indexTemplate = indexTemplate;
	}

	protected String getPackageAnchor(PythonPackage packageBag) {
		String anchor = "<a href=\"%s\">%s</a> ";
		return String.format(anchor,
				packageBag.getRepository().getPublicationUri() + separator + packageBag.getName(), 
				packageBag.getName());
	}

	protected String prepareTemplateString(String template, PythonRepository repository) {
		return template
				.replace("$document_title", repository.getName())
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
