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
package eu.openanalytics.rdepot.test.strategy.update;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.update.UpdateUserStrategy;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.strategy.StrategyTest;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class UpdateUserStrategyTest extends StrategyTest {
    @Mock
    NewsfeedEventService eventService;

    @Mock
    UserService service;

    @Mock
    CommonPackageService packageService;

    @Mock
    RepositoryMaintainerService repositoryMaintainerService;

    @Mock
    PackageMaintainerService packageMaintainerService;

    private Strategy<User> strategy;

    private User resource;
    private User updatedResource;
    private Role role;
    private Role roleUpdated;

    private static User requester;
    private static List<Package> packagesList;
    private static List<RepositoryMaintainer> repositoryMaintainerList;
    private static List<PackageMaintainer> packageMaintainerList;
    private static List<User> userList;
    private static int oldId;
    private static int newId;

    @BeforeAll
    public static void init() {
        Repository tmpRepository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();

        requester = UserTestFixture.GET_PACKAGE_MAINTAINER();
        oldId = 123;
        newId = 124;

        packagesList = PackageMaintainerTestFixture.GET_LIST_OF_PACKAGE_MAINTAINERS_FOR_REPOSITORY(
                3, tmpRepository, requester, "pakckage_name");

        packageMaintainerList = PackageMaintainerTestFixture.GET_PACKAGE_MAINTAINERS_FOR_REPOSITORY(3, tmpRepository);

        repositoryMaintainerList =
                RepositoryMaintainerTestFixture.GET_REPOSITORY_MAINTAINERS_FOR_REPOSITORY(3, tmpRepository);
    }

    @BeforeEach
    public void initEach() {
        resource = UserTestFixture.GET_PACKAGE_MAINTAINER();
        updatedResource = UserTestFixture.GET_PACKAGE_MAINTAINER();
        userList = List.of(resource, updatedResource);
        role = new Role();
        role.setId(oldId);
        roleUpdated = new Role();
        roleUpdated.setId(newId);
        resource.setRole(role);
        updatedResource.setRole(roleUpdated);
    }

    /**
     * prepare all condition to change role of user
     * USER = 0;
     * PACKAGEMAINTAINER = 1;
     * REPOSITORYMAINTAINER = 2;
     * ADMIN = 3;
     * @param oldRole
     * @param newRole
     * @throws Exception
     */
    private void changeUserRole(int oldRole, int newRole) throws Exception {
        resource.getRole().setValue(oldRole);
        updatedResource.getRole().setValue(newRole);

        strategy = new UpdateUserStrategy(
                resource,
                eventService,
                service,
                requester,
                updatedResource,
                packageService,
                bestMaintainerChooser,
                repositoryMaintainerService,
                packageMaintainerService);
    }

    @Test
    public void updateUser_changeFromAdmin_shouldUpdateRole() throws Exception {
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(resource);
        when(service.findByRole(any())).thenReturn(userList);
        when(packageService.findAll()).thenReturn(packagesList);

        changeUserRole(3, 2);
        strategy.perform();

        Assertions.assertEquals(resource.getRole(), updatedResource.getRole());
    }

    @Test
    public void updateUser_changeFromAdmin_shouldUpdatePackageMaintainers() throws Exception {
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(resource);
        when(service.findByRole(any())).thenReturn(userList);
        when(packageService.findAll()).thenReturn(packagesList);

        changeUserRole(3, 2);
        strategy.perform();

        verify(bestMaintainerChooser, times(3)).chooseBestPackageMaintainer(any());
    }

    @Test
    public void updateUser_changeFromAdminWhenThereAreNoOtherAdmins() throws Exception {
        when(service.findByRole(any())).thenReturn(List.of(resource));

        changeUserRole(3, 2);

        assertThrows(StrategyFailure.class, () -> strategy.perform());
    }

    @Test
    public void updateUser_changeFromAdminWhenWhenCannotChooseBestMaintainer() throws Exception {
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenThrow(NoSuitableMaintainerFound.class);
        when(packageService.findAll()).thenReturn(packagesList);
        when(service.findByRole(any())).thenReturn(userList);

        changeUserRole(3, 2);

        assertThrows(StrategyFailure.class, () -> strategy.perform());
    }

    @Test
    public void updateUser_changeFromPackageMaintainer_shouldUpdateRole() throws Exception {
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(resource);
        when(packageService.findAllByNameAndRepository(any(), any())).thenReturn(packagesList);
        when(packageMaintainerService.findByUser(resource)).thenReturn(packageMaintainerList);

        changeUserRole(1, 2);
        strategy.perform();

        Assertions.assertEquals(resource.getRole(), updatedResource.getRole());
    }

    @Test
    public void updateUser_changeFromPackageMaintainer_shouldUpdatePackageMaintainers() throws Exception {
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(resource);
        when(packageMaintainerService.findByUser(resource)).thenReturn(packageMaintainerList);
        when(packageService.findAllByNameAndRepository(any(), any())).thenReturn(packagesList);

        changeUserRole(1, 2);
        strategy.perform();

        verify(bestMaintainerChooser, times(9)).chooseBestPackageMaintainer(any());
    }

    @Test
    public void updateUser_changeFromPackageMaintainerWhenWhenCannotChooseBestMaintainer() throws Exception {
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenThrow(NoSuitableMaintainerFound.class);
        when(packageMaintainerService.findByUser(resource)).thenReturn(packageMaintainerList);
        when(packageService.findAllByNameAndRepository(any(), any())).thenReturn(packagesList);

        changeUserRole(1, 2);

        assertThrows(StrategyFailure.class, () -> strategy.perform());
    }

    @Test
    public void updateUser_changeFromRepositoryMaintainer_shouldUpdateRole() throws Exception {
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(resource);
        when(packageService.findAllByRepository(any())).thenReturn(packagesList);
        when(repositoryMaintainerService.findByUserWithoutDeleted(resource)).thenReturn(repositoryMaintainerList);

        changeUserRole(2, 1);
        strategy.perform();

        Assertions.assertEquals(resource.getRole(), updatedResource.getRole());
    }

    @Test
    public void updateUser_changeFromRepositoryMaintainer_shouldUpdatePackageMaintainers() throws Exception {
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(resource);
        when(packageService.findAllByRepository(any())).thenReturn(packagesList);
        when(repositoryMaintainerService.findByUserWithoutDeleted(resource)).thenReturn(repositoryMaintainerList);

        changeUserRole(2, 1);
        strategy.perform();

        verify(bestMaintainerChooser, times(9)).chooseBestPackageMaintainer(any());
    }

    @Test
    public void updateUser_changeFromRepositoryMaintainerWhenWhenCannotChooseBestMaintainer() throws Exception {
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenThrow(NoSuitableMaintainerFound.class);
        when(repositoryMaintainerService.findByUserWithoutDeleted(resource)).thenReturn(repositoryMaintainerList);
        when(packageService.findAllByRepository(any())).thenReturn(packagesList);

        changeUserRole(2, 1);

        assertThrows(StrategyFailure.class, () -> strategy.perform());
    }

    @Test
    public void updateUser_changeFromUser_shouldUpdateRole() throws Exception {

        changeUserRole(0, 3);
        strategy.perform();

        Assertions.assertEquals(resource.getRole(), updatedResource.getRole());
    }

    @Test
    public void updateUser_changeFromDifferent_shouldUpdateRole() throws Exception {

        changeUserRole(4, 3);
        strategy.perform();

        Assertions.assertEquals(resource.getRole(), updatedResource.getRole());
    }

    @Test
    public void updateUser_deactivateAdmin_shouldPassIfThereIsAnyOtherAdminUser() throws Exception {
        when(bestMaintainerChooser.findAllAdmins()).thenReturn(userList);

        resource.getRole().setId(newId);
        resource.setActive(true);
        updatedResource.setActive(false);
        resource.getRole().setValue(3);

        strategy = new UpdateUserStrategy(
                resource,
                eventService,
                service,
                requester,
                updatedResource,
                packageService,
                bestMaintainerChooser,
                repositoryMaintainerService,
                packageMaintainerService);

        strategy.perform();
        Assertions.assertEquals(resource.isActive(), updatedResource.isActive());
    }

    @Test
    public void updateUser_deactivateAdminIfThereIsNoOtherAdminUser() throws Exception {
        when(bestMaintainerChooser.findAllAdmins()).thenReturn(List.of(resource));

        resource.getRole().setId(newId);
        resource.setActive(true);
        updatedResource.setActive(false);
        resource.getRole().setValue(3);

        strategy = new UpdateUserStrategy(
                resource,
                eventService,
                service,
                requester,
                updatedResource,
                packageService,
                bestMaintainerChooser,
                repositoryMaintainerService,
                packageMaintainerService);

        assertThrows(StrategyFailure.class, () -> strategy.perform());
    }

    @Test
    public void updateUser_deactivateMaintainer_shouldPassAlways() throws Exception {
        resource.getRole().setId(newId);
        resource.setActive(true);
        updatedResource.setActive(false);
        resource.getRole().setValue(2);

        strategy = new UpdateUserStrategy(
                resource,
                eventService,
                service,
                requester,
                updatedResource,
                packageService,
                bestMaintainerChooser,
                repositoryMaintainerService,
                packageMaintainerService);

        strategy.perform();
        Assertions.assertEquals(resource.isActive(), updatedResource.isActive());
    }

    @Test
    public void updateUser_activateUser_shouldPassAlways() throws Exception {
        resource.getRole().setId(newId);
        resource.setActive(false);
        updatedResource.setActive(true);
        resource.getRole().setValue(2);

        strategy = new UpdateUserStrategy(
                resource,
                eventService,
                service,
                requester,
                updatedResource,
                packageService,
                bestMaintainerChooser,
                repositoryMaintainerService,
                packageMaintainerService);

        strategy.perform();
        Assertions.assertEquals(resource.isActive(), updatedResource.isActive());
    }
}
