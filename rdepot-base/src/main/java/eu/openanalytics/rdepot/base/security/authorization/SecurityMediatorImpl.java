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
package eu.openanalytics.rdepot.base.security.authorization;

import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.api.v2.permissions.Action;
import eu.openanalytics.rdepot.base.api.v2.permissions.Permissions;
import eu.openanalytics.rdepot.base.api.v2.permissions.UserPermissions;
import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.PackageMaintainerQueryDSLTuple;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import jakarta.json.JsonPatch;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityMediatorImpl implements SecurityMediator {

    private final RepositoryMaintainerService repositoryMaintainerService;
    private final PackageMaintainerService packageMaintainerService;
    protected final UserService userService;
    private final Environment env;
    private final RoleService roleService;
    private final BestMaintainerChooser bestMaintainerChooser;
    private final boolean declarative;
    private final boolean deletingPackagesEnabled;
    private final boolean deletingRepositoriesEnabled;

    public SecurityMediatorImpl(
            RepositoryMaintainerService repositoryMaintainerService,
            PackageMaintainerService packageMaintainerService,
            UserService userService,
            Environment env,
            RoleService roleService,
            BestMaintainerChooser bestMaintainerChooser,
            @Value("${declarative}") String declarative,
            @Value("${deleting.packages.enabled}") String deletingPackagesEnabled,
            @Value("${deleting.repositories.enabled}") String deletingRepositoriesEnabled) {
        this.repositoryMaintainerService = repositoryMaintainerService;
        this.packageMaintainerService = packageMaintainerService;
        this.userService = userService;
        this.env = env;
        this.roleService = roleService;
        this.bestMaintainerChooser = bestMaintainerChooser;
        this.declarative = Boolean.parseBoolean(declarative);
        this.deletingPackagesEnabled = Boolean.parseBoolean(deletingPackagesEnabled);
        this.deletingRepositoriesEnabled = Boolean.parseBoolean(deletingRepositoriesEnabled);
    }

    @Override
    public boolean isAuthorizedToAccept(Submission submission, User user) {
        return isAuthorizedToEdit(submission.getPackage(), user);
    }

    @Override
    public boolean isAuthorizedToCancel(Submission submission, User user) {
        return submission.getSubmitter().getId() == user.getId();
    }

    @Override
    public boolean canSeeEvent(NewsfeedEvent event, User user) {
        if (event.getRelatedResource().getResourceType().equals(ResourceType.USER)) {
            return user.getRole().getValue() != Role.VALUE.USER
                    || user.getId() == event.getRelatedResource().getId();
        }
        return true;
    }

    @Override
    public boolean isAuthorizedToSee(PackageMaintainer packageMaintainer, User user) {
        if (isAuthorizedToEdit(packageMaintainer, user)) {
            return true;
        }
        return packageMaintainer.getUser().getId() == user.getId()
                && Objects.equals(packageMaintainer.getDeleted(), false);
    }

    @Override
    public boolean isAuthorizedToEdit(PackageMaintainer packageMaintainer, User requester) {
        return isAuthorizedToEdit(packageMaintainer.getRepository(), requester);
    }

    @Override
    public boolean isAuthorizedToEdit(PackageMaintainerQueryDSLTuple packageMaintainer, User requester) {
        switch (requester.getRole().getValue()) {
            case Role.VALUE.ADMIN:
                return true;
            case Role.VALUE.REPOSITORYMAINTAINER:
                for (RepositoryMaintainer maintainer : repositoryMaintainerService.findByUserIdAndRepositoryId(
                        requester.getId(), packageMaintainer.getRepositoryId())) {
                    if (!maintainer.isDeleted()) return true;
                }
            default:
                return false;
        }
    }

    @Override
    public boolean isAuthorizedToEdit(Submission submission, SubmissionDto submissionDto, User requester) {
        if (!submission.getState().equals(submissionDto.getState())) {
            if (submissionDto.getState().equals(SubmissionState.ACCEPTED)
                    && isAuthorizedToAccept(submission, requester)) {
                return true;
            } else if (submissionDto.getState().equals(SubmissionState.CANCELLED)
                    && isAuthorizedToCancel(submission, requester)) {
                return true;
            } else
                return submissionDto.getState().equals(SubmissionState.REJECTED)
                        && isAuthorizedToReject(submission, requester);
        } else {
            return isAuthorizedToEdit(submission.getPackage(), requester);
        }
    }

    @Override
    public boolean isAuthorizedToEdit(Package packageBag, User requester) {
        if (packageBag.getUser().getId() == requester.getId()) {
            return true;
        }
        switch (requester.getRole().getValue()) {
            case Role.VALUE.ADMIN:
                return true;
            case Role.VALUE.REPOSITORYMAINTAINER:
                for (RepositoryMaintainer maintainer :
                        repositoryMaintainerService.findByUserWithoutDeleted(requester)) {
                    if (maintainer.getRepository().getId()
                                    == packageBag.getRepository().getId()
                            && maintainer.getRepository().getTechnology()
                                    == packageBag.getRepository().getTechnology()) {
                        return true;
                    }
                }
            case Role.VALUE.PACKAGEMAINTAINER:
                for (PackageMaintainer maintainer : packageMaintainerService.findNonDeletedByUser(requester)) {
                    if (maintainer.getRepository().getId()
                                    == packageBag.getRepository().getId()
                            && maintainer.getRepository().getTechnology()
                                    == packageBag.getRepository().getTechnology()
                            && maintainer.getPackageName().equals(packageBag.getName())) {
                        return true;
                    }
                }
            default:
                return false;
        }
    }

    @Override
    public boolean isAuthorizedToEdit(Repository repository, User requester) {
        switch (requester.getRole().getValue()) {
            case Role.VALUE.ADMIN:
                return true;
            case Role.VALUE.REPOSITORYMAINTAINER:
                for (RepositoryMaintainer maintainer :
                        repositoryMaintainerService.findByUserAndRepository(requester, repository)) {
                    if (!maintainer.isDeleted()) return true;
                }
            default:
                return false;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(String userLogin) {
        Collection<SimpleGrantedAuthority> authorities;
        if (Objects.equals(env.getProperty("app.authentication"), "simple")) {
            authorities = new ArrayList<>(0);
        } else {
            authorities = new HashSet<>(0);
        }

        Optional<User> user = userService.findActiveByLogin(userLogin);
        if (user.isPresent()) {
            for (int i = 0, v = user.get().getRole().getValue(); i <= v; i++) {
                authorities.add(new SimpleGrantedAuthority(roleService
                        .findByValue(i)
                        .orElseThrow(IllegalStateException::new)
                        .getName()));
            }
        } else {
            log.error("{} has been deactivated or deleted", userLogin);
        }

        return authorities;
    }

    @Override
    public boolean canUpload(String packageName, Repository repository, User user) {
        if (user.getRole().getValue() == Role.VALUE.ADMIN) {
            return true;
        } else if (user.getRole().getValue() == Role.VALUE.REPOSITORYMAINTAINER) {
            List<RepositoryMaintainer> maintainers =
                    repositoryMaintainerService.findByUserAndRepository(user, repository);
            for (RepositoryMaintainer maintainer : maintainers) {
                if (!maintainer.isDeleted()) return true;
            }
        } else if (user.getRole().getValue() == Role.VALUE.PACKAGEMAINTAINER) {
            List<PackageMaintainer> maintainers = packageMaintainerService.findByUser(user);

            for (PackageMaintainer maintainer : maintainers) {
                if (maintainer.getPackageName().equals(packageName)
                        && maintainer.getRepository().getId() == repository.getId()
                        && maintainer.getRepository().getTechnology() == repository.getTechnology()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAuthorizedToReject(Submission submission, User user) {
        if (submission.getSubmitter().getId() == user.getId()) return false;
        return isAuthorizedToEdit(submission.getPackage(), user);
    }

    @Override
    public boolean isAuthorizedToEditWithPatch(JsonPatch patch, User user, User requester) {
        return requester.getRole().getValue() == Role.VALUE.ADMIN;
    }

    @Override
    public <T extends Resource> boolean canSeeDeleted(User user, Class<T> resourceType) {
        return userService.isAdmin(user);
    }

    @Override
    public List<String> getPermissions(AccessToken token, User user) {
        List<String> permissions = new ArrayList<>();
        if (token.getUser().getId() == user.getId()) {
            if (token.isActive()) {
                permissions.add(Permissions.getPermission(ResourceType.ACCESS_TOKEN, Action.DEACTIVATE));
                permissions.add(Permissions.getPermission(ResourceType.ACCESS_TOKEN, Action.EDIT));
            }
            permissions.add(Permissions.getPermission(ResourceType.ACCESS_TOKEN, Action.DELETE_HARD));
        } else {
            permissions.add(Permissions.getPermission(ResourceType.ACCESS_TOKEN, Action.EDIT));
        }

        permissions.sort(Comparator.naturalOrder());
        return permissions;
    }

    @Override
    public List<String> getPermissions(Package packageBag, User user) {
        List<String> permissions = new ArrayList<>();

        if (isAuthorizedToEdit(packageBag, user) && !packageBag.isDeleted()) {
            permissions = permissionsForAuthorizedUser(packageBag);
        } else if (userService.isAdmin(user) && deletingPackagesEnabled && packageBag.isDeleted()) {
            permissions.add(Permissions.getPermission(ResourceType.PACKAGE, Action.DELETE_HARD));
        }

        permissions.sort(Comparator.naturalOrder());
        return permissions;
    }

    @Override
    public List<String> getPermissions(PackageMaintainer maintainer, User user) {
        List<String> permissions = new ArrayList<>();

        if (isAuthorizedToEdit(maintainer, user) && !maintainer.isDeleted()) {
            permissions.add(Permissions.getPermission(ResourceType.PACKAGE_MAINTAINER, Action.DELETE_SOFT));
            permissions.add(Permissions.getPermission(ResourceType.PACKAGE_MAINTAINER, Action.EDIT));
        } else if (userService.isAdmin(user) && maintainer.isDeleted()) {
            permissions.add(Permissions.getPermission(ResourceType.PACKAGE_MAINTAINER, Action.DELETE_HARD));
        }
        return permissions;
    }

    @Override
    public List<String> getPermissions(PackageMaintainerQueryDSLTuple maintainer, User user) {
        List<String> permissions = new ArrayList<>();

        if (maintainer.getId() == 0) return permissions;

        if (isAuthorizedToEdit(maintainer, user) && !maintainer.isDeleted()) {
            permissions.add(Permissions.getPermission(ResourceType.PACKAGE_MAINTAINER, Action.DELETE_SOFT));
            permissions.add(Permissions.getPermission(ResourceType.PACKAGE_MAINTAINER, Action.EDIT));
        } else if (userService.isAdmin(user) && maintainer.isDeleted()) {
            permissions.add(Permissions.getPermission(ResourceType.PACKAGE_MAINTAINER, Action.DELETE_HARD));
        }

        return permissions;
    }

    @Override
    public List<String> getPermissions(Repository repository, User user) {
        List<String> permissions = new ArrayList<>();

        if (isAuthorizedToEdit(repository, user) && !repository.isDeleted()) {
            permissions = permissionsForAuthorizedUser(repository);
        } else if (userService.isAdmin(user) && !declarative && deletingRepositoriesEnabled && repository.isDeleted()) {
            permissions.add(Permissions.getPermission(ResourceType.REPOSITORY, Action.DELETE_HARD));
        }

        permissions.sort(Comparator.naturalOrder());
        return permissions;
    }

    @Override
    public List<String> getPermissions(RepositoryMaintainer maintainer, User user) {
        List<String> permissions = new ArrayList<>();

        if (maintainer.isDeleted()) {
            permissions.add(Permissions.getPermission(ResourceType.REPOSITORY_MAINTAINER, Action.DELETE_HARD));
        } else {
            permissions.add(Permissions.getPermission(ResourceType.REPOSITORY_MAINTAINER, Action.DELETE_SOFT));
            permissions.add(Permissions.getPermission(ResourceType.REPOSITORY_MAINTAINER, Action.EDIT));
        }

        return permissions;
    }

    @Override
    public List<String> getPermissions(Submission submission, User user) {
        List<String> permissions = new ArrayList<>();

        if (userService.isAdmin(user) && submission.isDeleted()) {
            permissions.add(Permissions.getPermission(ResourceType.SUBMISSION, Action.DELETE_HARD));
        } else {
            permissions = permissionsForAuthorizedUser(submission, user);
        }
        return permissions;
    }

    @Override
    public List<String> getPermissions(User entity, User user) {
        List<String> permissions = new ArrayList<>();

        if (userService.isAdmin(user)) {
            permissions = permissionsForAuthorizedUser(entity, user);
        } else if (!entity.isDeleted()) {
            permissions.add(Permissions.getPermission(ResourceType.USER, Action.EDIT));
        }
        return permissions;
    }

    @Override
    public List<String> getPermissions(User user) {
        List<String> permissions;
        if (userService.isAdmin(user)) {
            permissions = UserPermissions.getAdminPermissions();
        } else if (userService.isRepositoryMaintainer(user)) {
            permissions = UserPermissions.getRepositoryMaintainerPermissions();
        } else {
            permissions = UserPermissions.getRegularUserPermissions();
        }
        return permissions;
    }

    private List<String> permissionsForAuthorizedUser(Package entity) {
        List<String> permissions = new ArrayList<>();

        if (entity.isActive()) {
            permissions.add(Permissions.getPermission(ResourceType.PACKAGE, Action.DEACTIVATE));
        } else {
            permissions.add(Permissions.getPermission(ResourceType.PACKAGE, Action.ACTIVATE));
        }

        if (deletingPackagesEnabled) {
            permissions.add(Permissions.getPermission(ResourceType.PACKAGE, Action.DELETE_SOFT));
        }

        permissions.add(Permissions.getPermission(ResourceType.PACKAGE, Action.EDIT));

        return permissions;
    }

    private List<String> permissionsForAuthorizedUser(Repository entity) {
        List<String> permissions = new ArrayList<>();
        if (!declarative && entity.getPublished()) {
            permissions.add(Permissions.getPermission(ResourceType.REPOSITORY, Action.REPUBLISH));
            permissions.add(Permissions.getPermission(ResourceType.REPOSITORY, Action.UNPUBLISH));
        } else if (!declarative) {
            permissions.add(Permissions.getPermission(ResourceType.REPOSITORY, Action.PUBLISH));
        } else if (entity.getPublished()) {
            permissions.add(Permissions.getPermission(ResourceType.REPOSITORY, Action.REPUBLISH));
        }

        if (deletingRepositoriesEnabled && !declarative) {
            permissions.add(Permissions.getPermission(ResourceType.REPOSITORY, Action.DELETE_SOFT));
        }

        if (!declarative) {
            permissions.add(Permissions.getPermission(ResourceType.REPOSITORY, Action.EDIT));
        }

        permissions.add(Permissions.getPermission(ResourceType.REPOSITORY, Action.SUBMIT_AUTO_APPROVE));
        return permissions;
    }

    private List<String> permissionsForAuthorizedUser(Submission entity, User user) {
        List<String> permissions = new ArrayList<>();

        if (isAuthorizedToAccept(entity, user) && entity.getState() == SubmissionState.WAITING) {
            permissions.add(Permissions.getPermission(ResourceType.SUBMISSION, Action.ACCEPT));
        }

        if (isAuthorizedToCancel(entity, user) && entity.getState() == SubmissionState.WAITING) {
            permissions.add(Permissions.getPermission(ResourceType.SUBMISSION, Action.CANCEL));
        }

        if (isAuthorizedToReject(entity, user) && entity.getState() == SubmissionState.WAITING) {
            permissions.add(Permissions.getPermission(ResourceType.SUBMISSION, Action.REJECT));
        }

        return permissions;
    }

    private List<String> permissionsForAuthorizedUser(User entity, User user) {
        List<String> permissions = new ArrayList<>();
        if (entity.isDeleted()) {
            return permissions;
        }

        final List<User> admins = bestMaintainerChooser.findAllAdmins();
        if (entity.isActive() && !(admins.size() == 1 && admins.get(0).equals(entity))) {
            permissions.add(Permissions.getPermission(ResourceType.USER, Action.DEACTIVATE));
        } else if (!entity.isActive()) {
            permissions.add(Permissions.getPermission(ResourceType.USER, Action.ACTIVATE));
        }

        if (entity.getId() != user.getId()) {
            permissions.add(Permissions.getPermission(ResourceType.USER, Action.DELETE_SOFT));
        }

        permissions.add(Permissions.getPermission(ResourceType.USER, Action.EDIT));

        return permissions;
    }
}
