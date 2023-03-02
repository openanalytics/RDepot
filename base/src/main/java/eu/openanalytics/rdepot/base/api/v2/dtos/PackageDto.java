/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import eu.openanalytics.rdepot.base.entities.Package;
/**
 * DTO representing Package entity.
 */
public abstract class PackageDto<D extends PackageDto<D, T>, T extends Package<T, D>> 
	implements IDto<T> {

	protected Integer id;
	protected Integer userId;
	protected String version;
	protected Integer repositoryId;
	protected Integer submissionId;
	protected String name;
	protected String description;
	protected String author;
	protected String title;
	protected String url;
	protected String source;
	protected Boolean active;
	protected Boolean deleted;

	public PackageDto() {
		
	}

	public PackageDto(Package<T,D> packageBag) {
		this.id = packageBag.getId();
		this.userId = packageBag.getUser().getId();
		this.version = packageBag.getVersion();
		this.repositoryId = packageBag.getRepository().getId();
		this.submissionId = packageBag.getSubmission().getId();
		this.name = packageBag.getName();
		this.description = packageBag.getDescription();
		this.author = packageBag.getAuthor();
		this.title = packageBag.getTitle();
		this.url = packageBag.getUrl();
		this.source = packageBag.getSource();
		this.active = packageBag.isActive();
		this.deleted = packageBag.isDeleted();
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public Integer getRepositoryId() {
		return repositoryId;
	}
	
	public void setRepositoryId(Integer repositoryId) {
		this.repositoryId = repositoryId;
	}
	
	public Integer getSubmissionId() {
		return submissionId;
	}
	
	public void setSubmissionId(Integer submissionId) {
		this.submissionId = submissionId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}

	public Boolean getActive() {
		return active;
	}
	
	public void setActive(Boolean active) {
		this.active = active;
	}
	
	public Boolean getDeleted() {
		return deleted;
	}
	
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}
