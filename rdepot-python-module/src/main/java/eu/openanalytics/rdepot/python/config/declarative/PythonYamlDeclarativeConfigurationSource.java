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

import eu.openanalytics.rdepot.base.config.declarative.DeclaredRepositoryDirectoriesProps;
import eu.openanalytics.rdepot.base.config.declarative.YamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.base.config.declarative.exceptions.DeclaredRepositoryTechnologyMismatch;
import eu.openanalytics.rdepot.base.config.declarative.exceptions.InvalidDeclaredRepositoryName;
import eu.openanalytics.rdepot.base.config.declarative.exceptions.InvalidRepositoryDeclaration;
import eu.openanalytics.rdepot.python.mirroring.PypiMirror;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonPackage;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonRepository;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import eu.openanalytics.rdepot.python.validation.repositories.PythonBasicNameValidator;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PythonYamlDeclarativeConfigurationSource
        extends YamlDeclarativeConfigurationSource<MirroredPythonRepository, MirroredPythonPackage, PypiMirror> {

    public PythonYamlDeclarativeConfigurationSource(
            DeclaredRepositoryDirectoriesProps declaredRepositoryDirectoriesProps,
            PythonBasicNameValidator nameValidation) {
        super(declaredRepositoryDirectoriesProps, MirroredPythonRepository.class, nameValidation);
    }

    /**
     * Parses provided YAML file into repository declaration.
     * @throws InvalidRepositoryDeclaration when YAML could not be parsed into repository
     * @throws InvalidDeclaredRepositoryName when repository name is not valid
     * @throws DeclaredRepositoryTechnologyMismatch when technology is missing or it is not Python
     */
    @Override
    protected MirroredPythonRepository retrieveDeclaredRepositoryFromFile(File configFile)
            throws InvalidRepositoryDeclaration, DeclaredRepositoryTechnologyMismatch, InvalidDeclaredRepositoryName {
        try {
            MirroredPythonRepository repository = mapper.readValue(configFile, repositoryClass);
            if (repository.getTechnology() == null
                    || !repository.getTechnology().equals(PythonLanguage.instance)) {
                throw new DeclaredRepositoryTechnologyMismatch(repository.getName());
            } else {
                super.retrieveDeclaredRepositoryFromFile(configFile);
            }

            return repository;
        } catch (DeclaredRepositoryTechnologyMismatch e) {
            log.debug(e.getMessage(), e);
            throw new DeclaredRepositoryTechnologyMismatch(configFile.getAbsolutePath());
        } catch (InvalidDeclaredRepositoryName e) {
            log.debug(e.getMessage(), e);
            throw new InvalidDeclaredRepositoryName(configFile.getAbsolutePath());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new InvalidRepositoryDeclaration(configFile.getAbsolutePath());
        }
    }
}
