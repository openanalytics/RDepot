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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import eu.openanalytics.rdepot.base.api.v2.dtos.UserSettingsDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents personal user settings.
 * They will mostly take effect in the user interface
 * that will be connected to this API.
 */
@Getter
@Setter
@Entity
@Table(name = "user_settings", schema = "public")
public class UserSettings extends Resource {

	@Column(name = "language", nullable = false)
	private String language;
	
	@Column(name = "theme", nullable = false)
	private String theme;
	
	@Column(name = "page_size", nullable = false)
	private int pageSize;
	
	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;
	
	public UserSettings() {
		super(InternalTechnology.instance, ResourceType.USER_SETTINGS);
	}
	
	public UserSettings(boolean deleted, String language, String theme, int pageSize) {
		this();		
		this.deleted = deleted;
		this.language = language;
		this.theme = theme;
		this.pageSize = pageSize;
	}
	
	public UserSettings(int id, boolean deleted, String language, String theme, int pageSize) {
		this();
		this.id = id;
		this.deleted = deleted;
		this.language = language;
		this.theme = theme;
		this.pageSize = pageSize;
	}
	
	public UserSettings(int id, boolean deleted, String language, String theme, int pageSize, User user) {
		this();
		this.id = id;
		this.deleted = deleted;
		this.language = language;
		this.theme = theme;
		this.pageSize = pageSize;
		this.user = user;
	}
	
	public UserSettings(UserSettingsDto dto) {
		this();
		this.id = dto.getId();
		this.deleted = dto.isDeleted();
		this.language = dto.getLanguage();
		this.theme = dto.getTheme();
		this.pageSize = dto.getPageSize();
	}
	
	public UserSettings(UserSettings that) {
		this();
		this.id = that.id;
		this.deleted = that.deleted;
		this.language = that.language;
		this.theme = that.theme;
		this.pageSize = that.pageSize;
		this.user = that.user;
	}

	@Override
	public String toString() {
		return String.format("User settings (user: %d, %s, language: %s, theme: %s, pageSize: %d)",
				user.getId(), user.getLogin(), language, theme, pageSize);		
	}
}
