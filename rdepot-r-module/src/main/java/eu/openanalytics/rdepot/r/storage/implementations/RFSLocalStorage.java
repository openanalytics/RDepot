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
package eu.openanalytics.rdepot.r.storage.implementations;

import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.implementations.CommonFSLocalStorage;
import eu.openanalytics.rdepot.r.entities.RPackage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * Local storage implementation for R.
 */
@Slf4j
@Component
public class RFSLocalStorage extends CommonFSLocalStorage<RPackage> {

    @Override
    public void setCheckSum(RPackage packageBag, File locallyStored) throws CheckSumCalculationException {
        log.debug("Calculating checksum for package: {}", packageBag.toString());
        try {
            packageBag.setMd5sum(DigestUtils.md5Hex(new FileInputStream(locallyStored)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CheckSumCalculationException();
        }
    }
}
