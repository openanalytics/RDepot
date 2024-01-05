/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.integrationtest.manager.v2;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.specification.MultiPartSpecification;

public class SubmissionMultipartBody {
	private String repository;
	private Boolean generateManual;
	private Boolean replace;
	private MultiPartSpecification multipartFile;
	
	public SubmissionMultipartBody(String repository, Boolean generateManual, 
				Boolean replace, MultiPartSpecification multipartFile) {
		this.repository = repository;
		this.generateManual = generateManual;
		this.replace = replace;
		this.multipartFile = multipartFile;
	}
	
	public SubmissionMultipartBody(String repository, MultiPartSpecification multipartFile) {
		this.repository = repository;
		this.multipartFile = multipartFile;
	}

	public String getRepository() {
		return repository;
	}

	public Boolean getGenerateManual() {
		return generateManual;
	}

	public Boolean getReplace() {
		return replace;
	}

	public MultiPartSpecification getMultipartFile() {
		return multipartFile;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public void setGenerateManual(Boolean generateManual) {
		this.generateManual = generateManual;
	}

	public void setReplace(Boolean replace) {
		this.replace = replace;
	}

	public void setMultipartFile(MultiPartSpecification multipartFile) {
		this.multipartFile = multipartFile;
	}
	
}