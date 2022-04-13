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
package eu.openanalytics.rdepot.api.v2.dto;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Submission;

public class RPackageDto extends EntityDto<Package> {
	
//	@JsonProperty("id")
	private Integer id;
	
	private String version;
	private Integer repositoryId;
	private Integer submissionId;
	private String name;
	private String description;
	private String author;
	private String depends;
	private String imports;
	private String suggests;
	private String systemRequirements;
	private String license;
	private String title;
	private String url;
	private String source;
	private String md5sum;
	private Boolean active;
	private Boolean deleted;
	
	@JsonIgnore
	private Set<RVignetteDto> vignettes = new HashSet<>();
	private String fileName;
	
	public RPackageDto() {
		
	}
	
	public RPackageDto(Package packageBag) {
		super(packageBag);
		this.id = packageBag.getId();
		this.version = packageBag.getVersion();
		this.repositoryId = packageBag.getRepository().getId();
		this.submissionId = packageBag.getSubmission().getId();
		this.name = packageBag.getName();
		this.description = packageBag.getDescription();
		this.author = packageBag.getAuthor();
		this.depends = packageBag.getDepends();
		this.imports = packageBag.getImports();
		this.suggests = packageBag.getSuggests();
		this.systemRequirements = packageBag.getSystemRequirements();
		this.license = packageBag.getLicense();
		this.title = packageBag.getTitle();
		this.url = packageBag.getUrl();
		this.source = packageBag.getSource();
		this.md5sum = packageBag.getMd5sum();
		this.active = packageBag.isActive();
		this.deleted = packageBag.isDeleted();
//		this.vignettes = packageBag.getVignettes(); //TODO: add vignettes support
		this.fileName = packageBag.getFileName();
		
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
	
	public String getDepends() {
		return depends;
	}
	
	public void setDepends(String depends) {
		this.depends = depends;
	}
	
	public String getImports() {
		return imports;
	}
	
	public void setImports(String imports) {
		this.imports = imports;
	}
	
	public String getSuggests() {
		return suggests;
	}
	
	public void setSuggests(String suggests) {
		this.suggests = suggests;
	}
	
	public String getSystemRequirements() {
		return systemRequirements;
	}
	
	public void setSystemRequirements(String systemRequirements) {
		this.systemRequirements = systemRequirements;
	}
	
	public String getLicense() {
		return license;
	}
	
	public void setLicense(String license) {
		this.license = license;
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
	
	public String getMd5sum() {
		return md5sum;
	}
	
	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
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
	
	@JsonIgnore
	public Set<RVignetteDto> getVignettes() {
		return vignettes;
	}
	
	@JsonIgnore
	public void setVignettes(Set<RVignetteDto> vignettes) {
		this.vignettes = vignettes;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public Package toEntity() {
		Package entity = new Package();
		entity.setActive(active);
		entity.setAuthor(author);
		entity.setDeleted(deleted);
		entity.setDepends(depends);
		entity.setDescription(description);
		entity.setId(id);
		entity.setImports(imports);
		entity.setLicense(license);
		entity.setMd5sum(md5sum);
		entity.setName(name);
		Repository repository = new Repository();
		repository.setId(repositoryId);
		entity.setRepository(repository);
		entity.setSource(source);
		Submission submission = new Submission();
		submission.setId(submissionId);
		entity.setSubmission(submission);
		entity.setSuggests(suggests);
		entity.setSystemRequirements(systemRequirements);
		entity.setTitle(title);
		entity.setUrl(url);
		entity.setVersion(version);
		
		return entity;
	}
}
