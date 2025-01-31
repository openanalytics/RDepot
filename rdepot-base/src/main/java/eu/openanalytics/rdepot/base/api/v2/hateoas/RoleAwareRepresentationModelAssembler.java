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
package eu.openanalytics.rdepot.base.api.v2.hateoas;

import eu.openanalytics.rdepot.base.entities.User;
import lombok.Getter;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

/**
 * {@link RepresentationModelAssembler} that assembles models
 * taking user privileges into account.
 */
public interface RoleAwareRepresentationModelAssembler<T, D extends RepresentationModel<?>>
        extends RepresentationModelAssembler<T, D> {

    @Getter
    enum HTTP_METHODS {
        PUT("PUT"),
        PATCH("PATCH"),
        GET("GET"),
        DELETE("DELETE");

        final String value;

        HTTP_METHODS(String value) {
            this.value = value;
        }
    }

    /**
     * Creates {@link RepresentationModel} with links to actions
     * that {@link User} is allowed to perform.
     */
    D toModel(T entity, User user);

    /**
     * Creates {@link RepresentationModelAssembler} with a fixed user assigned.
     * Such an assembler must <b>not</b> be a bean as it is only valid for a specific request.
     *
     * <h2>Why it exists?</h2>
     * <p>There are <i>situations</i> when the {@link #toModel(Object, User)}
     * method cannot be used.
     * For example, {@link PagedResourcesAssembler} makes methods responsible
     * for creating a list out of rendered {@link EntityModel entity models} private.
     * Hence, only {@link #toModel(Object)} method (without {@link User} object) will ever be called.
     * Therefore, we will not be able to provide links for user-specific actions.</p>
     *
     * <h2>How it works?</h2>
     * <p>In order to solve this problem this method creates a copy of the assembler bean object.
     * Such a copy, however, has a {@link User} object injected
     * as an object property which is then used in {@link #toModel(Object)} method.</p>
     * <p>In order to create a viable instance, a separate constructor has to be provided.
     * It is <b>highly recommended</b>
     * that such a constructor remains private so Spring will not attempt to instantiate it.</p>
     */
    RepresentationModelAssembler<T, D> assemblerWithUser(User user);
}
