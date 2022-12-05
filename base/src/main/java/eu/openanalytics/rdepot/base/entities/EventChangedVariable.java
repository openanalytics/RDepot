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

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Objects of this class contain information about 
 * properties changed during an update request.
 * They are later attached to their {@link NewsfeedEvent}.
 */
@Entity
@Table(name = "changed_variable", schema = "public")
public class EventChangedVariable {
	
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id = 0;
	
	@Column(name = "changed_variable", nullable = false)
	private String changedVariable;
	
	@Column(name = "value_before", nullable = false)
	private String valueBefore;
	
	@Column(name = "value_after", nullable = false)
	private String valueAfter;
	
	@ManyToOne
	@JoinColumn(name = "newsfeed_event_id", nullable = false)
	private NewsfeedEvent relatedNewsfeedEvent;
	
	@Column(name = "deleted", nullable = false)
	protected Boolean deleted = false;
	
	public EventChangedVariable() {}

	public EventChangedVariable(String changedVariable, String valueBefore, 
			String valueAfter) {
		super();
		this.changedVariable = changedVariable;
		this.valueBefore = valueBefore;
		this.valueAfter = valueAfter;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getChangedVariable() {
		return changedVariable;
	}

	public void setChangedVariable(String changedVariable) {
		this.changedVariable = changedVariable;
	}

	public String getValueBefore() {
		return valueBefore;
	}

	public void setValueBefore(String valueBefore) {
		this.valueBefore = valueBefore;
	}

	public String getValueAfter() {
		return valueAfter;
	}

	public void setValueAfter(String valueAfter) {
		this.valueAfter = valueAfter;
	}

	public NewsfeedEvent getRelatedNewsfeedEvent() {
		return relatedNewsfeedEvent;
	}

	public void setRelatedEvent(NewsfeedEvent relatedNewsfeedEvent) {
		this.relatedNewsfeedEvent = relatedNewsfeedEvent;
	}

	@Override
	public String toString() {
		return "Property " + changedVariable 
				+ " changed from \"" 
				+ valueBefore + "\" to \"" 
				+ valueAfter + "\".";
	}

	@Override
	public int hashCode() {
		return Objects.hash(changedVariable, id, relatedNewsfeedEvent, valueAfter, valueBefore);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventChangedVariable other = (EventChangedVariable) obj;
		return Objects.equals(changedVariable, other.changedVariable) && id == other.id
				&& Objects.equals(relatedNewsfeedEvent, other.relatedNewsfeedEvent)
				&& Objects.equals(valueAfter, other.valueAfter) && Objects.equals(valueBefore, other.valueBefore);
	}
	
	public Boolean isDeleted() {
		return deleted;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
