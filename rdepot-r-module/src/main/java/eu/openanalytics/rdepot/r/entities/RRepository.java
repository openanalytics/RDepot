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
package eu.openanalytics.rdepot.r.entities;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.r.api.v2.dtos.RRepositoryDto;
import eu.openanalytics.rdepot.r.api.v2.dtos.RRepositorySimpleDto;
import eu.openanalytics.rdepot.r.technology.RLanguage;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import java.io.Serial;
import java.io.Serializable;

@Entity
@DiscriminatorValue("R")
@SecondaryTable(name = "rrepository", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public class RRepository extends Repository implements Serializable {

    @Serial
    private static final long serialVersionUID = 3346101145064616895L;

    public RRepository() {
        super(RLanguage.instance);
    }

    public RRepository(RRepositoryDto dto) {
        super(RLanguage.instance, dto);
    }

    public RRepository(RRepository that) {
        super(that);
    }

    @Override
    public RRepositorySimpleDto createSimpleDto() {
        return new RRepositorySimpleDto(this);
    }
}
