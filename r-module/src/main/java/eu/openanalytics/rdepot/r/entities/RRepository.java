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
package eu.openanalytics.rdepot.r.entities;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.r.api.v2.dtos.RRepositoryDto;
import eu.openanalytics.rdepot.r.mirroring.CranMirror;
import eu.openanalytics.rdepot.r.technology.RLanguage;

@Entity
@SecondaryTable(name = "rrepository", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public class RRepository extends Repository<RRepository, RRepositoryDto> 
	implements Serializable {
	
	private static final long serialVersionUID = 3346101145064616895L;

	@Transient
	private Set<CranMirror> cranMirrors;
	
	@JsonProperty("packages")
	public Set<RPackage> getRPackages() { 
		return getPackages().stream()
				.filter(p -> p instanceof RPackage)
				.map(p -> (RPackage)p)
				.collect(Collectors.toSet());
	}
	
	public RRepository() {
		super(RLanguage.instance);
	}
	
	public RRepository(RRepositoryDto dto) {
		super(RLanguage.instance, dto);
	}
	
	public RRepository(RRepository that) {
		super(that);
	}

	@Override
	public RRepositoryDto createDto() {
		return new RRepositoryDto(this);
	}

	@Override
	public String getDescription() {
		return "R Repository \"" + getName() + "\"";
	}
	
	@Transient
	public void setMirrors(Set<CranMirror> mirrors) {
		this.mirrors.addAll(mirrors);
		this.cranMirrors = mirrors;
	}
	
	@Transient
	public Set<CranMirror> getCranMirrors() {
		return cranMirrors;
	}
}
