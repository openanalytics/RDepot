/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.api.v2.controller;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.api.v2.dto.EntityDto;
import eu.openanalytics.rdepot.api.v2.dto.ResponseDto;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.IEntity;
import jakarta.json.Json;
import jakarta.json.JsonPatch;
import jakarta.json.JsonStructure;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

/**
 * This abstract class defines the most common functionality of RDepot controllers.
 *
 * @param <E> Entity model class, used for communication with the database, must implement {@link IEntity} class.
 * @param <D> DTO model class, used for communication via Rest API, must extend {@link EntityDto} class.
 */
public abstract class ApiV2Controller<E extends IEntity<E>, D extends EntityDto<E>> {
	
	protected MessageSource messageSource;
	protected Locale locale;
	protected Logger logger;
	protected RepresentationModelAssembler<E, EntityModel<D>> modelAssembler;
	protected PagedResourcesAssembler<E> pagedModelAssembler;
	protected ObjectMapper objectMapper;
	private Validator validator;
	private Class<D> dtoParametrizedType;
	
	/**
	 * 
	 * @param messageSource Source of success/error messages.
	 * @param locale Current locale settings
	 * @param modelAssembler used to build a DTO object from Entity.
	 * @param pagedModelAssembler used to build a collection of DTOs with support for pagination.
	 * @param objectMapper bean used to serialize/deserialize JSON values.
	 * @param dtoParametrizedType DTO class, must extend {@link EntityDto} class.
	 * @param logger
	 * @param validator default entity validator
	 */
	public ApiV2Controller(MessageSource messageSource, Locale locale,
			RepresentationModelAssembler<E, EntityModel<D>> modelAssembler,
			PagedResourcesAssembler<E> pagedModelAssembler, ObjectMapper objectMapper,
			Class<D> dtoParametrizedType, Logger logger, Validator validator) {
		this.messageSource = messageSource;
		this.locale = locale;
		this.modelAssembler = modelAssembler;
		this.pagedModelAssembler = pagedModelAssembler;
		this.objectMapper = objectMapper;
		this.dtoParametrizedType = dtoParametrizedType;
		this.logger = logger;
		this.validator = validator;
	}
	
	/**
	 * This method builds a default success response for a single entity.
	 * For collection see {@link ApiV2Controller#handleSuccessForPagedCollection(Page)} method.
	 * @param data entity to return
	 * @return full response DTO with 200 http status code
	 */
	protected @ResponseBody ResponseEntity<ResponseDto<EntityModel<D>>> handleSuccessForSingleEntity(E data) {
		EntityModel<D> model = modelAssembler.toModel(data);
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(ResponseDto.generateSuccessBody(messageSource, locale, model));
	}
	
	/**
	 * This method builds a default created response for a single entity.
	 * @param data entity to return
	 * @return full response DTO with 201 http status code
	 */
	protected @ResponseBody ResponseEntity<ResponseDto<EntityModel<D>>> handleCreatedForSingleEntity(E data) {
		EntityModel<D> model = modelAssembler.toModel(data);
		
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ResponseDto.generateCreatedBody(messageSource, locale, model));
	}
	
	/**
	 * This method builds a default success response for a paginated collection.
	 * @param items wrapped with {@link Page}
	 * @return full response DTO with 200 http status code.
	 */
	protected @ResponseBody ResponseDto<PagedModel<EntityModel<D>>> handleSuccessForPagedCollection(
			Page<E> items) {
		PagedModel<EntityModel<D>> pagedItems = pagedModelAssembler.toModel(items, modelAssembler);
		
		return ResponseDto.generateSuccessBody(messageSource, locale, pagedItems);
	}
	
	/**
	 * This method processes and returns validation error.
	 * @param messageCode
	 * @return
	 */
	protected @ResponseBody ResponseEntity<ResponseDto<String>> handleValidationError(String messageCode) {
		return ResponseEntity
				.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(ResponseDto.generateErrorBody(messageSource, locale,
						HttpStatus.UNPROCESSABLE_ENTITY, 
						MessageCodes.ERROR_VALIDATION,
						messageSource.getMessage(messageCode, null, messageCode, locale)));
	}

	/**
	 * This method processes and returns validation error.
	 * All error messages can be retrieved with {@link ResponseDto#getData()}.
	 * @param bindingResult
	 * @return ResponseDto with error related information.
	 */
	protected @ResponseBody ResponseEntity<ResponseDto<List<String>>> handleValidationError(BindingResult bindingResult) {
		List<String> errorMessages = bindingResult.getAllErrors().stream().map(err -> { 
			return messageSource.getMessage(
					err.getCode(), null, err.getCode(), locale); })
				.collect(Collectors.toList());
		
		return ResponseEntity
				.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(ResponseDto.generateErrorBody(messageSource, locale,
						HttpStatus.UNPROCESSABLE_ENTITY, 
						MessageCodes.ERROR_VALIDATION,
						errorMessages));
	}
	
	/**
	 * This method processes and returns validation error for exceptions.
	 * All error messages can be retrieved with {@link ResponseDto#getData()}.
	 * @param exception
	 * @return ResponseDto with error related information.
	 */
	protected @ResponseBody ResponseEntity<ResponseDto<List<String>>> handleValidationError(Exception e) {
		List<String> errorMessage = new ArrayList<>();
		errorMessage.add(messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale));
		
		return ResponseEntity
				.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(ResponseDto.generateErrorBody(messageSource, locale,
						HttpStatus.UNPROCESSABLE_ENTITY, 
						MessageCodes.ERROR_VALIDATION,
						errorMessage));
	}
	
	/**
	 * This method applies {@link JsonPatch} to an entity.<br>
	 * It returns an updated DTO object with out-of-date entity included.
	 * You must update an entity on your own using a dedicated service method.
	 * 
	 * @param patch to apply
	 * @param entity to update
	 * @return updated DTO object with out-of-date entity
	 * @throws JsonProcessingException generally indicates that there was some internal error e.g. associated to data integrity.
	 */
	protected D applyPatchToEntity(JsonPatch patch, E entity) throws JsonProcessingException {
		//Create DTO from entity
		EntityDto<E> dto = entity.createDto();
		
		// Convert DTO to JSON
		

		String dtoJsonStr = objectMapper.writeValueAsString(dto);
		StringReader reader = new StringReader(dtoJsonStr);
		JsonStructure target = Json.createReader(reader).read();
		
		//Apply patch
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
		D patchedDto = objectMapper.readValue(patchedJsonStr, dtoParametrizedType);
		
		patchedDto.setEntity(entity);
		
		return patchedDto;
	}
	
	/**
	 * Returns default {@link BindingResult} for a given entity.
	 * @param target entity to validate
	 * @return BindingResult object
	 */
	protected BindingResult createBindingResult(Object target) {
		final DataBinder dataBinder = new DataBinder(target);
		
		dataBinder.setValidator(validator);
		
		return dataBinder.getBindingResult();
	}
}