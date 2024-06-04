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
package eu.openanalytics.rdepot.base.service;

import eu.openanalytics.rdepot.base.api.v2.exceptions.RepositoryMaintainerNotFound;
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
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * {@link SpringDataJpaCapableRetriever} used to access all kinds of {@link NewsfeedEvent NewsfeedEvents}.
 */
@Slf4j
@Service
public class NewsfeedEventService extends eu.openanalytics.rdepot.base.service.Service<NewsfeedEvent> {

    private final NewsfeedEventDao dao;
    private final EventChangedVariableDao eventChangedVariableDao;

    public NewsfeedEventService(
            EventChangedVariableDao eventChangedVariableDao,
            NewsfeedEventDao newsfeedEventDao,
            PackageMaintainerService packageMaintainerService,
            RepositoryMaintainerService repositoryMaintainerService,
            SubmissionService submissionService) {
        super(newsfeedEventDao);
        this.eventChangedVariableDao = eventChangedVariableDao;
        this.dao = newsfeedEventDao;
    }

    /**
     * Convert string to {@link NewsfeedEventType} enum.
     * @param eventType
     * @return enumerator
     * @throws UnknownEventType
     */
    private NewsfeedEventType resolveEventType(String eventType) throws UnknownEventType {
        switch (eventType) {
            case "create":
                return NewsfeedEventType.CREATE;
            case "delete":
                return NewsfeedEventType.DELETE;
            case "update":
                return NewsfeedEventType.UPDATE;
            case "upload":
                return NewsfeedEventType.UPLOAD;
            default:
                throw new UnknownEventType();
        }
    }

    /**
     * Converts string to {@link ResourceType} enum.
     * @param resourceType
     * @return
     * @throws UnknownResourceType
     */
    private ResourceType resolveResourceType(String resourceType) throws UnknownResourceType {
        switch (resourceType) {
            case "package":
                return ResourceType.PACKAGE;
            case "repository":
                return ResourceType.REPOSITORY;
            case "user":
                return ResourceType.USER;
            case "submission":
                return ResourceType.SUBMISSION;
            case "packageMaintainer":
                return ResourceType.PACKAGE_MAINTAINER;
            case "repositoryMaintainer":
                return ResourceType.REPOSITORY_MAINTAINER;
            case "accessToken":
                return ResourceType.ACCESS_TOKEN;
            default:
                throw new UnknownResourceType();
        }
    }

    private Specification<NewsfeedEvent> addComponent(
            Specification<NewsfeedEvent> specification, Specification<NewsfeedEvent> specificationComponent) {
        if (specification == null) specification = Specification.where(specificationComponent);
        else specification = specification.and(specificationComponent);

        return specification;
    }

    /**
     * Finds an event based on supplied parameters.
     * All of them are {@link Optional Optionals}
     * so that they can but do not need to be supplied.
     * @param pageable used for pagination
     * @param isAdmin flag used to determine if the user making a request has administrator rights
     * @param technologyStr
     * @param user
     * @param resourceId
     * @param eventType
     * @param resourceType
     * @return {@link Page} of events.
     * @throws RepositoryMaintainerNotFound
     */
    public Page<NewsfeedEvent> findEventsByParameters(
            Pageable pageable,
            boolean isAdmin,
            List<String> technologies,
            List<String> userNames,
            List<String> eventTypes,
            List<String> resourceTypes,
            Optional<String> fromDate,
            Optional<String> toDate,
            Specification<NewsfeedEvent> specification)
            throws RepositoryMaintainerNotFound {

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
            return new PageImpl<NewsfeedEvent>(List.of()); // There are no events of such type
        }

        try {
            if (Objects.nonNull(resourceTypes)) {
                specification = addComponent(
                        specification, NewsfeedEventSpecs.ofResourceTypes(convertToResourceTypes(resourceTypes)));
            }
        } catch (UnknownResourceType e) {
            log.debug(e.getMessage(), e);
            return new PageImpl<NewsfeedEvent>(List.of());
        }

        if (fromDate.isPresent()) {
            specification = addComponent(specification, NewsfeedEventSpecs.fromDate(fromDate.get()));
        }

        if (toDate.isPresent()) {
            specification = addComponent(specification, NewsfeedEventSpecs.toDate(toDate.get()));
        }

        Pageable pageableWithSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Direction.DESC, "date").and(Sort.by(Direction.DESC, "time"))));

        if (specification == null) return findAll(pageableWithSort);
        else return findAllBySpecification(specification, pageableWithSort);
    }

    public List<NewsfeedEvent> findAllByResource(Resource resource) {
        return dao.findAll(NewsfeedEventSpecs.hasRelatedResource(resource));
    }

    public List<NewsfeedEvent> findAllByRelatedResourceType(ResourceType resourceType) {
        return dao.findAll(NewsfeedEventSpecs.hasResourceOfType(resourceType));
    }

    public void attachVariables(NewsfeedEvent entity, Set<EventChangedVariable> eventChangedVariables) {
        eventChangedVariables.forEach(v -> {
            v.setRelatedNewsfeedEvent(entity);
            eventChangedVariableDao.save(v);
        });
    }

    public List<NewsfeedEvent> findByDateAndResource(LocalDate date, Resource resource) {
        return dao.findAll(SpecificationUtils.andComponent(
                NewsfeedEventSpecs.byDate(date), NewsfeedEventSpecs.hasRelatedResource(resource)));
    }

    public List<NewsfeedEvent> findByDateAndRepository(LocalDate date, Repository repository) {
        return dao.findAll(SpecificationUtils.andComponent(
                NewsfeedEventSpecs.byDate(date), NewsfeedEventSpecs.relatedResourceHasRelatedRepository(repository)));
    }

    public List<NewsfeedEvent> findByRepository(Repository repository) {
        return dao.findAll(NewsfeedEventSpecs.relatedResourceHasRelatedRepository(repository));
    }

    public void deleteRelatedEvents(Resource resource) throws DeleteEntityException {
        for (NewsfeedEvent event : findAllByResource(resource)) {
            delete(event);
        }
    }

    private List<NewsfeedEvent> findAllForPackage(int id) {
        return dao.findAllForPackageWithId(id);
    }

    public void deleteRelatedPackageEvents(int resourceId) throws DeleteEntityException {
        List<NewsfeedEvent> events = findAllForPackage(resourceId);
        for (NewsfeedEvent event : events) {
            delete(event.getId());
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
