/*
 * RDepot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.entities;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objects of this class contain information about 
 * properties changed during an update request.
 * They are later attached to their {@link NewsfeedEvent}.
 */
@Entity
@Data
@NoArgsConstructor
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
	
	public EventChangedVariable(String changedVariable, String valueBefore, 
			String valueAfter) {
		super();
		this.changedVariable = changedVariable;
		this.valueBefore = valueBefore;
		this.valueAfter = valueAfter;
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
}
