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
package eu.openanalytics.rdepot.r.manuals.implementations.fs;

import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.manuals.ManualReader;
import eu.openanalytics.rdepot.r.storage.exceptions.GetReferenceManualException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@ConditionalOnProperty(name = "storage.implementation", havingValue = "local", matchIfMissing = true)
@Component
public class LocalFSManualReader implements ManualReader {

    private static final String separator = FileSystems.getDefault().getSeparator();

    @Override
    public boolean checkIfManualExists(Path path) {
        return Files.exists(path) && Files.isRegularFile(path);
    }

    @Override
    public byte[] getReferenceManual(RPackage packageBag) throws GetReferenceManualException {
        final String manualPath = new File(packageBag.getSource()).getParent() + separator + packageBag.getName()
                + separator + packageBag.getName() + ".pdf";
        final Path manualFile = Path.of(manualPath);

        try {
            return Files.readAllBytes(manualFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new GetReferenceManualException(e);
        }
    }
}
