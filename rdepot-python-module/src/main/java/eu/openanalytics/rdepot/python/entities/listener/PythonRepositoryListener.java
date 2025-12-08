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
package eu.openanalytics.rdepot.python.entities.listener;

import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.entities.PythonRepositoryAllowedFiles;
import jakarta.persistence.PostLoad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PythonRepositoryListener {

    private static PythonRepositoryAllowedFiles repositoryAllowedFiles;

    @Autowired
    public void setRepositoryAllowedFiles(PythonRepositoryAllowedFiles repositoryAllowedFiles) {
        PythonRepositoryListener.repositoryAllowedFiles = repositoryAllowedFiles;
    }

    @PostLoad
    public void postLoad(PythonRepository repository) {
        repository.setAllowedFiles(repositoryAllowedFiles.getPython());
    }
}
