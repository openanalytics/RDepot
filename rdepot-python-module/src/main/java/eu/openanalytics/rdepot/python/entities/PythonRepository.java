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
package eu.openanalytics.rdepot.python.entities;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.python.api.v2.dtos.PythonRepositoryDto;
import eu.openanalytics.rdepot.python.entities.enums.HashMethod;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("Python")
@SecondaryTable(name = "pythonrepository", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public class PythonRepository extends Repository implements Serializable {

    @Serial
    private static final long serialVersionUID = 3346101145064616895L;

    @Column(name = "hash_method", nullable = false, table = "pythonrepository")
    @Enumerated(value = EnumType.STRING)
    private HashMethod hashMethod = HashMethod.SHA256;

    public PythonRepository() {
        super(PythonLanguage.instance);
    }

    public PythonRepository(PythonRepositoryDto dto, Instant lastPublicationTimestamp, Instant lastModifiedTimestamp) {
        super(PythonLanguage.instance, dto, lastPublicationTimestamp, lastModifiedTimestamp);
        if (dto.getHashMethod() != null) {
            this.hashMethod = dto.getHashMethod();
        }
    }

    public PythonRepository(PythonRepository that) {
        super(that);
        this.hashMethod = that.hashMethod;
    }
}
