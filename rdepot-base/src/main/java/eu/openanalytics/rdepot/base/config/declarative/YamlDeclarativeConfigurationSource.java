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
package eu.openanalytics.rdepot.base.config.declarative;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu.openanalytics.rdepot.base.config.declarative.exceptions.DeclaredRepositoryTechnologyMismatch;
import eu.openanalytics.rdepot.base.config.declarative.exceptions.InvalidRepositoryDeclaration;
import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredRepository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Allows to use YAML files as source for declarative configurations of repositories.
 * @param <R> technology-specific Mirrored Repository class
 * @param <P> technology-specific Mirrored Package class
 * @param <M> technology-specific Mirror class
 */
@Slf4j
public abstract class YamlDeclarativeConfigurationSource<
                R extends MirroredRepository<P, M>, P extends MirroredPackage, M extends Mirror<P>>
        implements DeclarativeConfigurationSource<R, P, M> {

    protected final DeclaredRepositoryDirectoriesProps declaredRepositoryDirectoriesProps;
    protected final Class<R> repositoryClass;
    protected final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    protected YamlDeclarativeConfigurationSource(
            DeclaredRepositoryDirectoriesProps declaredRepositoryDirectoriesProps, Class<R> repositoryClass) {
        super();
        this.declaredRepositoryDirectoriesProps = declaredRepositoryDirectoriesProps;
        this.repositoryClass = repositoryClass;
        mapper.findAndRegisterModules();
    }

    @Override
    public List<R> retrieveDeclaredRepositories() {
        final List<R> declaredRepositories = new ArrayList<>();

        for (String directoryPath : declaredRepositoryDirectoriesProps.getPaths()) {
            declaredRepositories.addAll(retrieveRepositoriesFromDirectory(directoryPath));
        }

        return declaredRepositories;
    }

    /**
     * Parses YAML repository declarations from provided directory.
     */
    protected List<R> retrieveRepositoriesFromDirectory(String directoryPath) {
        final List<R> declaredRepositories = new ArrayList<>();
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
    protected R retrieveDeclaredRepositoryFromFile(File configFile)
            throws InvalidRepositoryDeclaration, DeclaredRepositoryTechnologyMismatch {
        try {
            return mapper.readValue(configFile, repositoryClass);
        } catch (DeclaredRepositoryTechnologyMismatch e) {
            log.debug(e.getMessage(), e);
            throw new DeclaredRepositoryTechnologyMismatch(configFile.getAbsolutePath());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new InvalidRepositoryDeclaration(configFile.getAbsolutePath());
        }
    }
}
