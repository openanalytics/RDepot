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
package eu.openanalytics.rdepot.base.mirroring.pojos;

import java.util.HashSet;
import java.util.Set;

import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.base.technology.Technology;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Specifies the repository that will server as a mirror.
 * @param <P> type of {@link MirroredPackage Mirrored Packages}
 * @param <M> type of {@link Mirror}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class MirroredRepository<P extends MirroredPackage, M extends Mirror<P>> {
	protected String name;
	protected String publicationUri;
	protected String serverAddress;
	protected Boolean deleted = false;
	protected Boolean published = true;
	protected Set<M> mirrors = new HashSet<>();
	public abstract Technology getTechnology();
}
