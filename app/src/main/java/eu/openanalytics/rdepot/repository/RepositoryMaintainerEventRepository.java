/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
package eu.openanalytics.rdepot.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.RepositoryMaintainerEvent;
import eu.openanalytics.rdepot.model.User;

@Repository
@Transactional(readOnly = true)
public interface RepositoryMaintainerEventRepository extends JpaRepository<RepositoryMaintainerEvent, Integer>
{
	public List<RepositoryMaintainerEvent> findByChangedBy(User changedBy);
	public List<RepositoryMaintainerEvent> findByRepositoryMaintainer(RepositoryMaintainer repositoryMaintainer);
	public List<RepositoryMaintainerEvent> findByDate(Date date);
}
