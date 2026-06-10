/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.base.security.backends.idp;

import java.io.Serial;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Transient;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * @author Joe Grandja
 * @author Steve Riesenberg
 * @since 1.1
 */
@Transient
public class DeviceClientAuthenticationToken extends OAuth2ClientAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 731084549787773964L;

    public DeviceClientAuthenticationToken(
            String clientId,
            ClientAuthenticationMethod clientAuthenticationMethod,
            @Nullable Object credentials,
            @Nullable Map<String, Object> additionalParameters) {
        super(clientId, clientAuthenticationMethod, credentials, additionalParameters);
    }

    public DeviceClientAuthenticationToken(
            RegisteredClient registeredClient,
            ClientAuthenticationMethod clientAuthenticationMethod,
            @Nullable Object credentials) {
        super(registeredClient, clientAuthenticationMethod, credentials);
    }
}
