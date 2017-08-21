/**
 * RDepot
 *
 * Copyright (C) 2012-2017 Open Analytics NV
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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.model.UserEvent;

@Repository
@Transactional(readOnly = true)
public interface UserEventRepository extends JpaRepository<UserEvent, Integer>
{
	public List<UserEvent> findByChangedBy(User changedBy);
	public List<UserEvent> findByUser(User user);
	@Query("SELECT u FROM UserEvent u WHERE u.user = (:user) AND u.changedVariable <> 'last logged in'")
	public List<UserEvent> findByUserNoLogs(@Param("user") User user);
	public List<UserEvent> findByDate(Date date);
	public UserEvent findByUserAndEvent_Value(User user, String string);
	public List<UserEvent> findByDateAndUser(Date date, User user);
	@Query("SELECT u FROM UserEvent u WHERE u.user = (:user) AND u.date = (:date) AND u.changedVariable <> 'last logged in'")
	public List<UserEvent> findByDateAndUserNoLogs(@Param("date") Date date, @Param("user") User user);
	@Query("SELECT u FROM UserEvent u WHERE u.id = (SELECT MAX(id) FROM UserEvent e WHERE e.user = (:user) AND e.changedVariable = (:changedVariable))")
	public UserEvent findLastByUserAndChangedVariable(@Param("user") User user, @Param("changedVariable") String changedVariable);
}
