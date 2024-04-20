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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.base.entities.Repository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Package Upload Request body
 * @param <T> technology-specific Repository class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageUploadRequest<T extends Repository> {
	private MultipartFile fileData;
	private T repository;
	private boolean generateManual = true;
	private boolean replace = false;
}
