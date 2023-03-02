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
package eu.openanalytics.rdepot.r.api.v2.dtos;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.r.entities.RPackage;

public class RPackageDto extends PackageDto<RPackageDto, RPackage> implements IDto<RPackage> {
	
	private String depends;
	private String imports;
	private String suggests;
	private String systemRequirements;
	private String license;
	private String md5sum;
	private RPackage entity;
	
	@JsonIgnore
	private Set<RVignetteDto> vignettes = new HashSet<>();
//	private String fileName;
	
	public RPackageDto() {
		
	}
	
	public RPackageDto(RPackage packageBag) {
		super(packageBag);
		this.entity = packageBag;
		this.depends = packageBag.getDepends();
		this.imports = packageBag.getImports();
		this.suggests = packageBag.getSuggests();
		this.systemRequirements = packageBag.getSystemRequirements();
		this.license = packageBag.getLicense();
		this.md5sum = packageBag.getMd5sum();
//		this.vignettes = packageBag.getVignettes(); //TODO: add vignettes support
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
	
	public String getMd5sum() {
		return md5sum;
	}
	
	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}
			
	
	@JsonIgnore
	public Set<RVignetteDto> getVignettes() {
		return vignettes;
	}
	
	@JsonIgnore
	public void setVignettes(Set<RVignetteDto> vignettes) {
		this.vignettes = vignettes;
	}
	
//	public String getFileName() {
//		return fileName;
//	}
//	
//	public void setFileName(String fileName) {
//		this.fileName = fileName;
//	}

	@Override
	@JsonIgnore
	public RPackage getEntity() {
		return entity;
	}

	@Override
	public void setEntity(RPackage entity) {
		this.entity = entity;
	}
}
