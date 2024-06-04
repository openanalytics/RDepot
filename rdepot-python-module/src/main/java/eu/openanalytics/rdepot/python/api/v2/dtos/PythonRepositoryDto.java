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
package eu.openanalytics.rdepot.python.api.v2.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryDto;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.entities.enums.HashMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringExclude;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PythonRepositoryDto extends RepositoryDto {

    private HashMethod hashMethod;

    @ToStringExclude
    @JsonIgnore
    private PythonRepository pythonRepository;

    public PythonRepositoryDto(PythonRepository repository, int numberOfPackages) {
        super(repository, numberOfPackages);
        this.hashMethod = repository.getHashMethod();
        this.pythonRepository = repository;
    }

    @Override
    public PythonRepository getEntity() {
        return pythonRepository;
    }

    public PythonRepository toEntity() {
        return new PythonRepository(this);
    }
}
