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
package eu.openanalytics.rdepot.r.api.v2.dtos;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.r.entities.RRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * R Package Upload Request body
 * extends PackageUploadRequest with binary properties
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RPackageUploadRequest extends PackageUploadRequest<RRepository> {

    private boolean binaryPackage;
    private String rVersion;
    private String architecture;
    private String distribution;

    public RPackageUploadRequest(
            MultipartFile fileData,
            RRepository repository,
            boolean generateManual,
            boolean replace,
            boolean binaryPackage,
            String rVersion,
            String architecture,
            String distribution) {
        super(fileData, repository, generateManual, replace);
        this.binaryPackage = binaryPackage;
        this.rVersion = rVersion;
        this.architecture = architecture;
        this.distribution = distribution;
    }
}
