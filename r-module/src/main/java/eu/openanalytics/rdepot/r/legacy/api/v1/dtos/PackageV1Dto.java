/**
 * R Depot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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
package eu.openanalytics.rdepot.r.legacy.api.v1.dtos;

import java.util.List;
import java.util.stream.Collectors;

import eu.openanalytics.rdepot.r.entities.RPackage;

public class PackageV1Dto {
	private int id;
	private String version;
	private RepositoryProjection repository;
	private SubmissionProjection submission;
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
	private boolean active;
	private boolean deleted;
	private UserProjection user;

	public PackageV1Dto(RPackage packageBag){
		this.id = packageBag.getId();
		this.version = packageBag.getVersion();
		this.repository = new  RepositoryProjection(packageBag.getRepository());
		this.submission = new SubmissionProjection(packageBag.getSubmission());
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
		this.user = new UserProjection(packageBag.getUser());		
	}
	
	static PackageV1Dto of(RPackage packageBag){
		return new PackageV1Dto(packageBag);
	}
	
	public static List<PackageV1Dto> toDtoList(List<RPackage> entities) {
		return entities.stream().map(PackageV1Dto::of).collect(Collectors.toList());
	}
	
	public int getId() {
		return id;
	}
	public String getVersion() {
		return version;
	}
	public RepositoryProjection getRepository() {
		return repository;
	}
	public SubmissionProjection getSubmission() {
		return submission;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public String getAuthor() {
		return author;
	}
	public String getDepends() {
		return depends;
	}
	public String getImports() {
		return imports;
	}
	public String getSuggests() {
		return suggests;
	}
	public String getSystemRequirements() {
		return systemRequirements;
	}
	public String getLicense() {
		return license;
	}
	public String getTitle() {
		return title;
	}
	public String getUrl() {
		return url;
	}
	public String getSource() {
		return source;
	}
	public String getMd5sum() {
		return md5sum;
	}
	public boolean isActive() {
		return active;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public UserProjection getUser() {
		return user;
	}
}
