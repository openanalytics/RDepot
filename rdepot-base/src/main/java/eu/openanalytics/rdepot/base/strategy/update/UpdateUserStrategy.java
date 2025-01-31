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
package eu.openanalytics.rdepot.base.strategy.update;

import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;
import eu.openanalytics.rdepot.base.exception.NoAdminLeftException;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;

/**
 * Updates {@link User}.
 */
public class UpdateUserStrategy extends UpdateStrategy<User> {

    private final UserService userService;
    private final CommonPackageService packageService;
    private final BestMaintainerChooser bestMaintainerChooser;
    private final RepositoryMaintainerService repositoryMaintainerService;
    private final PackageMaintainerService packageMaintainerService;

    public UpdateUserStrategy(
            User resource,
            NewsfeedEventService eventService,
            UserService userService,
            User requester,
            User updatedResource,
            CommonPackageService packageService,
            BestMaintainerChooser bestMaintainerChooser,
            RepositoryMaintainerService repositoryMaintainerService,
            PackageMaintainerService packageMaintainerService) {
        super(resource, userService, eventService, requester, updatedResource, new User(resource));
        this.userService = userService;
        this.packageService = packageService;
        this.bestMaintainerChooser = bestMaintainerChooser;
        this.repositoryMaintainerService = repositoryMaintainerService;
        this.packageMaintainerService = packageMaintainerService;
    }

    @Override
    protected User actualStrategy() throws StrategyFailure {
        if (resource.getRole().getId() != updatedResource.getRole().getId())
            updateRole(resource, updatedResource.getRole());
        if (resource.isActive() != updatedResource.isActive()) {
            if (updatedResource.isActive()) activateUser(resource);
            else deactivateUser(resource);
        }
        if (!resource.isDeleted() && updatedResource.isDeleted()) {
            softDeleteUser(resource);
        }
        return resource;
    }

    private void updateRole(User user, Role role) throws StrategyFailure {
        Role currentRole = user.getRole();

        try {
            switch (currentRole.getValue()) {
                case Role.VALUE.ADMIN:
                    if (userService.findByRole(currentRole).size() <= 1) throw new AdminNotFound();
                    else {
                        user.setRole(role);
                        refreshMaintainerForAll();
                    }
                    break;
                case Role.VALUE.PACKAGEMAINTAINER:
                    user.setRole(role);
                    deletePackageMaintainers(user);
                    break;
                case Role.VALUE.REPOSITORYMAINTAINER:
                    user.setRole(role);
                    deleteRepositoryMaintainers(user);
                    break;
                default:
                    user.setRole(role);
                    break;
            }
        } catch (AdminNotFound | NoSuitableMaintainerFound e) {
            throw new StrategyFailure(e);
        }

        changedValues.add(new EventChangedVariable("role", currentRole.getName(), role.getName()));
    }

    private void deleteRepositoryMaintainers(User user) throws NoSuitableMaintainerFound {
        for (RepositoryMaintainer maintainer : repositoryMaintainerService.findByUserWithoutDeleted(user)) {
            maintainer.setDeleted(true);

            for (Package packageBag : packageService.findAllByRepository(maintainer.getRepository())) {
                packageBag.setUser(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag));
            }
        }
    }

    private void deletePackageMaintainers(User user) throws NoSuitableMaintainerFound {
        for (PackageMaintainer packageMaintainer : packageMaintainerService.findByUser(user)) {
            packageMaintainer.setDeleted(true);
            for (Package packageBag : packageService.findAllByNameAndRepository(
                    packageMaintainer.getPackageName(), packageMaintainer.getRepository())) {
                packageBag.setUser(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag));
            }
        }
    }

    private void refreshMaintainerForAll() throws NoSuitableMaintainerFound {
        for (Package packageBag : packageService.findAll()) {
            packageBag.setUser(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag));
        }
    }

    private void activateUser(User user) {
        user.setActive(true);
        changedValues.add(new EventChangedVariable("active", Boolean.FALSE.toString(), Boolean.TRUE.toString()));
    }

    private void deactivateUser(User user) throws StrategyFailure {
        try {
            if (user.getRole().getValue() == Role.VALUE.ADMIN
                    && bestMaintainerChooser.findAllAdmins().size() <= 1) {
                throw new NoAdminLeftException();
            }
            // TODO #32885 what if this user is an admin and some packages has this user in user field? And when for
            // example user
            // has role of packageMaintainer, then we should check if there exists some PackageMaintainer
            // objects with him (the same situation for RepositoryMaintainer)
        } catch (NoAdminLeftException e) {
            throw new StrategyFailure(e);
        }

        user.setActive(false);
        for (AccessToken token : user.getAccessTokens()) {
            if (token.isActive()) {
                token.setActive(false);
            }
        }
        changedValues.add(new EventChangedVariable("active", Boolean.TRUE.toString(), Boolean.FALSE.toString()));
    }

    private void softDeleteUser(User user) throws StrategyFailure {
        if (user.isActive()) deactivateUser(user);
        user.setDeleted(true);
        changedValues.add(new EventChangedVariable("deleted", Boolean.FALSE.toString(), Boolean.TRUE.toString()));
    }

    @Override
    protected NewsfeedEvent generateEvent(User resource) {
        return new NewsfeedEvent(requester, NewsfeedEventType.UPDATE, resource);
    }
}
