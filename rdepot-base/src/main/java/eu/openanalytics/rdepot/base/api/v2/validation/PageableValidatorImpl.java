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
package eu.openanalytics.rdepot.base.api.v2.validation;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UnrecognizedQueryParameterException;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Component
@Slf4j
public class PageableValidatorImpl implements PageableValidator {

	protected final MessageSource messageSource;
	protected static final Locale locale = LocaleContextHolder.getLocale();

	@Override
	public void validate(Class<? extends IDto> c, DtoResolvedPageable pageable)
			throws UnrecognizedQueryParameterException {

		if(pageable.getDtoSort().isUnsorted())
			return;

		Set<String> fieldsToSortBy= pageable.getDtoSort().toSet()
				.stream()
				.map(field -> field.toString().substring(0, field.toString().indexOf(":")))
				.collect(Collectors.toSet())
				;
		
		Set<String> dtoFields = getDtoFields(c);
		
		for(String field : fieldsToSortBy) {
			if(validateField(field, dtoFields))
				continue;
			throw new UnrecognizedQueryParameterException(Arrays.asList(field), messageSource, locale);
		}
	}
	
	private boolean validateField(String field, Set<String> dtoFields) {
		return dtoFields.contains(field);
	}
	
	private Set<String> getDtoFields(Class<? extends IDto> c){
		Set<String> dtoFields = Arrays.asList(c.getDeclaredFields())
				.stream()
				.map(f -> f.getName())
				.collect(Collectors.toSet())
				;

		if(!c.getSuperclass().equals(Object.class)){
			dtoFields.addAll(
				Arrays.asList(c.getSuperclass().getDeclaredFields())
				.stream()
				.map(f -> f.getName())
				.collect(Collectors.toSet()));
		}
		
		Set<String> projetionFields = Arrays.asList(
				c.getSuperclass().equals(Object.class)
					? c.getDeclaredFields()
							: c.getSuperclass().getDeclaredFields())
				.stream()
				.filter(f -> f.getType().toString().contains("Projection"))
				.map(f -> f.getName())
				.collect(Collectors.toSet())
				;
		
		if(!projetionFields.isEmpty()) {
			for(String nameOfProjectionField : projetionFields) {

				Field projectionField = null;
				try {
					projectionField = c.getSuperclass().equals(Object.class)
							?  c.getDeclaredField(nameOfProjectionField)
								: c.getSuperclass().getDeclaredField(nameOfProjectionField);
				} catch (NoSuchFieldException | SecurityException e) {
					log.error(e.getMessage(), e);
					throw new IllegalStateException("Reflection error");
				}

				dtoFields.addAll(
						Arrays.asList(projectionField.getType().getDeclaredFields())
						.stream()
						.map(f -> nameOfProjectionField
								+ '.'
								+ f.getName())
						.collect(Collectors.toSet())
						);
			}
		}
		
		if(c.equals(SubmissionDto.class)) {
			ParameterizedType parameterizedType = (ParameterizedType) c.getGenericSuperclass();
			Class genericClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
			
			dtoFields.addAll(
					Arrays.asList(genericClass.getDeclaredFields())
					.stream()
					.map(f -> "packageBag."
							+ f.getName())
					.collect(Collectors.toSet()));
			
			dtoFields.addAll(
					Arrays.asList(genericClass.getSuperclass().getDeclaredFields())
					.stream()
					.map(f -> "packageBag."
							+ f.getName())
					.collect(Collectors.toSet()));
			
			Set<String> projectionFieldsOfGenericClass = 
					Arrays.asList(genericClass.getSuperclass().getDeclaredFields())
					.stream()
					.filter(f -> f.getType().toString().contains("Projection"))
					.map(f -> f.getName())
					.collect(Collectors.toSet())
					;
			
			for(String nameOfProjectionField : projectionFieldsOfGenericClass) {

				Field projectionField = null;
				try {
					projectionField = genericClass
							.getSuperclass().getDeclaredField(nameOfProjectionField);
				} catch (NoSuchFieldException | SecurityException e) {
					log.error(e.getMessage(), e);
					throw new IllegalStateException("Reflection error");
				}

				dtoFields.addAll(
						Arrays.asList(projectionField.getType().getDeclaredFields())
						.stream()
						.map(f -> "packageBag."
								+ nameOfProjectionField
								+ '.'
								+ f.getName())
						.collect(Collectors.toSet())
						);
			}
		}
		
		return dtoFields;
	}
}
