/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
package eu.openanalytics.rdepot.base.service;

import java.util.List;
import java.util.Optional;

import eu.openanalytics.rdepot.base.daos.SubmissionDao;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;

@org.springframework.stereotype.Service
public class SubmissionService extends Service<Submission> {
	
	private final SubmissionDao submissionDao;
	
	public SubmissionService(SubmissionDao dao) {
		super(dao);
		this.submissionDao = dao; 
	}

	public List<Submission> findBySubmitter(User submitter) {
		return submissionDao.findByUser(submitter);
	}

	public List<Submission> findAllByRepository(Repository<?, ?> repository) {
		return submissionDao.findByPackageBag_Repository(repository);
	}
	
	public Optional<Submission> findByPackage(Package<?,?> packageBag) {
		return submissionDao.findByPackageBag(packageBag);
	}
	
	public List<Submission> findAllByState(SubmissionState state){
		return submissionDao.findAllByState(state);
	}
	public Optional<Submission> findByIdAndState(int id, SubmissionState state){
		return submissionDao.findByIdAndState(id, state);
	}
}
