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
package eu.openanalytics.rdepot.base.api.v2.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2PackageController;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2RepositoryController;
import eu.openanalytics.rdepot.base.api.v2.dtos.MaintainedPackageDto;
import lombok.NonNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MaintainedPackageAssembler
        implements RepresentationModelAssembler<MaintainedPackageDto, EntityModel<MaintainedPackageDto>> {

    @Override
    public EntityModel<MaintainedPackageDto> toModel(@NonNull MaintainedPackageDto entity) {
        UriComponentsBuilder builder = linkTo(ApiV2PackageController.class).toUriComponentsBuilder();
        builder.queryParam("repository", entity.repositoryName());
        builder.queryParam("search", entity.name());
        return EntityModel.of(
                entity,
                linkTo(ApiV2RepositoryController.class)
                        .slash(entity.repositoryId())
                        .withRel("repository"),
                Link.of(builder.toUriString(), entity.name().concat(" versions")));
    }
}
