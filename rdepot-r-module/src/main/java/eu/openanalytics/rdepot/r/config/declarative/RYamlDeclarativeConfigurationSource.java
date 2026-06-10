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
package eu.openanalytics.rdepot.r.config.declarative;

import eu.openanalytics.rdepot.base.config.declarative.DeclaredRepositoryDirectoriesProps;
import eu.openanalytics.rdepot.base.config.declarative.YamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.base.config.declarative.exceptions.InvalidRepositoryDeclaration;
import eu.openanalytics.rdepot.r.mirroring.CranMirror;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRPackage;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRRepository;
import eu.openanalytics.rdepot.r.technology.RLanguage;
import eu.openanalytics.rdepot.r.validation.repositories.RBasicNameValidator;
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
public class RYamlDeclarativeConfigurationSource
        extends YamlDeclarativeConfigurationSource<MirroredRRepository, MirroredRPackage, CranMirror> {

    public RYamlDeclarativeConfigurationSource(
            DeclaredRepositoryDirectoriesProps declaredRepositoryDirectoriesProps, RBasicNameValidator nameValidator) {
        super(declaredRepositoryDirectoriesProps, MirroredRRepository.class, nameValidator);
    }

    @Override
    protected boolean acceptFile(File configFile) throws InvalidRepositoryDeclaration {
        try (InputStream input = new FileInputStream(configFile)) {
            Map<String, Object> data = new Yaml().load(input);
            Object technology = data.get("technology");
            return technology == null || RLanguage.instance.getName().equalsIgnoreCase(technology.toString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new InvalidRepositoryDeclaration(configFile.getAbsolutePath());
        }
    }
}
