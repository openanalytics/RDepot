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
package eu.openanalytics.rdepot.base.validation;

import org.springframework.validation.Errors;

import eu.openanalytics.rdepot.base.entities.Resource;
import lombok.Setter;

@Setter
public abstract class ChainValidator<R extends Resource>{
	public ChainValidator<R> next = null;
	
	protected abstract void validateField(R resource, Errors errors);
	
	public void validate(R resource, Errors errors) {
		validateField(resource, errors);
		if(next != null) {
			next.validate(resource, errors);
		}
	}
}
