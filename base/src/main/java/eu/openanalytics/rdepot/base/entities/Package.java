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
package eu.openanalytics.rdepot.base.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.technology.Technology;

/**
 * Entity representing a package.
 * It should be extending depending on the requirements of the implemented technology.
 * @param <P> Extended Package entity (e.g. for R it is {@link RPackage}).
 * @param <D> Extended {@link IDto DTO} object (e.g. for R it is {@link RPackageDto}).
 */
@Entity
@Table(name = "package", schema = "public")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class Package<P extends Package<P,D>, D extends PackageDto<D, P>> 
	extends Resource 
	implements Comparable<P>, IEntity<D>, Serializable {
	
	private static final long serialVersionUID = 2298415552029766827L;

	@Column(name = "version", nullable = false)
	private String version;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "repository_id", nullable = false)
	private Repository<?, ?> repository;
	
	@ManyToOne
	@JoinColumn(name = "maintainer_id", nullable = false)
	private User user;
	
	@OneToOne(mappedBy = "packageBag")
	@JoinColumn(name = "submission_id")
	private Submission submission;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "description", nullable = false)
	private String description;
	
	@Column(name = "author", nullable = false)
	private String author;
	
	@Column(name = "title", nullable = false)
	private String title;
	
	@Column(name = "url")
	private String url = "";
	
	@Column(name = "source", nullable = false)
	private String source;
	
	@Column(name = "active", nullable = false)
	private boolean active;
	
	@OneToMany(orphanRemoval = true, mappedBy = "packageBag")
	private Set<NewsfeedEvent> events;
	
	protected Package(Package<P,D> packageBag) {
		super(packageBag.id, packageBag.getTechnology(), ResourceType.PACKAGE);
		this.repository = packageBag.repository;
		this.user = packageBag.user;
		this.name = packageBag.name;
		this.description = packageBag.description;
		this.author = packageBag.author;
		this.url = packageBag.url;
		this.source = packageBag.source;
		this.title = packageBag.title;
		this.active = packageBag.active;
		this.deleted = packageBag.deleted;
		this.submission = packageBag.submission;
		this.version = packageBag.version;
	}

	protected Package(Technology technology) {
		super(technology, ResourceType.PACKAGE);
	}
	
	protected Package(Technology technology, D packageDto, 
			Submission submission, User user) {
		this(technology);
		this.submission = submission;
		this.user = user;
		this.id = packageDto.getId();
		this.name = packageDto.getName();
		this.description = packageDto.getDescription();
		this.author = packageDto.getAuthor();
		this.url = packageDto.getUrl();
		this.source = packageDto.getSource();
		this.title = packageDto.getTitle();
		this.active = packageDto.getActive();
		this.deleted = packageDto.getDeleted();
		this.version = packageDto.getVersion();
	}

	protected Package(Technology technology, int id, Repository<?, ?> repository, User user, String name,
			String description, String author, String source,
			String title, boolean active, boolean deleted) {
		super(id, technology, ResourceType.PACKAGE);
		this.repository = repository;
		this.user = user;
		this.name = name;
		this.description = description;
		this.author = author;
		this.title = title;
		this.source = source;
		this.active = active;
		this.deleted = deleted;
	}
	
	protected Package(Technology technology, int id, Repository<?,?> repository, User user, String name,
			String description, String author,
			String url, String source, String title, boolean active, boolean deleted,
			Submission submission) {
		super(id, technology, ResourceType.PACKAGE);
		this.repository = repository;
		this.user = user;
		this.name = name;
		this.description = description;
		this.author = author;
		this.url = url;
		this.source = source;
		this.title = title;
		this.active = active;
		this.submission = submission;
		this.deleted = deleted;
	}
	
	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public Repository<?, ?> getRepository() {
		return this.repository;
	}
	
	public void setRepository(Repository<?,?> repository) {
		this.repository = repository;
	}
			
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}
		
	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	@Transient
	public String getFileName() {
		int pathLength = this.source.split("/").length;
		String filename = this.source.split("/")[pathLength - 1];
		return filename;
	}
	
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}
		
	public Submission getSubmission() {
		return this.submission;
	}

	public void setSubmission(Submission submission) {
		this.submission = submission;
	}

	@Override
	public String toString() {
		return "Package [id: " 
				+ id + ", technology: \"" 
				+ getTechnology().getName() + " " 
				+ getTechnology().getVersion() + "\", name: \"" 
				+ name + "\", version: \"" 
				+ version +"\"]";
 	}

	@Override
	public int compareTo(P that) 
	{	
		if (!this.name.equals(that.getName())) {
			throw new IllegalArgumentException(
					"Trying to compare package " + that.getName() + 
					" with package " + this.name + 
					" is like comparing apples with oranges.");
		}
				
		String[] theseSplittedDots = this.version.split("-|\\.");
		String[] thoseSplittedDots = that.getVersion().split("-|\\.");
		int length;
		if(theseSplittedDots.length - thoseSplittedDots.length > 0) {
			length = thoseSplittedDots.length;
		} else {
			length = theseSplittedDots.length;
		}
		
		int thisNumber, thatNumber;
		for(int i = 0; i < length; i++) {
			thisNumber = Integer.parseInt(theseSplittedDots[i]);
			thatNumber = Integer.parseInt(thoseSplittedDots[i]);
			if(thisNumber > thatNumber) {
				return 1;
			}
			if(thatNumber > thisNumber) {
				return -1;
			}
		}
		
		if(theseSplittedDots.length == thoseSplittedDots.length) { 
			return 0;
		}
		
		if(theseSplittedDots.length - thoseSplittedDots.length > 0) {
			return 1;
		} else {
			return -1;
		}
	}
}
