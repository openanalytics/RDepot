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
package eu.openanalytics.rdepot.r.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageDto;
import eu.openanalytics.rdepot.r.technology.RLanguage;

@Entity
@SecondaryTable(name = "rpackage", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"))
public class RPackage extends Package<RPackage, RPackageDto> {

	private static final long serialVersionUID = -3373259770906796151L;

	/**
	 * 
	 */
	@Column(name = "depends", table = "rpackage")
	private String depends = "";
	
	@Column(name = "imports", table = "rpackage")
	private String imports = "";
	
	@Column(name = "suggests", table = "rpackage")
	private String suggests = "";
	
	@Column(name = "system_requirements", table = "rpackage")
	private String systemRequirements = "";
	
	@Column(name = "license", nullable = false, table = "rpackage")
	private String license;
	
	@Column(name = "md5sum", nullable = false, table = "rpackage")
	private String md5sum;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "repository_id", nullable = false)
	private RRepository repository;
	
	@Transient
	private Boolean generateManuals;

	@Transient
	public Boolean getGenerateManuals() {
		return generateManuals;
	}

	@Transient
	public void setGenerateManuals(Boolean generateManuals) {
		this.generateManuals = generateManuals;
	}

	@Transient
	private List<Vignette> vignettes;
	
	public RPackage(RPackage packageBag) {
		super(packageBag);
		this.depends = packageBag.depends;
		this.imports = packageBag.imports;
		this.suggests = packageBag.suggests;
		this.systemRequirements = packageBag.systemRequirements;
		this.license = packageBag.license;
		this.md5sum = packageBag.md5sum;
		this.repository = packageBag.repository;
		super.setRepository(repository);
	}

	public RPackage() {
		super(RLanguage.instance);
	}
	
	public RPackage(RPackageDto dto, RRepository repository, Submission submission, User user) {
		super(RLanguage.instance, dto, submission, user);
		this.repository = repository;
		this.suggests = dto.getSuggests();
		this.systemRequirements = dto.getSystemRequirements();
		this.md5sum = dto.getMd5sum();
		this.license = dto.getLicense();
		this.depends = dto.getDepends();
		this.imports = dto.getImports();
	}

	public RPackage(int id, RRepository repository, User user, String name,
			String description, String author, String license, String source,
			String title, String md5sum, boolean active, boolean deleted) {
		super(RLanguage.instance, id, repository, user, name, description, author, source, title, active,deleted);
		this.license = license;
		this.md5sum = md5sum;
		this.repository = repository;
		super.setRepository(repository);
	}
	
	public RPackage(int id, RRepository repository, User user, String name,
			String description, String author, String depends, String imports,
			String suggests, String systemRequirements, String license,
			String url, String source, String title, String md5sum, boolean active, boolean deleted,
			Submission submission)
	{
		/**
		 * Technology technology, int id, Repository repository, User user, String name,
			String description, String author,
			String url, String source, String title, boolean active, boolean deleted,
			Submission submission, Set<PackageEvent> packageEvents
		 */
		super(RLanguage.instance, id, repository, user, name, description, author, 
				url, source, title, active, deleted, submission);
		this.depends = depends;
		this.imports = imports;
		this.suggests = suggests;
		this.systemRequirements = systemRequirements;
		this.license = license;
		this.md5sum = md5sum;
		this.repository = repository;
		super.setRepository(repository);
	}
	
	public String getDepends() {
		return this.depends;
	}

	public void setDepends(String depends) {
		this.depends = depends;
	}
	
	public String getImports()	{
		return this.imports;
	}

	public void setImports(String imports) {
		this.imports = imports;
	}
	
	public String getSuggests()	{
		return this.suggests;
	}

	public void setSuggests(String suggests) {
		this.suggests = suggests;
	}
	
	public String getSystemRequirements() {
		return this.systemRequirements;
	}

	public void setSystemRequirements(String systemRequirements) {
		this.systemRequirements = systemRequirements;
	}
	
	public String getLicense() {
		return this.license;
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
	
	public int getId() {
		return id;
	}
	
	@Transient
	public List<Vignette> getVignettes() {
		return this.vignettes;
	}
	
	@Override
	public void setRepository(Repository<?, ?> repository) {
		if(repository.getTechnology() != RLanguage.instance)
			throw new IllegalStateException("Attempted to use incompatible repository with the package: " + toString());
		super.setRepository(repository);
		setRepository((RRepository) repository);
	}
	
	public void setRepository(RRepository repository) {
		this.repository = repository;
	}
		
	public RRepository getRepository() {
		return repository;
	}

	@Override
	public RPackageDto createDto() {
		return new RPackageDto(this);
	}
	
}
