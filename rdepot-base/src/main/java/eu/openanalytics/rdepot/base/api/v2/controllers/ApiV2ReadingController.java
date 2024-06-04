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
package eu.openanalytics.rdepot.base.api.v2.controllers;

import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.hateoas.RoleAwareRepresentationModelAssembler;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.User;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

@AllArgsConstructor
public abstract class ApiV2ReadingController<E extends Resource, D extends IDto> {

    protected MessageSource messageSource;
    protected Locale locale;
    protected RoleAwareRepresentationModelAssembler<E, EntityModel<D>> modelAssembler;
    protected PagedResourcesAssembler<E> pagedModelAssembler;

    /**
     * Builds a default success response for a single entity.
     * For collection see {@link ApiV2Controller#handleSuccessForPagedCollection(Page)} method.
     * @param data entity to return
     * @return full response DTO with 200 http status code
     */
    protected @ResponseBody ResponseEntity<ResponseDto<EntityModel<D>>> handleSuccessForSingleEntity(E data) {
        final EntityModel<D> model = modelAssembler.toModel(data);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.generateSuccessBody(messageSource, locale, model));
    }

    /**
     * Builds a default success response for a single entity
     * considering given user's permissions.
     * For collection see {@link ApiV2Controller#handleSuccessForPagedCollection(Page)} method.
     * @param data entity to return
     * @return full response DTO with 200 http status code
     */
    protected @ResponseBody ResponseEntity<ResponseDto<EntityModel<D>>> handleSuccessForSingleEntity(
            E data, User user) {
        final EntityModel<D> model = modelAssembler.toModel(data, user);

        return ResponseEntity.status(HttpStatus.OK).body(ResponseDto.generateSuccessBody(messageSource, locale, model));
    }

    /**
     * Builds a default success response for a paginated collection.
     * @param items wrapped with {@link Page}
     * @return full response DTO with 200 http status code.
     */
    protected @ResponseBody ResponseDto<PagedModel<EntityModel<D>>> handleSuccessForPagedCollection(
            Page<E> items, User user) {
        final PagedModel<EntityModel<D>> pagedItems =
                pagedModelAssembler.toModel(items, modelAssembler.assemblerWithUser(user));

        return ResponseDto.generateSuccessBody(messageSource, locale, pagedItems);
    }

    /**
     * Builds a default success response for a paginated collection.
     * @param items wrapped with {@link Page}
     * @return full response DTO with 200 http status code.
     */
    protected @ResponseBody ResponseDto<PagedModel<EntityModel<D>>> handleSuccessForPagedCollection(Page<E> items) {
        final PagedModel<EntityModel<D>> pagedItems = pagedModelAssembler.toModel(items, modelAssembler);

        return ResponseDto.generateSuccessBody(messageSource, locale, pagedItems);
    }

    /**
     * Builds a success response where there are no elements to return
     * but the request itself was processed successfully.
     * @return full response DTO with 200 http status code.
     */
    protected @ResponseBody ResponseDto<PagedModel<EntityModel<D>>> emptyPage() {
        return handleSuccessForPagedCollection(new PageImpl<>(List.of()));
    }
}
