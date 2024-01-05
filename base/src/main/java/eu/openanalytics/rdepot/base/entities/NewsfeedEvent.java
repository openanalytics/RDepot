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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import eu.openanalytics.rdepot.base.api.v2.dtos.NewsfeedEventDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;

/**
 * Newsfeed Event contains information about what happened in the system.
 * For example, whenever a user uploads a package, 
 * it is registered and then displayed in the Newsfeed.
 * Events are created on per request basis. 
 * Therefore, if a strategy leads to certain side-effect actions,
 * these actions are not registered with their separate events.
 */
@Entity
@Table(name = "newsfeed_event", schema = "public")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@SuppressWarnings("unused")
public class NewsfeedEvent extends Resource 
	implements IEntity<NewsfeedEventDto> {
	
	@Column(name = "time", nullable = false)
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//    @JsonSerialize(using = LocalDateTimeSerializer.class)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm:ss")
	private LocalDateTime time = LocalDateTime.now(); //TODO: can it be null? previously it was
	
	@Column(name = "date", nullable = false)
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//    @JsonSerialize(using = LocalDateTimeSerializer.class)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private LocalDate date = time.toLocalDate();
	
	@ManyToOne
	@JoinColumn(name = "author_id", nullable = false)
	private User author;
	
	@ManyToOne
	@JoinColumn(name = "related_packagemaintainer_id", nullable = true)
	private PackageMaintainer packageMaintainer;
	
	@ManyToOne
	@JoinColumn(name = "related_repositorymaintainer_id", nullable = true)
	private RepositoryMaintainer repositoryMaintainer;
	
	@ManyToOne
	@JoinColumn(name = "related_user_id", nullable = true)
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "related_submission_id", nullable = true)
	private Submission submission;
	
	@ManyToOne
	@JoinColumn(name = "related_repository_id", nullable = true)
	private Repository<?, ?> repository;
	
	@ManyToOne
	@JoinColumn(name = "related_package_id", nullable = true)
	private Package<?, ?> packageBag;

	@Enumerated(EnumType.STRING)
	@Column(name = "newsfeed_event_type", nullable = false)
	private NewsfeedEventType type;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE, mappedBy = "relatedNewsfeedEvent")
	private Set<EventChangedVariable> eventChangedVariables = new HashSet<>();
	
	public NewsfeedEvent() {
		super(InternalTechnology.instance, ResourceType.EVENT);
	}
	
	public NewsfeedEvent(User author, 
			NewsfeedEventType type) {
		this();
		this.author = author;
		this.type = type;
	}
	
	public NewsfeedEvent(User author, 
			NewsfeedEventType type, PackageMaintainer packageMaintainer) {
		this(author, type);
		this.packageMaintainer = packageMaintainer;
	}
	
	public NewsfeedEvent(User author, 
			NewsfeedEventType type, RepositoryMaintainer repositoryMaintainer) {
		this(author, type);
		this.repositoryMaintainer = repositoryMaintainer;
	}
	
	public NewsfeedEvent(User author, 
			NewsfeedEventType type, Submission submission) {
		this(author, type);
		this.submission = submission;
	}
	
	public NewsfeedEvent(User author, 
			NewsfeedEventType type, User user) {
		this(author, type);
		this.user = user;
	}
	
	public NewsfeedEvent(User author, 
			NewsfeedEventType type, Repository<?,?> repository) {
		this(author, type);
		this.repository = repository;
	}
	
	public NewsfeedEvent(User author, 
			NewsfeedEventType type, Package<?,?> packageBag) {
		this(author, type);
		this.packageBag = packageBag;
	}	
	
	public Set<EventChangedVariable> getEventChangedVariables() {
		return eventChangedVariables;
	}
	
	public void setEventChangedVariables(Set<EventChangedVariable> eventChangedVariables) {
		this.eventChangedVariables = eventChangedVariables;
	}
	
	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public NewsfeedEventType getType() {
		return type;
	}

	public void setType(NewsfeedEventType type) {
		this.type = type;
	}

	@Override
	public NewsfeedEventDto createDto() {
		return new NewsfeedEventDto(this);
	}

	@Override
	public String toString() {
		return null; 
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Returns related resource, depending on what the event was used for.
	 * The resource can be assigned only once, during construction.
	 * This way erroneous assignment of multiple resources is prevented.
	 * @return related resource
	 */
	@Transient
	public Resource getRelatedResource() {
		if(packageMaintainer != null)
			return packageMaintainer;
		else if(repositoryMaintainer != null)
			return repositoryMaintainer;
		else if(user != null)
			return user;
		else if(submission != null)
			return submission;
		else if(packageBag != null)
			return packageBag;
		else if(repository != null)
			return repository;
		else
			return null;
	}

	private PackageMaintainer getPackageMaintainer() {
		return packageMaintainer;
	}

	private RepositoryMaintainer getRepositoryMaintainer() {
		return repositoryMaintainer;
	}

	private User getUser() {
		return user;
	}

	private Submission getSubmission() {
		return submission;
	}

	private Repository<?, ?> getRepository() {
		return repository;
	}

	private Package<?, ?> getPackageBag() {
		return packageBag;
	}

	private void setPackageMaintainer(PackageMaintainer packageMaintainer) {
		this.packageMaintainer = packageMaintainer;
	}

	private void setRepositoryMaintainer(RepositoryMaintainer repositoryMaintainer) {
		this.repositoryMaintainer = repositoryMaintainer;
	}

	private void setUser(User user) {
		this.user = user;
	}

	private void setSubmission(Submission submission) {
		this.submission = submission;
	}

	private void setRepository(Repository<?, ?> repository) {
		this.repository = repository;
	}

	private void setPackageBag(Package<?, ?> packageBag) {
		this.packageBag = packageBag;
	}
}
