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
package eu.openanalytics.rdepot.base.service;

import eu.openanalytics.rdepot.base.daos.EventChangedVariableDao;
import eu.openanalytics.rdepot.base.daos.NewsfeedEventDao;
import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.service.exceptions.UnknownEventType;
import eu.openanalytics.rdepot.base.service.exceptions.UnknownResourceType;
import eu.openanalytics.rdepot.base.utils.specs.NewsfeedEventSpecs;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * {@link SpringDataJpaCapableRetriever} used to access all kinds of {@link NewsfeedEvent NewsfeedEvents}.
 */
@Slf4j
@Service
public class NewsfeedEventService extends eu.openanalytics.rdepot.base.service.Service<NewsfeedEvent> {

    private final NewsfeedEventDao newsfeedEventDao;
    private final EventChangedVariableDao eventChangedVariableDao;

    public NewsfeedEventService(EventChangedVariableDao eventChangedVariableDao, NewsfeedEventDao newsfeedEventDao) {
        super(newsfeedEventDao);
        this.eventChangedVariableDao = eventChangedVariableDao;
        this.newsfeedEventDao = newsfeedEventDao;
    }

    /**
     * Convert string to {@link NewsfeedEventType} enum.
     * @param eventType
     * @return enumerator
     * @throws UnknownEventType
     */
    private NewsfeedEventType resolveEventType(String eventType) throws UnknownEventType {
        return switch (eventType) {
            case "create" -> NewsfeedEventType.CREATE;
            case "delete" -> NewsfeedEventType.DELETE;
            case "update" -> NewsfeedEventType.UPDATE;
            case "upload" -> NewsfeedEventType.UPLOAD;
            case "republish" -> NewsfeedEventType.REPUBLISH;
            default -> throw new UnknownEventType();
        };
    }

    /**
     * Converts string to {@link ResourceType} enum.
     * @param resourceType
     * @return
     * @throws UnknownResourceType
     */
    private ResourceType resolveResourceType(String resourceType) throws UnknownResourceType {
        return switch (resourceType) {
            case "package" -> ResourceType.PACKAGE;
            case "repository" -> ResourceType.REPOSITORY;
            case "user" -> ResourceType.USER;
            case "submission" -> ResourceType.SUBMISSION;
            case "packageMaintainer" -> ResourceType.PACKAGE_MAINTAINER;
            case "repositoryMaintainer" -> ResourceType.REPOSITORY_MAINTAINER;
            case "accessToken" -> ResourceType.ACCESS_TOKEN;
            default -> throw new UnknownResourceType();
        };
    }

    private Specification<NewsfeedEvent> addComponent(
            Specification<NewsfeedEvent> specification, Specification<NewsfeedEvent> specificationComponent) {
        if (specification == null) specification = Specification.where(specificationComponent);
        else specification = specification.and(specificationComponent);

        return specification;
    }

    private Specification<NewsfeedEvent> orComponent(
            Specification<NewsfeedEvent> specification, Specification<NewsfeedEvent> specificationComponent) {
        if (specification == null) specification = Specification.where(specificationComponent);
        else specification = specification.or(specificationComponent);

        return specification;
    }

    /**
     * Finds an event based on supplied parameters.
     * All of them are {@link Optional Optionals}
     * so that they can but do not need to be supplied.
     * @param pageable used for pagination
     * @param technologies
     * @param userNames
     * @param packageNames
     * @param repositoryNames
     * @param eventTypes
     * @param resourceTypes
     * @param fromDate
     * @param toDate
     * @return {@link Page} of events.
     */
    public Page<NewsfeedEvent> findEventsByParameters(
            Pageable pageable,
            List<String> technologies,
            List<String> userNames,
            List<String> packageNames,
            List<String> packageVersions,
            List<String> repositoryNames,
            List<String> eventTypes,
            List<String> resourceTypes,
            Optional<Instant> fromDate,
            Optional<Instant> toDate,
            Specification<NewsfeedEvent> specification) {

        if (Objects.nonNull(packageNames) && Objects.nonNull(repositoryNames)) {
            if (Objects.nonNull(packageVersions)) {
                specification = addComponent(
                        specification,
                        NewsfeedEventSpecs.byPackageNamesAndVersionsAndRepositoryNames(
                                packageNames, packageVersions, repositoryNames));
                specification = orComponent(
                        specification,
                        NewsfeedEventSpecs.bySubmissionAndPackageNamesAndVersionsAndRepositoryNames(
                                packageNames, packageVersions, repositoryNames));
            } else {
                specification = addComponent(
                        specification,
                        NewsfeedEventSpecs.byPackageNamesAndRepositoryNames(packageNames, repositoryNames));
                specification = orComponent(
                        specification,
                        NewsfeedEventSpecs.bySubmissionAndPackageNamesAndRepositoryNames(
                                packageNames, repositoryNames));
            }
            specification = orComponent(
                    specification,
                    NewsfeedEventSpecs.byPackageMaintainerAndPackageNamesAndRepositoryNames(
                            packageNames, repositoryNames));
        } else if (Objects.nonNull(packageNames)) {
            if (Objects.nonNull(packageVersions)) {
                specification = addComponent(
                        specification, NewsfeedEventSpecs.byPackageNamesAndVersions(packageNames, packageVersions));
                specification = orComponent(
                        specification,
                        NewsfeedEventSpecs.bySubmissionAndPackageNamesAndVersions(packageNames, packageVersions));
            } else {
                specification = addComponent(specification, NewsfeedEventSpecs.byPackageNames(packageNames));
                specification =
                        orComponent(specification, NewsfeedEventSpecs.bySubmissionAndPackageNames(packageNames));
            }
            specification =
                    orComponent(specification, NewsfeedEventSpecs.byPackageMaintainerAndPackageNames(packageNames));
        } else if (Objects.nonNull(repositoryNames)) {
            specification = addComponent(specification, NewsfeedEventSpecs.byRepositoryNames(repositoryNames));
            specification = orComponent(specification, NewsfeedEventSpecs.byPackageAndRepositoryNames(repositoryNames));
            specification = orComponent(
                    specification, NewsfeedEventSpecs.byPackageMaintainerAndRepositoryNames(repositoryNames));
            specification = orComponent(
                    specification, NewsfeedEventSpecs.byRepositoryMaintainerAndRepositoryNames(repositoryNames));
        }

        if (Objects.nonNull(technologies)) {
            specification = addComponent(specification, NewsfeedEventSpecs.byTechnology(technologies));
        }

        if (Objects.nonNull(userNames)) {
            specification = addComponent(specification, NewsfeedEventSpecs.byUserName(userNames));
        }

        try {
            if (Objects.nonNull(eventTypes)) {
                specification =
                        addComponent(specification, NewsfeedEventSpecs.ofType(convertToNewsfeedEventTypes(eventTypes)));
            }
        } catch (UnknownEventType e) {
            log.debug(e.getMessage(), e);
            return new PageImpl<>(List.of()); // There are no events of such type
        }

        try {
            if (Objects.nonNull(resourceTypes)) {
                specification = addComponent(
                        specification, NewsfeedEventSpecs.ofResourceTypes(convertToResourceTypes(resourceTypes)));
            }
        } catch (UnknownResourceType e) {
            log.debug(e.getMessage(), e);
            return new PageImpl<>(List.of());
        }

        if (fromDate.isPresent()) {
            specification = addComponent(specification, NewsfeedEventSpecs.fromDate(fromDate.get()));
        }

        if (toDate.isPresent()) {
            specification = addComponent(specification, NewsfeedEventSpecs.toDate(toDate.get()));
        }

        Pageable pageableWithSort = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSortOr(Sort.by(Direction.DESC, "time")));

        if (specification == null) return findAll(pageableWithSort);
        else return findAllBySpecification(specification, pageableWithSort);
    }

    public List<NewsfeedEvent> findAllByResource(Resource resource) {
        return newsfeedEventDao.findAll(NewsfeedEventSpecs.hasRelatedResource(resource));
    }

    public void attachVariables(NewsfeedEvent entity, Set<EventChangedVariable> eventChangedVariables) {
        eventChangedVariables.forEach(v -> {
            v.setRelatedNewsfeedEvent(entity);
            eventChangedVariableDao.save(v);
        });
    }

    public List<NewsfeedEvent> findByRepository(Repository repository) {
        return newsfeedEventDao.findAll(NewsfeedEventSpecs.relatedResourceHasRelatedRepository(repository));
    }

    public void deleteRelatedEvents(Resource resource) throws DeleteEntityException {
        for (NewsfeedEvent event : findAllByResource(resource)) {
            delete(event);
        }
    }

    private List<ResourceType> convertToResourceTypes(List<String> types) throws UnknownResourceType {
        List<ResourceType> resourceTypes = new ArrayList<>();
        for (String type : types) {
            resourceTypes.add(resolveResourceType(type));
        }
        return resourceTypes;
    }

    private List<NewsfeedEventType> convertToNewsfeedEventTypes(List<String> types) throws UnknownEventType {
        List<NewsfeedEventType> eventTypes = new ArrayList<>();
        for (String type : types) {
            eventTypes.add(resolveEventType(type));
        }
        return eventTypes;
    }
}
