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
package eu.openanalytics.rdepot.r.mediator.deletion;

import eu.openanalytics.rdepot.base.mediator.deletion.RepositoryDeleter;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import org.springframework.stereotype.Component;

@Component
public class RRepositoryDeleter extends RepositoryDeleter<RRepository, RPackage> {

    public RRepositoryDeleter(
            NewsfeedEventService newsfeedEventService,
            RRepositoryService resourceService,
            PackageMaintainerService packageMaintainerService,
            RepositoryMaintainerService repositoryMaintainerService,
            Storage<RRepository, RPackage> storage,
            RSubmissionDeleter submissionDeleter,
            RPackageService rPackageService) {
        super(
                newsfeedEventService,
                resourceService,
                packageMaintainerService,
                repositoryMaintainerService,
                storage,
                submissionDeleter,
                rPackageService);
    }
}
