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
package eu.openanalytics.rdepot.python.config.declarative;

import eu.openanalytics.rdepot.base.config.declarative.DeclaredRepositoryDirectoriesProps;
import eu.openanalytics.rdepot.base.config.declarative.YamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.base.config.declarative.exceptions.InvalidRepositoryDeclaration;
import eu.openanalytics.rdepot.python.mirroring.PypiMirror;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonPackage;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonRepository;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import eu.openanalytics.rdepot.python.validation.repositories.PythonBasicNameValidator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Slf4j
@Component
public class PythonYamlDeclarativeConfigurationSource
        extends YamlDeclarativeConfigurationSource<MirroredPythonRepository, MirroredPythonPackage, PypiMirror> {

    public PythonYamlDeclarativeConfigurationSource(
            DeclaredRepositoryDirectoriesProps declaredRepositoryDirectoriesProps,
            PythonBasicNameValidator nameValidation) {
        super(declaredRepositoryDirectoriesProps, MirroredPythonRepository.class, nameValidation);
    }

    @Override
    protected boolean acceptFile(File configFile) throws InvalidRepositoryDeclaration {
        try (InputStream input = new FileInputStream(configFile)) {
            Map<String, Object> data = new Yaml().load(input);
            Object technology = data.get("technology");
            return technology != null && PythonLanguage.instance.getName().equalsIgnoreCase(technology.toString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new InvalidRepositoryDeclaration(configFile.getAbsolutePath());
        }
    }
}
