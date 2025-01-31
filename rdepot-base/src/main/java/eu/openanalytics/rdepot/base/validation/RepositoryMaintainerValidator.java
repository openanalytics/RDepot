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
package eu.openanalytics.rdepot.base.validation;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.UserService;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates repository maintainer
 */
@Component
public class RepositoryMaintainerValidator implements Validator {

    private final UserService userService;
    private final RepositoryService<Repository> repositoryService;
    private final RepositoryMaintainerService repositoryMaintainerService;

    public RepositoryMaintainerValidator(
            UserService userService,
            RepositoryService<Repository> repositoryService,
            RepositoryMaintainerService repositoryMaintainerService) {
        this.userService = userService;
        this.repositoryService = repositoryService;
        this.repositoryMaintainerService = repositoryMaintainerService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RepositoryMaintainer.class);
    }

    protected void validateUser(RepositoryMaintainer maintainer, Errors errors) {
        Optional<User> userOpt = Optional.empty();

        if (maintainer.getUser() != null) {
            userOpt = userService.findById(maintainer.getUser().getId());
        }

        if (userOpt.isEmpty()) {
            errors.rejectValue("user", MessageCodes.EMPTY_USER);
        } else if (userOpt.get().getRole().getValue() < Role.VALUE.REPOSITORYMAINTAINER) {
            errors.rejectValue("user", MessageCodes.USER_PERMISSIONS_NOT_SUFFICIENT);
        }
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        RepositoryMaintainer maintainer = (RepositoryMaintainer) target;

        validateUser(maintainer, errors);
        if (maintainer.getRepository() == null
                || repositoryService
                        .findById(maintainer.getRepository().getId())
                        .isEmpty()) {
            errors.rejectValue("repository", MessageCodes.EMPTY_REPOSITORY);
        }
        if (maintainer.getId() <= 0
                && repositoryMaintainerService.existsByRepositoryAndUserAndDeleted(
                        maintainer.getRepository(), maintainer.getUser(), false)) {
            errors.rejectValue("repository", MessageCodes.REPOSITORYMAINTAINER_DUPLICATE);
        }
    }
}
