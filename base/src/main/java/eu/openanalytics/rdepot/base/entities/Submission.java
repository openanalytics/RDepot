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
package eu.openanalytics.rdepot.base.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;

@Entity
@Table(name = "submission", schema = "public")
public class Submission extends Resource implements IEntity<SubmissionDto> {

	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="submitter_id", nullable=false)
    private User user;
    
    @OneToOne(fetch=FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name="package_id", nullable=false)
    private Package<?, ?> packageBag;
    
    @Column(name="changes")
    private String changes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private SubmissionState state;
    
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "submission")
    @JsonIgnore
    private Set<NewsfeedEvent> events = new HashSet<NewsfeedEvent>(0);

    public Submission() {
    	super(InternalTechnology.instance, ResourceType.SUBMISSION);
    }

    public Submission(SubmissionDto submissionDto, Package<?,?> packageBag, User user) {
    	this();
    	this.id = submissionDto.getId();
    	this.changes = submissionDto.getChanges();
    	this.state = submissionDto.getState();
    	this.user = user;
    	this.packageBag = packageBag;
    }
    
    public Submission(int id, User user, Package<?,?> packageBag, Set<NewsfeedEvent> events) {
    	this();
        this.id = id;
        this.user = user;
        this.packageBag = packageBag;
        this.events = events;
    }

    public Submission(int id, User user, Package<?,?> packageBag, String changes, Set<NewsfeedEvent> events) {
    	this();
	    this.id = id;
	    this.user = user;
	    this.packageBag = packageBag;
	    this.changes = changes;
	    this.events = events;
    }

    public Submission(Submission that) {
    	this();
    	this.id = that.id;
    	this.packageBag = that.packageBag;
    	this.state = that.state;
    	this.user = that.user;
    	this.changes = that.changes;
    	this.events = that.events;
	}

    @JoinColumn(name="repository_id", nullable=false)
    public Repository<?,?> getRepository() {
    	return packageBag.getRepository();
    }
   
    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Package<?, ?> getPackage() {
        return this.packageBag;
    }

    public void setPackage(Package<?,?> packageBag) {
        this.packageBag = packageBag;
    }

    public String getChanges() {
        return this.changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }
    
    public Set<NewsfeedEvent> getEvents() {
    	return events;
    }
    
    public void setEvents(Set<NewsfeedEvent> events) {
    	this.events = events;
    }

    @Override
	public String toString() {
		return "Submission [id: " + id + ", package: \"" 
				+ this.packageBag.getName() + " " 
				+ this.packageBag.getVersion() 
				+ "\", author: \"" 
				+ user.getLogin() + "\"]";
 	}

	@Override
	public SubmissionDto createDto() {
		return new SubmissionDto(this);
	}
	
	public void setState(SubmissionState state) {
		this.state = state;
	}
	
	public SubmissionState getState() {
		return state;
	}

	@Override
	public String getDescription() {
		return toString();
	}
}




