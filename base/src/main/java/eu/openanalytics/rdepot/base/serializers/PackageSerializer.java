package eu.openanalytics.rdepot.base.serializers;
/**
 * R Depot
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

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;

public class PackageSerializer extends StdSerializer<Package<?,?>> {
	   private static final long serialVersionUID = 7966780776110275696L;
	public PackageSerializer(Class t) {
	      super(t);
	   }
	   public PackageSerializer() {
	      this(Repository.class);
	   }

	@Override
	public void serialize(Package<?, ?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		
	
		jgen.writeStartObject();
	      jgen.writeNumberField("id", value.getId());
	      jgen.writeStringField("version", value.getVersion());
//	      @JsonSerialize(using = RepositoryShortSerializer.class);
//	      RRepository repository = value.getRepository(); 
//	      jgen.writeObjectField("repository", value.getRepositoryShort());
//	      jgen.writeObject("submission", value.getSubmission());
	      jgen.writeStringField("name", value.getName());
	      jgen.writeStringField("description", value.getDescription());
	      jgen.writeStringField("author", value.getAuthor());
//	      jgen.writeStringField("depends", value.is());
//	      jgen.writeStringField("imports", value.getI());
//	      jgen.writeStringField("suggests", value.getAuthor());
//	      jgen.writeStringField("systemRequirements", value.getS());
//	      jgen.writeStringField("license", value.getL());
	      jgen.writeStringField("title", value.getTitle());
	      jgen.writeStringField("url", value.getUrl());
	      jgen.writeStringField("source", value.getSource());
//	      jgen.writeStringField("md5sum", value.getM());
	      jgen.writeBooleanField("active", value.isActive());
	      jgen.writeBooleanField("deleted", value.isDeleted());
//	      jgen.writeStringField("packageEvents", value.getSource());
	      jgen.writeEndObject();
	}
	}