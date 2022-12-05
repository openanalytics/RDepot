package eu.openanalytics.rdepot.base.serializers;
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
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import eu.openanalytics.rdepot.base.entities.Repository;

public class RepositorySerializer extends StdSerializer<Repository<?,?>> {
	   private static final long serialVersionUID = 7966780776110275696L;

	public RepositorySerializer(Class t) {
	      super(t);
	   }
	   public RepositorySerializer() {
	      this(Repository.class);
	   }

	@Override
	public void serialize(Repository<?, ?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		 jgen.writeStartObject();
	      jgen.writeNumberField("id", value.getId());
	      jgen.writeNumberField("version", value.getVersion());
	      jgen.writeStringField("publicationUri", value.getPublicationUri());
	      jgen.writeStringField("name", value.getName());
	      jgen.writeStringField("serverAddress", value.getServerAddress());
	      jgen.writeBooleanField("deleted", value.isDeleted());
	      jgen.writeBooleanField("published", value.isPublished());
	      jgen.writeObjectField("packages", value.getPackages());
//	      jgen.writeObjectField("packageMaintainers", value.getPackageMaintainers());
//	      jgen.writeObjectField("repositoryEvents", value.getRepositoryMaintainers());
	      jgen.writeEndObject();
		
	}
	}