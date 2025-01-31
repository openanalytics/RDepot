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
package eu.openanalytics.rdepot.repo.validation;

import eu.openanalytics.rdepot.repo.hash.HashCalculator;
import eu.openanalytics.rdepot.repo.hash.model.HashMethod;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Slf4j
public abstract class IntegrityValidator<T extends SynchronizeRepositoryRequestBody> {

    private final HashCalculator hashCalculator;

    public boolean areHashesValid(T chunk) {
        Map<String, String> checksums = chunk.getChecksums();

        for (MultipartFile file : chunk.getFilesToUpload()) {
            if (file.getOriginalFilename() == null
                    || isMultipartInvalid(
                            file, checksums, getExpectedKeyForFile(file.getOriginalFilename()), chunk.getHashMethod()))
                return false;
        }

        return areHashesOfTechnologySpecificFilesValid(chunk);
    }

    protected String getExpectedKeyForFile(@NotNull String filename) {
        return filename;
    }

    protected boolean isMultipartInvalid(
            MultipartFile file, Map<String, String> checksums, String expectedKey, HashMethod hashMethod) {
        try {
            final String expectedHash = checksums.get(expectedKey);
            if (expectedHash == null) return true;

            final Optional<String> hash = hashCalculator.calculate(file.getInputStream(), hashMethod);
            if (hash.isEmpty() || !MessageDigest.isEqual(hash.get().getBytes(), expectedHash.getBytes())) return true;
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            return true;
        }

        return false;
    }

    protected abstract boolean areHashesOfTechnologySpecificFilesValid(T chunk);
}
