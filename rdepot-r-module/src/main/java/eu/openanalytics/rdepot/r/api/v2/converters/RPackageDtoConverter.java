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
package eu.openanalytics.rdepot.r.api.v2.converters;

import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageDto;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RPackageDtoConverter implements DtoConverter<RPackage, RPackageDto> {

    private final RRepositoryService repositoryService;
    private final UserService userService;
    private final SubmissionService submissionService;

    @Override
    public RPackage resolveDtoToEntity(RPackageDto dto) throws EntityResolutionException {
        RRepository repository = repositoryService
                .findById(dto.getRepository().getId())
                .orElseThrow(() -> new EntityResolutionException(dto));
        User user = userService.findById(dto.getUser().getId()).orElseThrow(() -> new EntityResolutionException(dto));
        Submission submission = submissionService
                .findById(dto.getSubmission().getId())
                .orElseThrow(() -> new EntityResolutionException(dto));
        return new RPackage(dto, repository, submission, user);
    }

    @Override
    public RPackageDto convertEntityToDto(RPackage entity) {
        return new RPackageDto(entity);
    }
}
