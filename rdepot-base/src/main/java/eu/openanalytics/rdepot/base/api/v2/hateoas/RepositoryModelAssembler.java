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
package eu.openanalytics.rdepot.base.api.v2.hateoas;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2ReadingController;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2RepositoryController;
import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryDto;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.technology.Technology;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/**
 * {@link RepresentationModelAssembler Model Assembler}
 * for {@link Repository Repositories}.
 */
@Component
public class RepositoryModelAssembler extends AbstractRoleAwareModelAssembler<Repository, RepositoryDto> {

    private final SecurityMediator securityMediator;
    private final Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>>
            repositoryControllerClassesByTechnology;

    @Value("${declarative}")
    private String declarative;

    @Autowired
    public RepositoryModelAssembler(
            DtoConverter<Repository, RepositoryDto> dtoConverter,
            @Qualifier("repositoryControllerClassesByTechnology")
                    Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>>
                            repositoryControllerClassesByTechnology,
            SecurityMediator securityMediator) {
        super(dtoConverter, ApiV2RepositoryController.class, "repository", Optional.empty());
        this.securityMediator = securityMediator;
        this.repositoryControllerClassesByTechnology = repositoryControllerClassesByTechnology;
    }

    private RepositoryModelAssembler(
            DtoConverter<Repository, RepositoryDto> dtoConverter,
            Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> repositoryControllerClassesByTechnology,
            SecurityMediator securityMediator,
            User user) {
        super(dtoConverter, ApiV2RepositoryController.class, "repository", Optional.of(user));
        this.securityMediator = securityMediator;
        this.repositoryControllerClassesByTechnology = repositoryControllerClassesByTechnology;
    }

    @Override
    protected List<Link> getLinksToMethodsWithLimitedAccess(Repository entity, User user, Link baseLink) {
        List<Link> links = new ArrayList<>();

        if (securityMediator.isAuthorizedToEdit(entity, user) && !Boolean.parseBoolean(declarative)) {
            links.add(baseLink.withType(HTTP_METHODS.PATCH.getValue()));
            links.add(baseLink.withType(HTTP_METHODS.DELETE.getValue()));
        }

        return links;
    }

    @Override
    public RepresentationModelAssembler<Repository, EntityModel<RepositoryDto>> assemblerWithUser(User user) {
        return new RepositoryModelAssembler(
                dtoConverter, repositoryControllerClassesByTechnology, securityMediator, user);
    }

    @Override
    protected Class<?> getExtensionControllerClass(Repository entity) {
        return repositoryControllerClassesByTechnology.getOrDefault(
                entity.getTechnology(), ApiV2RepositoryController.class);
    }
}
