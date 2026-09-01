/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.base.mirroring.dtos;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryProjection;
import eu.openanalytics.rdepot.base.entities.RepositorySynchronizationStatus;
import eu.openanalytics.rdepot.base.mirroring.pojos.SynchronizationStatusEnum;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.PagedModel.PageMetadata;

@Data
@NoArgsConstructor
public class RepositorySynchronizationStatusDto {
    private RepositoryProjection repository;
    private String status;
    private Date timestamp;
    private List<PackageSynchronizationStatusDto> packages;
    private PageMetadata page;

    @JsonIgnore
    private boolean pending;

    @JsonGetter("status")
    public String getStatusForJson() {
        if (pending) return SynchronizationStatusEnum.PENDING.getStatus();

        return this.status;
    }

    public RepositorySynchronizationStatusDto(
            RepositorySynchronizationStatus repoStatus,
            List<PackageSynchronizationStatusDto> packages,
            PageMetadata page) {
        this.repository = repoStatus.getRepository();
        this.status = repoStatus.getStatus().getStatus();
        this.pending = repoStatus.isPending();
        this.timestamp = repoStatus.getTimestamp();
        this.packages = packages;
        this.page = page;
    }
}
