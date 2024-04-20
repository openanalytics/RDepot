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
package eu.openanalytics.rdepot.python.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.python.api.v2.dtos.PythonRepositoryDto;
import eu.openanalytics.rdepot.python.api.v2.dtos.PythonRepositorySimpleDto;
import eu.openanalytics.rdepot.python.entities.enums.HashMethod;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("Python")
@SecondaryTable(name = "pythonrepository", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public class PythonRepository extends Repository implements Serializable {
	
	private static final long serialVersionUID = 3346101145064616895L;
	
	@Column(name = "hash_method", nullable = false, table = "pythonrepository")
	@Enumerated(value = EnumType.STRING)
	private HashMethod hashMethod = HashMethod.SHA256;

	public PythonRepository() {
		super(PythonLanguage.instance);
	}
	
	public PythonRepository(PythonRepositoryDto dto) {
		super(PythonLanguage.instance, dto);
	}
	
	public PythonRepository(PythonRepository that) {
		super(that);
	}

	public PythonRepositorySimpleDto createDto() {
		return new PythonRepositorySimpleDto(this);
	}
}