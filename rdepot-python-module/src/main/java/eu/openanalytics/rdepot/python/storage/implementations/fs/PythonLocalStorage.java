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
package eu.openanalytics.rdepot.python.storage.implementations.fs;

import eu.openanalytics.rdepot.base.entities.enums.HashMethod;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.implementations.CommonLocalStorage;
import eu.openanalytics.rdepot.python.config.PythonProperties;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.mediator.hash.HashCalculator;
import eu.openanalytics.rdepot.python.storage.PythonStorage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class PythonLocalStorage extends CommonLocalStorage<PythonPackage> implements PythonStorage {

    private final PythonProperties pythonProperties;

    @Override
    public void setCheckSum(PythonPackage packageBag) throws CheckSumCalculationException {
        log.debug("Calculating checksum for package: {}", packageBag.toString());
        final File sourceFile = new File(packageBag.getSource());
        final HashMethod hashMethod = packageBag.getRepository().getHashMethod();
        packageBag.setHash(calculateCheckSum(sourceFile, hashMethod));
    }

    public String calculateCheckSum(File file, HashMethod hashMethod) throws CheckSumCalculationException {
        final HashCalculator hashCalculator = new HashCalculator(hashMethod, file);
        return hashCalculator.calculateHash();
    }

    public String calculateCheckSum(PythonPopulatedPackage packageBag) throws CheckSumCalculationException {
        return calculateCheckSum(
                new File(packageBag.getPopulatedPath()),
                packageBag.getRepository().getHashMethod());
    }

    @Override
    public MultipartFile returnMultipartFile(String url, File destination) {
        return new MultipartFile() {

            @Override
            public void transferTo(@NonNull File destination) throws IOException, IllegalStateException {
                FileCopyUtils.copy(getInputStream(), Files.newOutputStream(destination.toPath()));
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                try {
                    return Files.size(destination.toPath());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    return -1;
                }
            }

            @Override
            public @NonNull String getOriginalFilename() {
                String[] tokens = url.split("/");
                if (tokens.length == 0) return "downloaded";

                String hashFunctionsRegex = String.join("|", pythonProperties.getHashFunctions());
                Pattern hashesPattern = Pattern.compile(hashFunctionsRegex);
                Matcher matcher = hashesPattern.matcher(tokens[tokens.length - 1]);

                if (matcher.find()) {
                    return StringUtils.substringBefore(tokens[tokens.length - 1], "#" + matcher.group());
                } else {
                    return tokens[tokens.length - 1];
                }
            }

            @Override
            public @NonNull String getName() {
                return getOriginalFilename();
            }

            @Override
            public @NonNull InputStream getInputStream() throws IOException {
                return new FileInputStream(destination);
            }

            @Override
            public String getContentType() {
                return "application/gzip";
            }

            @Override
            public byte @NonNull [] getBytes() throws IOException {
                return FileUtils.readFileToByteArray(destination);
            }
        };
    }

    @Override
    public String calculateChecksum(HashMethod hashMethod, String path) throws CheckSumCalculationException {
        final HashCalculator hashCalculator = new HashCalculator(hashMethod, new File(path));
        return hashCalculator.calculateHash();
    }
}
