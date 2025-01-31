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
package eu.openanalytics.rdepot.base.api.v2.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.hateoas.RoleAwareRepresentationModelAssembler;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.validation.ValidationResult;
import jakarta.json.Json;
import jakarta.json.JsonPatch;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.MessageSource;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This abstract class defines the most common functionality of RDepot controllers.
 *
 * @param <E> Entity model class, used for communication with the database, must implement {@link E Entity} class.
 * @param <D> DTO model class, used for communication via Rest API, must extend {@link IDto} class.
 */
public abstract class ApiV2Controller<E extends Resource, D extends IDto> extends ApiV2ReadingController<E, D> {

    protected final ObjectMapper objectMapper;
    private final Class<D> dtoParametrizedType;
    private final Optional<Validator> validator;
    protected final DtoConverter<E, D> dtoConverter;

    protected ApiV2Controller(
            MessageSource messageSource,
            Locale locale,
            RoleAwareRepresentationModelAssembler<E, EntityModel<D>> modelAssembler,
            PagedResourcesAssembler<E> pagedModelAssembler,
            ObjectMapper objectMapper,
            Class<D> dtoParametrizedType,
            Optional<Validator> validator,
            DtoConverter<E, D> dtoConverter) {
        super(messageSource, locale, modelAssembler, pagedModelAssembler);
        this.objectMapper = objectMapper;
        this.dtoParametrizedType = dtoParametrizedType;
        this.validator = validator;
        this.dtoConverter = dtoConverter;
    }

    /**
     * Builds a default created response for a single entity.
     * @param data entity to return
     * @return full response DTO with 201 http status code
     */
    protected @ResponseBody ResponseEntity<ResponseDto<EntityModel<D>>> handleCreatedForSingleEntity(E data) {
        EntityModel<D> model = modelAssembler.toModel(data);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.generateCreatedBody(messageSource, locale, model));
    }

    /**
     * Builds a default created response for a single entity
     * considering given user's permissions.
     * @param data entity to return
     * @return full response DTO with 201 http status code
     */
    protected @ResponseBody ResponseEntity<ResponseDto<EntityModel<D>>> handleCreatedForSingleEntity(
            E data, User user) {
        EntityModel<D> model = modelAssembler.toModel(data, user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.generateCreatedBody(messageSource, locale, model));
    }

    /**
     * Builds a default warning response for a single entity
     * considering given user's permissions.
     * @param data entity to return
     * @param messageCode warning message code
     * @param user the requester
     * @return full response DTO with 200 http status code
     */
    protected @ResponseBody ResponseEntity<ResponseDto<EntityModel<D>>> handleWarningForSingleEntity(
            E data, String messageCode, User user, boolean created) {
        EntityModel<D> model = modelAssembler.toModel(data, user);
        final HttpStatus httpStatus = created ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(httpStatus)
                .body(ResponseDto.generateWarningBody(messageSource, locale, httpStatus, messageCode, model));
    }

    /**
     * Processes and returns validation error.
     */
    protected @ResponseBody ResponseEntity<ResponseDto<String>> handleValidationError(String messageCode) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ResponseDto.generateErrorBody(
                        messageSource,
                        locale,
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        MessageCodes.ERROR_VALIDATION,
                        messageSource.getMessage(messageCode, null, messageCode, locale)));
    }

    /**
     * Processes and returns validation error.
     * All error messages can be retrieved with {@link ResponseDto#getData()}.
     * @return ResponseDto with error related information.
     */
    protected @ResponseBody ResponseEntity<ResponseDto<List<String>>> handleValidationError(
            BindingResult bindingResult) {
        final List<String> errorMessages = bindingResult.getAllErrors().stream()
                .map(err ->
                        messageSource.getMessage(Objects.requireNonNull(err.getCode()), null, err.getCode(), locale))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ResponseDto.generateErrorBody(
                        messageSource,
                        locale,
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        MessageCodes.ERROR_VALIDATION,
                        errorMessages));
    }

    /**
     * Processes and returns validation error.
     * All error messages can be retrieved with {@link ResponseDto#getData()}.
     * @return ResponseDto with error related information.
     */
    protected @ResponseBody ResponseEntity<ResponseDto<List<String>>> handleValidationError(
            ValidationResult validationResult) {
        final List<String> errorMessages = validationResult.getErrors().stream()
                .map(err -> messageSource.getMessage(
                        Objects.requireNonNull(err.messageCode()), null, err.messageCode(), locale))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ResponseDto.generateErrorBody(
                        messageSource,
                        locale,
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        MessageCodes.ERROR_VALIDATION,
                        errorMessages));
    }

    /**
     * Processes and returns validation error.
     * All error messages can be retrieved with {@link ResponseDto#getData()}.
     * @return ResponseDto with error related information.
     */
    protected @ResponseBody ResponseEntity<ResponseDto<List<String>>> handleValidationErrorWithDefaultMessage(
            BindingResult bindingResult) {
        final List<String> errorMessages = bindingResult.getAllErrors().stream()
                .map(err -> {
                    return messageSource.getMessage(
                            Objects.requireNonNull(err.getCode()), null, err.getDefaultMessage(), locale);
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ResponseDto.generateErrorBody(
                        messageSource,
                        locale,
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        MessageCodes.ERROR_VALIDATION,
                        errorMessages));
    }

    /**
     * Processes and returns validation error for exceptions.
     * All error messages can be retrieved with {@link ResponseDto#getData()}.
     * @return ResponseDto with error related information.
     */
    protected @ResponseBody ResponseEntity<ResponseDto<List<String>>> handleValidationError(Exception e) {
        final List<String> errorMessage = new ArrayList<>();
        errorMessage.add(e.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ResponseDto.generateErrorBody(
                        messageSource,
                        locale,
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        MessageCodes.ERROR_VALIDATION,
                        errorMessage));
    }

    /**
     * Applies {@link JsonPatch} to an entity.<br>
     * It returns an updated DTO object with out-of-date entity included.
     * You must update an entity on your own using a dedicated service method.
     *
     * @param patch to apply
     * @param entity to update
     * @return updated DTO object
     * @throws JsonProcessingException generally indicates that there was some internal error e.g. associated to data integrity.
     */
    protected D applyPatchToEntity(JsonPatch patch, E entity) throws JsonProcessingException {
        // Create DTO from entity
        IDto dto = dtoConverter.convertEntityToDto(entity);

        // Convert DTO to JSON
        String dtoJsonStr = objectMapper.writeValueAsString(dto);
        StringReader reader = new StringReader(dtoJsonStr);
        JsonStructure target;
        try (JsonReader r = Json.createReader(reader)) {
            target = r.read();
        }

        // Apply patch
        JsonStructure patched = patch.apply(target);

        // Convert patched JSON back to DTO
        Map<String, Object> properties = new HashMap<>();
        properties.put(JsonGenerator.PRETTY_PRINTING, false);
        StringWriter writer = new StringWriter();

        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        JsonWriter jsonWriter = writerFactory.createWriter(writer);

        jsonWriter.writeObject(patched.asJsonObject());
        jsonWriter.close();

        String patchedJsonStr = writer.toString();

        return objectMapper.readValue(patchedJsonStr, dtoParametrizedType);
    }

    /**
     * Returns default {@link BindingResult} for a given entity.
     * @param target entity to validate
     * @return BindingResult object
     */
    protected BindingResult createBindingResult(Object target) {
        final DataBinder dataBinder = new DataBinder(target);

        validator.ifPresent(dataBinder::setValidator);

        return dataBinder.getBindingResult();
    }
}
