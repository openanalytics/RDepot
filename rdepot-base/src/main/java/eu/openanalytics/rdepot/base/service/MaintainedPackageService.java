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
package eu.openanalytics.rdepot.base.service;

import eu.openanalytics.rdepot.base.api.v2.dtos.MaintainedPackageDto;
import eu.openanalytics.rdepot.base.daos.MaintainedPackageDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MaintainedPackageService {

    protected final MaintainedPackageDao dao;

    public MaintainedPackageService(MaintainedPackageDao dao) {
        this.dao = dao;
    }

    public Page<MaintainedPackageDto> findMaintainedPackages(
            int userId, String name, Integer repositoryId, String repositoryName, Pageable pageable) {
        return dao.findAllMaintainedPackages(userId, name, repositoryId, repositoryName, pageable);
    }
}
