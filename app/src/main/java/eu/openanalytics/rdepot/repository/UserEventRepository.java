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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.model.UserEvent;

@Repository
//@Transactional(readOnly = true)
public interface UserEventRepository extends JpaRepository<UserEvent, Integer>
{
	public List<UserEvent> findByChangedBy(User changedBy);
	public List<UserEvent> findByUser(User user);
	@Query("SELECT u FROM UserEvent u WHERE u.user = (:user) AND u.changedVariable <> 'last logged in'")
	public List<UserEvent> findByUserNoLogs(@Param("user") User user);
	public List<UserEvent> findByDate(Date date);
	public UserEvent findByUserAndEvent_Value(User user, String string);
	public List<UserEvent> findByDateAndUser(Date date, User user);
	public default List<UserEvent> findByDateAndUserNoLogs(Date date, User user)
	{
		List<UserEvent> events = findByDateAndUserOnlyRoles(date, user);
		events.addAll(findByDateAndUserNoLogsNoRoles(date, user));
		return events;
	}
	@Query(
		"SELECT new UserEvent(u1.id, u1.date, u1.changedBy, u1.user, u1.event, u1.changedVariable, r1.description, r2.description, u1.time) "
		+ "FROM UserEvent u1 "
		+ "JOIN Role r1 ON r1.value = CAST(u1.valueBefore AS java.lang.Integer) "
		+ "JOIN Role r2 ON r2.value = CAST(u1.valueAfter AS java.lang.Integer) "
		+ "WHERE u1.user = (:user) AND u1.date = (:date) AND u1.changedVariable = 'role'")
	public List<UserEvent> findByDateAndUserOnlyRoles(@Param("date") Date date, @Param("user") User user);
	@Query(
		"SELECT u2 FROM UserEvent u2 "
		+ "WHERE u2.user = (:user) AND u2.date = (:date) AND u2.changedVariable <> 'last logged in' AND u2.changedVariable <> 'role'")
	public List<UserEvent> findByDateAndUserNoLogsNoRoles(@Param("date") Date date, @Param("user") User user);
	@Query("SELECT u FROM UserEvent u WHERE u.id = (SELECT MAX(id) FROM UserEvent e WHERE e.user = (:user) AND e.changedVariable = (:changedVariable))")
	public UserEvent findLastByUserAndChangedVariable(@Param("user") User user, @Param("changedVariable") String changedVariable);
}
