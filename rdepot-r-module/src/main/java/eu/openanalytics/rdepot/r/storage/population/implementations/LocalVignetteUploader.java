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
package eu.openanalytics.rdepot.r.storage.population.implementations;

import eu.openanalytics.rdepot.base.storage.exceptions.StoreFileException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.storage.population.VignetteUploader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "storage.implementation", havingValue = "local", matchIfMissing = true)
public class LocalVignetteUploader implements VignetteUploader {
    @Override
    public void storeVignettes(List<File> vignetteFiles, RPackage packageBag) throws StoreFileException {
        final Path packageSourceDir = Path.of(packageBag.getSource()).getParent();
        if (Objects.isNull(packageSourceDir)) {
            log.error("Could not find package's \"{}\" source directory: {}", packageBag, packageBag.getSource());
            throw new StoreFileException();
        }
        final Path vignettesPath =
                packageSourceDir.resolve(packageBag.getName()).resolve("inst").resolve("doc");
        try {
            Files.createDirectories(vignettesPath);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new StoreFileException(vignettesPath.toFile());
        }

        try {
            for (final File vignette : vignetteFiles) {
                FileUtils.copyFileToDirectory(vignette, vignettesPath.toFile());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            try {
                FileUtils.forceDelete(vignettesPath.toFile());
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
            throw new StoreFileException();
        }
    }
}
