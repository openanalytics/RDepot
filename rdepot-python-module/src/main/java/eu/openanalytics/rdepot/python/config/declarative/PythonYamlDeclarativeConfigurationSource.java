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
package eu.openanalytics.rdepot.python.config.declarative;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu.openanalytics.rdepot.base.config.declarative.DeclaredRepositoryDirectoriesProps;
import eu.openanalytics.rdepot.base.config.declarative.exceptions.DeclaredRepositoryTechnologyMismatch;
import eu.openanalytics.rdepot.base.config.declarative.exceptions.InvalidRepositoryDeclaration;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PythonYamlDeclarativeConfigurationSource {

    protected final DeclaredRepositoryDirectoriesProps declaredRepositoryDirectoriesProps;
    protected final Class<DeclarativePythonRepository> repositoryClass = DeclarativePythonRepository.class;
    protected final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public PythonYamlDeclarativeConfigurationSource(
            DeclaredRepositoryDirectoriesProps declaredRepositoryDirectoriesProps) {
        this.declaredRepositoryDirectoriesProps = declaredRepositoryDirectoriesProps;
        mapper.findAndRegisterModules();
    }

    public List<DeclarativePythonRepository> retrieveDeclaredRepositories() {
        final List<DeclarativePythonRepository> declaredRepositories = new ArrayList<>();

        for (String directoryPath : declaredRepositoryDirectoriesProps.getPaths()) {
            declaredRepositories.addAll(retrieveRepositoriesFromDirectory(directoryPath));
        }

        return declaredRepositories;
    }

    /**
     * Parses YAML repository declarations from provided directory.
     */
    protected List<DeclarativePythonRepository> retrieveRepositoriesFromDirectory(String directoryPath) {
        final List<DeclarativePythonRepository> declaredRepositories = new ArrayList<>();
        final File dir = new File(directoryPath);

        if (!dir.exists()) {
            log.error("Directory with repository declarations: {} does not exist.", directoryPath);
            return declaredRepositories;
        }

        File[] filesInDir = dir.listFiles();
        if (filesInDir == null) return declaredRepositories;

        for (File configFile : filesInDir) {
            if (configFile.getName().endsWith("repository.yaml")
                    || configFile.getName().endsWith("repository.yml")) {
                try {
                    declaredRepositories.add(retrieveDeclaredRepositoryFromFile(configFile));
                } catch (DeclaredRepositoryTechnologyMismatch e) {
                    log.debug(e.getMessage(), e);
                } catch (InvalidRepositoryDeclaration e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        return declaredRepositories;
    }

    /**
     * Parses provided YAML file into repository declaration.
     * @throws InvalidRepositoryDeclaration when YAML could not be parsed into repository
     */
    protected DeclarativePythonRepository retrieveDeclaredRepositoryFromFile(File configFile)
            throws InvalidRepositoryDeclaration, DeclaredRepositoryTechnologyMismatch {
        try {
            DeclarativePythonRepository repository = mapper.readValue(configFile, repositoryClass);
            if (repository.technology == null || !repository.technology.equals(PythonLanguage.instance)) {
                throw new DeclaredRepositoryTechnologyMismatch(repository.name);
            }
            return repository;
        } catch (DeclaredRepositoryTechnologyMismatch e) {
            log.debug(e.getMessage(), e);
            throw new DeclaredRepositoryTechnologyMismatch(configFile.getAbsolutePath());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new InvalidRepositoryDeclaration(configFile.getAbsolutePath());
        }
    }
}
