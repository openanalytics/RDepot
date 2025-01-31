/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.base.api.v2.hateoas.linking;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;

/**
 * {@link Link} extended with a list of modifiable properties.
 * It is usually a PATCH request link
 * that also specifies which properties can be changed by the requester.
 */
public class LinkWithModifiableProperties extends Link {

    @Serial
    private static final long serialVersionUID = -6116942776233062885L;

    private final List<String> modifiableProperties;
    private final String href;
    private transient LinkRelation rel;
    private final String type;

    public LinkWithModifiableProperties(Link baseLink, String... modifiableProperties) {
        super();
        this.href = baseLink.getHref();
        this.rel = baseLink.getRel();
        this.type = baseLink.getType();
        this.modifiableProperties = Arrays.asList(modifiableProperties);
    }

    @JsonProperty
    public List<String> getModifiableProperties() {
        return modifiableProperties;
    }

    @JsonProperty
    @Override
    public @NonNull String getHref() {
        return this.href;
    }

    @JsonProperty
    @Override
    public @NonNull LinkRelation getRel() {
        return this.rel;
    }

    @JsonProperty
    @Override
    public String getType() {
        return this.type;
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException, ClassNotFoundException, NotSerializableException {
        out.defaultWriteObject();
        out.writeObject(getRel());
    }

    private void readObject(ObjectInputStream in) throws IOException, NotSerializableException, ClassNotFoundException {
        in.defaultReadObject();
        rel = (LinkRelation) in.readObject();
    }
}
