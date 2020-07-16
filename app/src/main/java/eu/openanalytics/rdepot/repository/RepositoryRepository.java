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

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.model.Repository;

@org.springframework.stereotype.Repository
@Transactional(readOnly = true)
public interface RepositoryRepository extends JpaRepository<Repository, Integer>
{
	Repository findByName(String name);
	Repository findByPublicationUri(String publicationUri);
	Repository findByIdAndDeleted(int id, boolean deleted);
	List<Repository> findByDeleted(boolean deleted, Sort sort);
	List<Repository> findAll(Sort sort);
	Repository findByNameAndDeleted(String name, boolean deleted);
	Repository findByPublicationUriAndDeleted(String publicationUri, boolean deleted);
}

