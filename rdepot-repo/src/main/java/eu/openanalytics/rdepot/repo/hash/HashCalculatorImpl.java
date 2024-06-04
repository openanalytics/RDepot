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
package eu.openanalytics.rdepot.repo.hash;

import eu.openanalytics.rdepot.repo.hash.model.HashMethod;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HashCalculatorImpl implements HashCalculator {
    @Override
    public Optional<String> calculate(InputStream data, HashMethod method) {
        try {
            return Optional.of(
                    switch (method) {
                        case MD5 -> DigestUtils.md5Hex(data);
                        case SHA1 -> DigestUtils.sha1Hex(data);
                        case SHA224 -> DigestUtils.sha3_224Hex(data);
                        case SHA256 -> DigestUtils.sha256Hex(data);
                        case SHA384 -> DigestUtils.sha384Hex(data);
                        case SHA512 -> DigestUtils.sha512Hex(data);
                    });
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            return Optional.empty();
        }
    }
}
