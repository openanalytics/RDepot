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
package eu.openanalytics.rdepot.base.api.v2.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

/**
 * Implementation template for {@link RoleAwareRepresentationModelAssembler}.
 *
 * @param <T> resource class that will be converted to model DTO
 * @param <D> DTO class that a resource will be converted to
 */
@AllArgsConstructor
public abstract class AbstractRoleAwareModelAssembler<T extends Resource, D extends IDto>
        implements RoleAwareRepresentationModelAssembler<T, EntityModel<D>> {

    protected final DtoConverter<T, D> dtoConverter;
    protected final Class<?> baseControllerClass;
    protected final String resourceTypeName;
    protected final Optional<User> user;

    protected List<Link> generateRoleBasedAvailableLinksForEntity(T entity, User user) {
        List<Link> links =
                new ArrayList<>(generateAvailableLinksForEntity(entity, getExtensionControllerClass(entity)));

        final Link baseSelfLink = linkTo(getExtensionControllerClass(entity))
                .slash(entity.getId())
                .withSelfRel();
        links.addAll(getLinksToMethodsWithLimitedAccess(entity, user, baseSelfLink));

        return links;
    }

    /**
     * Returns a list of links based on whether
     * a given user is allowed to access resources they point at.
     */
    protected abstract List<Link> getLinksToMethodsWithLimitedAccess(T entity, User user, Link baseLink);

    /**
     * Returns class of the controller takes care of given entity.
     */
    protected abstract Class<?> getExtensionControllerClass(T entity);

    /**
     * Generates list of links that should be attached
     * to the model representing given entity.
     */
    protected List<Link> generateAvailableLinksForEntity(T entity, Class<?> extensionControllerClass) {
        return List.of(
                linkTo(extensionControllerClass).slash(entity.getId()).withSelfRel(),
                linkTo(baseControllerClass).withRel(resourceTypeName + "List"));
    }

    @Override
    public @NonNull EntityModel<D> toModel(@NonNull T entity) {
        if (user.isPresent()) return toModel(entity, user.get());
        D dto = dtoConverter.convertEntityToDto(entity);

        return EntityModel.of(dto, generateAvailableLinksForEntity(entity, getExtensionControllerClass(entity)));
    }

    @Override
    public EntityModel<D> toModel(T entity, User user) {
        final D dto = dtoConverter.convertEntityToDto(entity);

        return EntityModel.of(dto, generateRoleBasedAvailableLinksForEntity(entity, user));
    }
}
