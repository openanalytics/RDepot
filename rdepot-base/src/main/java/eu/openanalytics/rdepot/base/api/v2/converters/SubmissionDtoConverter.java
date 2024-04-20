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
package eu.openanalytics.rdepot.base.api.v2.converters;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.UserService;

/**
 * {@link DtoConverter DTO Converter} for {@link Submission Submissions}
 */
@Component
public class SubmissionDtoConverter implements DtoConverter<Submission, SubmissionDto> {

	private final DtoConverter<Package, PackageDto> packageDtoConverter;
	private final PackageService<Package> packageService;
	private final UserService userService;

	@Autowired
	public SubmissionDtoConverter(PackageDtoConverter packageDtoConverter,
			PackageService<Package> packageService, UserService userService) {
		super();
		this.packageDtoConverter = packageDtoConverter;
		this.packageService = packageService;
		this.userService = userService;
	}

	@Override
	public Submission resolveDtoToEntity(SubmissionDto dto) throws EntityResolutionException {
		Package packageBag = packageService.findById(dto.getPackageBag().getContent().getId())
				.orElseThrow(() -> new EntityResolutionException(dto));
		User submitter = userService.findById(dto.getSubmitter().getId())
				.orElseThrow(() -> new EntityResolutionException(dto));
		Optional<User> approver;
		if(dto.getApprover() != null) {
			approver = userService.findById(dto.getApprover().getId());
		} else {
			approver = Optional.empty();
		}
		return new Submission(dto, packageBag, submitter, approver);
	}

	@Override
	public SubmissionDto convertEntityToDto(Submission entity) {
		return new SubmissionDto(entity, 
				packageDtoConverter.convertEntityToDto(entity.getPackage()));
	}

}
