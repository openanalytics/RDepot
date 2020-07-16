/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.User;

@org.springframework.stereotype.Repository
@Transactional(readOnly = true)
public interface RepositoryEventRepository extends JpaRepository<RepositoryEvent, Integer>
{
	public List<RepositoryEvent> findByChangedBy(User changedBy);
	public List<RepositoryEvent> findByRepository(Repository repository);
	public List<RepositoryEvent> findByDate(Date date);
	public RepositoryEvent findByRepositoryAndEvent_Value(Repository repository, String value);
	public List<RepositoryEvent> findByDateAndRepository(Date date, Repository repository);
	@Query("SELECT r FROM RepositoryEvent r WHERE r.valueAfter = (:valueAfter) AND r.changedVariable <> (:changedVariable)")
	public List<RepositoryEvent> findByValueAfterAndChangedVariableNot(@Param("valueAfter") String valueAfter, @Param("changedVariable") String changedVariable);	
	@Query("SELECT r FROM RepositoryEvent r WHERE r.date = (:date) AND r.valueAfter = (:valueAfter) AND r.changedVariable = (:changedVariable)")
	public List<RepositoryEvent> findByDateAndValueAfterAndChangedVariableNot(@Param("date") Date date, @Param("valueAfter") String valueAfter, @Param("changedVariable") String changedVariable);
}
