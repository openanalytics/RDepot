/*
 * RDepot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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
package eu.openanalytics.rdepot.python.mediator.hash;

import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.python.entities.enums.HashMethod;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

@RequiredArgsConstructor
public class HashCalculator {

    private final HashMethod hashMethod;
    private String hash;
    private final File file;

    public String calculateHash() throws CheckSumCalculationException {
        try {
            switch (hashMethod) {
                case MD5:
                    calculateMd5();
                    break;
                case SHA1:
                    calculateSha1();
                    break;
                case SHA224:
                    calculateSha224();
                    break;
                case SHA256:
                    calculateSha256();
                    break;
                case SHA384:
                    calculateSha384();
                    break;
                case SHA512:
                    calculateSha512();
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            throw new CheckSumCalculationException();
        }

        return hash;
    }

    private FileInputStream getFileInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private void calculateMd5() throws IOException {
        hash = DigestUtils.md5Hex(getFileInputStream());
    }

    private void calculateSha1() throws IOException {
        hash = DigestUtils.sha1Hex(getFileInputStream());
    }

    private void calculateSha224() throws IOException {
        hash = DigestUtils.sha3_224Hex(getFileInputStream());
    }

    private void calculateSha256() throws IOException {
        hash = DigestUtils.sha256Hex(getFileInputStream());
    }

    private void calculateSha384() throws IOException {
        hash = DigestUtils.sha384Hex(getFileInputStream());
    }

    private void calculateSha512() throws FileNotFoundException, IOException {
        hash = DigestUtils.sha512Hex(getFileInputStream());
    }
}
