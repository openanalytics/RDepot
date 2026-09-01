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
package eu.openanalytics.rdepot.python.api.v2.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.hateoas.AbstractRoleAwareModelAssembler;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.python.api.v2.controllers.PythonPackageController;
import eu.openanalytics.rdepot.python.api.v2.controllers.PythonRepositoryController;
import eu.openanalytics.rdepot.python.api.v2.converters.PythonPackageDtoConverter;
import eu.openanalytics.rdepot.python.api.v2.dtos.PythonPackageDto;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class PythonPackageModelAssembler extends AbstractRoleAwareModelAssembler<PythonPackage, PythonPackageDto> {

    private final PythonPackageDtoConverter pythonPackageConverter;
    private final SecurityMediator securityMediator;

    @Value("${declarative}")
    private String declarative;

    @Autowired
    public PythonPackageModelAssembler(
            DtoConverter<PythonPackage, PythonPackageDto> dtoConverter,
            SecurityMediator securityMediator,
            PythonPackageDtoConverter pythonPackageDtoConverter) {
        super(dtoConverter, PythonPackageController.class, "package", Optional.empty());
        this.securityMediator = securityMediator;
        this.pythonPackageConverter = pythonPackageDtoConverter;
    }

    private PythonPackageModelAssembler(
            DtoConverter<PythonPackage, PythonPackageDto> dtoConverter,
            SecurityMediator securityMediator,
            User user,
            PythonPackageDtoConverter pythonPackageDtoConverter) {
        super(dtoConverter, PythonPackageController.class, "package", Optional.of(user));
        this.pythonPackageConverter = pythonPackageDtoConverter;
        this.securityMediator = securityMediator;
    }

    @Override
    public RepresentationModelAssembler<PythonPackage, EntityModel<PythonPackageDto>> assemblerWithUser(User user) {
        return new PythonPackageModelAssembler(dtoConverter, securityMediator, user, pythonPackageConverter);
    }

    @Override
    protected List<Link> getLinksToMethodsWithLimitedAccess(PythonPackage entity, User user, Link baseLink) {
        List<Link> links = new ArrayList<>();

        if (securityMediator.isAuthorizedToEdit(entity, user) && !Boolean.parseBoolean(declarative)) {
            links.add(baseLink.withType(HTTP_METHODS.PATCH.getValue()));
            links.add(baseLink.withType(HTTP_METHODS.DELETE.getValue()));
        }

        return links;
    }

    @Override
    protected Class<?> getExtensionControllerClass(PythonPackage entity) {
        return PythonPackageController.class;
    }

    @Override
    protected List<Link> generateAvailableLinksForEntity(PythonPackage entity, Class<?> extensionControllerClass) {
        List<Link> links = new ArrayList<>(super.generateAvailableLinksForEntity(entity, extensionControllerClass));
        links.add(linkTo(PythonRepositoryController.class)
                .slash(entity.getRepository().getId())
                .withRel("repository"));
        return links;
    }

    @Override
    public EntityModel<PythonPackageDto> toModel(PythonPackage entity, User user) {
        PythonPackageDto dto = dtoConverter.convertEntityToDto(entity);
        dto.setPermissions(securityMediator.getPermissions(entity, user));

        return EntityModel.of(dto, generateRoleBasedAvailableLinksForEntity(entity, user));
    }
}
