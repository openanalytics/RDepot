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
package eu.openanalytics.rdepot.model;

// Generated Jun 24, 2013 12:33:03 PM by Hibernate Tools 4.0.0

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * User generated by hbm2java
 */
@Entity
@Table(name = "user", schema = "public", uniqueConstraints = {
		@UniqueConstraint(columnNames = "login"),
		@UniqueConstraint(columnNames = "email") })
//@JsonDeserialize(using=UserDeserializer.class)
public class User implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5552711100729158602L;
	private int id;
	private Role role;
	private String name;
	private String email;
	private String login;
	private boolean active;
	private Date lastLoggedInOn;
	private boolean deleted = false;
	private Set<RepositoryMaintainer> repositoryMaintainers = new HashSet<RepositoryMaintainer>(0);
	private Set<PackageMaintainer> packageMaintainers = new HashSet<PackageMaintainer>(0);
	private Set<Package> packages = new HashSet<Package>(0);
	private Set<UserEvent> userEvents = new HashSet<UserEvent>(0);
	private Set<Submission> submissions = new HashSet<Submission>(0);
	private Set<UserEvent> changedUserEvents = new HashSet<UserEvent>(0);
	private Set<PackageEvent> changedPackageEvents = new HashSet<PackageEvent>(0);
	private Set<RepositoryEvent> changedRepositoryEvents = new HashSet<RepositoryEvent>(0);
	private Set<PackageMaintainerEvent> changedPackageMaintainerEvents = new HashSet<PackageMaintainerEvent>(0);
	private Set<RepositoryMaintainerEvent> changedRepositoryMaintainerEvents = new HashSet<RepositoryMaintainerEvent>(0);
	private Set<SubmissionEvent> changedSubmissionEvents = new HashSet<SubmissionEvent>(0);
	
	public User()
	{
	}

	public User(int id, Role role, String name, String email, String login, boolean active, boolean deleted)
	{
		this.id = id;
		this.role = role;
		this.name = name;
		this.email = email;
		this.login = login;
		this.active = active;
		this.deleted = deleted;
	}
	
	public User(int id, Role role, String name, String email, String login, boolean active, boolean deleted, Date lastLoggedInOn)
	{
		this.id = id;
		this.role = role;
		this.name = name;
		this.email = email;
		this.login = login;
		this.active = active;
		this.lastLoggedInOn = lastLoggedInOn;
		this.deleted = deleted;
	}

	public User(int id, Role role, String name, String email, String login,
			boolean active, boolean deleted, Set<RepositoryMaintainer> repositoryMaintainers,
			Set<Submission> submissions, Set<PackageMaintainer> packageMaintainers,
			Set<Package> packages, Set<UserEvent> userEvents, Set<UserEvent> changedUserEvents,
			Set<PackageEvent> changedPackageEvents, Set<RepositoryEvent> changedRepositoryEvents,
			Set<PackageMaintainerEvent> changedPackageMaintainerEvents, 
			Set<RepositoryMaintainerEvent> changedRepositoryMaintainerEvents,
			Set<SubmissionEvent> changedSubmissionEvents)
	{
		this.id = id;
		this.role = role;
		this.name = name;
		this.email = email;
		this.login = login;
		this.active = active;
		this.deleted = deleted;
		this.repositoryMaintainers = repositoryMaintainers;
		this.submissions = submissions;
		this.packageMaintainers = packageMaintainers;
		this.packages = packages;	
		this.userEvents = userEvents;
		this.changedUserEvents = changedUserEvents;
		this.changedPackageEvents = changedPackageEvents;
		this.changedRepositoryEvents = changedRepositoryEvents;
		this.changedPackageMaintainerEvents = changedPackageMaintainerEvents;
		this.changedRepositoryMaintainerEvents = changedRepositoryMaintainerEvents;
		this.changedSubmissionEvents = changedSubmissionEvents;
	}
	
	public User(int id, Role role, String name, String email, String login,
			boolean active, boolean deleted, Date lastLoggedInOn, Set<RepositoryMaintainer> repositoryMaintainers,
			Set<Submission> submissions, Set<PackageMaintainer> packageMaintainers, 
			Set<Package> packages, Set<UserEvent> userEvents, Set<UserEvent> changedUserEvents,
			Set<PackageEvent> changedPackageEvents, Set<RepositoryEvent> changedRepositoryEvents,
			Set<PackageMaintainerEvent> changedPackageMaintainerEvents, 
			Set<RepositoryMaintainerEvent> changedRepositoryMaintainerEvents,
			Set<SubmissionEvent> changedSubmissionEvents)
	{
		this.id = id;
		this.role = role;
		this.name = name;
		this.email = email;
		this.login = login;
		this.active = active;
		this.deleted = deleted;
		this.repositoryMaintainers = repositoryMaintainers;
		this.submissions = submissions;
		this.packageMaintainers = packageMaintainers;
		this.packages = packages;
		this.lastLoggedInOn = lastLoggedInOn;
		this.userEvents = userEvents;
		this.changedUserEvents = changedUserEvents;
		this.changedPackageEvents = changedPackageEvents;
		this.changedRepositoryEvents = changedRepositoryEvents;
		this.changedPackageMaintainerEvents = changedPackageMaintainerEvents;
		this.changedRepositoryMaintainerEvents = changedRepositoryMaintainerEvents;
		this.changedSubmissionEvents = changedSubmissionEvents;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public int getId()
	{
		return this.id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", nullable = false)
	public Role getRole()
	{
		return this.role;
	}

	public void setRole(Role role)
	{
		this.role = role;
	}

	@Column(name = "name", nullable = false)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Column(name = "email", unique = true, nullable = false)
	public String getEmail()
	{
		return this.email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	@Column(name = "login", unique = true, nullable = false)
	public String getLogin()
	{
		return this.login;
	}

	public void setLogin(String login)
	{
		this.login = login;
	}

	@Column(name = "active", nullable = false)
	public boolean isActive()
	{
		return this.active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	//@JsonBackReference(value="user-repository-maintainers")
	public Set<RepositoryMaintainer> getRepositoryMaintainers()
	{
		return this.repositoryMaintainers;
	}
	
	public void setRepositoryMaintainers(Set<RepositoryMaintainer> repositoryMaintainers)
	{
		this.repositoryMaintainers = repositoryMaintainers;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	//@JsonBackReference(value="user-submissions")
	public Set<Submission> getSubmissions()
	{
		return this.submissions;
	}

	public void setSubmissions(Set<Submission> submissions)
	{
		this.submissions = submissions;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	//@JsonBackReference(value="user-package-maintainers")
	public Set<PackageMaintainer> getPackageMaintainers()
	{
		return this.packageMaintainers;
	}

	public void setPackageMaintainers(Set<PackageMaintainer> packageMaintainers)
	{
		this.packageMaintainers = packageMaintainers;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	//@JsonBackReference(value="user-packages")
	public Set<Package> getPackages()
	{
		return this.packages;
	}

	public void setPackages(Set<Package> packages)
	{
		this.packages = packages;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name="last_logged_in_on")
	public Date getLastLoggedInOn() 
	{
		return lastLoggedInOn;
	}

	public void setLastLoggedInOn(Date lastLoggedInOn) 
	{
		this.lastLoggedInOn = lastLoggedInOn;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	//@JsonBackReference(value="user-events")
	public Set<UserEvent> getUserEvents()
	{
		return this.userEvents;
	}

	public void setUserEvents(Set<UserEvent> userEvents)
	{
		this.userEvents = userEvents;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "changedBy")
	//@JsonBackReference(value="user-events-changed")
	public Set<UserEvent> getChangedUserEvents()
	{
		return this.changedUserEvents;
	}

	public void setChangedUserEvents(Set<UserEvent> changedUserEvents)
	{
		this.changedUserEvents = changedUserEvents;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "changedBy")
	@JsonBackReference(value="user-package-events")
	public Set<PackageEvent> getChangedPackageEvents()
	{
		return this.changedPackageEvents;
	}

	public void setChangedPackageEvents(Set<PackageEvent> changedPackageEvents)
	{
		this.changedPackageEvents = changedPackageEvents;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "changedBy")
	//@JsonBackReference(value="user-repository-events")
	public Set<RepositoryEvent> getChangedRepositoryEvents()
	{
		return this.changedRepositoryEvents;
	}

	public void setChangedRepositoryEvents(Set<RepositoryEvent> changedRepositoryEvents)
	{
		this.changedRepositoryEvents = changedRepositoryEvents;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "changedBy")
	//@JsonBackReference(value="user-package-maintainer-events")
	public Set<PackageMaintainerEvent> getChangedPackageMaintainerEvents()
	{
		return this.changedPackageMaintainerEvents;
	}

	public void setChangedPackageMaintainerEvents(Set<PackageMaintainerEvent> changedPackageMaintainerEvents)
	{
		this.changedPackageMaintainerEvents = changedPackageMaintainerEvents;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "changedBy")
	//@JsonBackReference(value="user-repository-maintainer-events")
	public Set<RepositoryMaintainerEvent> getChangedRepositoryMaintainerEvents()
	{
		return this.changedRepositoryMaintainerEvents;
	}

	public void setChangedRepositoryMaintainerEvents(Set<RepositoryMaintainerEvent> changedRepositoryMaintainerEvents)
	{
		this.changedRepositoryMaintainerEvents = changedRepositoryMaintainerEvents;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "changedBy")
	//@JsonBackReference(value="user-submission-events")
	public Set<SubmissionEvent> getChangedSubmissionEvents()
	{
		return this.changedSubmissionEvents;
	}

	public void setChangedSubmissionEvents(Set<SubmissionEvent> changedSubmissionEvents)
	{
		this.changedSubmissionEvents = changedSubmissionEvents;
	}
	
	@Column(name = "deleted", nullable = false)
	public boolean isDeleted()
	{
		return this.deleted;
	}

	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}	
}
