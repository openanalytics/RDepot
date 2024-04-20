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
package eu.openanalytics.rdepot.r.api.v2.hateoas;

import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.SubmissionDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.api.v2.hateoas.AbstractRoleAwareModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.hateoas.PackageModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.hateoas.linking.LinkWithModifiableProperties;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.r.api.v2.controllers.RPackageController;
import eu.openanalytics.rdepot.r.api.v2.controllers.RSubmissionController;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class RSubmissionModelAssembler 
	extends AbstractRoleAwareModelAssembler<Submission, SubmissionDto>{

	private final PackageModelAssembler packageModelAssembler;
	private final SecurityMediator securityMediator;
	
	@Autowired
	public RSubmissionModelAssembler(SubmissionDtoConverter dtoConverter,
									 PackageModelAssembler packageModelAssembler, SecurityMediator securityMediator) {
		super(dtoConverter, RSubmissionController.class, "submission", Optional.empty());
		this.packageModelAssembler = packageModelAssembler;
		this.securityMediator = securityMediator;
	}
	
	private RSubmissionModelAssembler(DtoConverter<Submission, SubmissionDto> dtoConverter,
			PackageModelAssembler packageModelAssembler, SecurityMediator securityMediator, User user) {
		super(dtoConverter, RSubmissionController.class, "submission", Optional.of(user));
		this.packageModelAssembler = packageModelAssembler;
		this.securityMediator = securityMediator;
	}

	@Override
	protected List<Link> getLinksToMethodsWithLimitedAccess(Submission entity, User user, Link baseLink) {
		final List<Link> links = new ArrayList<>();

		if(entity.getState().equals(SubmissionState.WAITING) && 
				(securityMediator.isAuthorizedToAccept(entity, user) 
				|| securityMediator.isAuthorizedToReject(entity, user) 
				|| securityMediator.isAuthorizedToCancel(entity, user))
		) {
			LinkWithModifiableProperties link = new LinkWithModifiableProperties(
						baseLink.withType(HTTP_METHODS.PATCH.getValue()), "state");
			links.add(link);
		}
		
		return links;
	}
	
	@Override
	public @NonNull EntityModel<SubmissionDto> toModel(@NonNull Submission entity) {
		final EntityModel<SubmissionDto> model = super.toModel(entity);
		Objects.requireNonNull(model.getContent()).setPackageBag(
				packageModelAssembler.toModel(entity.getPackage(), RPackageController.class)
		);
		return model;
	}
	
	@Override
	public EntityModel<SubmissionDto> toModel(Submission entity, User user) {
		final EntityModel<SubmissionDto> model = super.toModel(entity, user);

		if(model.getContent() != null) {
			model.getContent().setPackageBag(
					packageModelAssembler.toModel(entity.getPackage(), user)
			);
		}

		return model;
	}

	@Override
	public RepresentationModelAssembler<Submission, EntityModel<SubmissionDto>> assemblerWithUser(User user) {
		return new RSubmissionModelAssembler(dtoConverter, packageModelAssembler, securityMediator, user);
	}

	@Override
	protected Class<?> getExtensionControllerClass(Submission entity) {
		return RSubmissionController.class;
	}
}