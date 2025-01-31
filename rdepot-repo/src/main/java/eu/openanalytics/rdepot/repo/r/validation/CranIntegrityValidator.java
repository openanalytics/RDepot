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
package eu.openanalytics.rdepot.repo.r.validation;

import eu.openanalytics.rdepot.repo.hash.HashCalculator;
import eu.openanalytics.rdepot.repo.r.model.SynchronizeCranRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.validation.IntegrityValidator;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CranIntegrityValidator extends IntegrityValidator<SynchronizeCranRepositoryRequestBody> {
    public CranIntegrityValidator(HashCalculator hashCalculator) {
        super(hashCalculator);
    }

    @Override
    protected boolean areHashesOfTechnologySpecificFilesValid(SynchronizeCranRepositoryRequestBody chunk) {
        final Map<String, String> checksums = chunk.getChecksums();

        for (MultipartFile file : chunk.getFilesToUploadToArchive()) {
            final String filename = file.getOriginalFilename();
            if (filename == null) return false;
            final String expectedKey = isPackagesFile(filename) ? "archive/" + filename : filename;
            if (isMultipartInvalid(file, checksums, expectedKey, chunk.getHashMethod())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected String getExpectedKeyForFile(@NotNull String filename) {
        if (isPackagesFile(filename)) {
            return "recent/" + filename;
        }
        return super.getExpectedKeyForFile(filename);
    }

    private boolean isPackagesFile(@NotNull String filename) {
        return Objects.equals(filename, "PACKAGES") || Objects.equals(filename, "PACKAGES.gz");
    }
}
