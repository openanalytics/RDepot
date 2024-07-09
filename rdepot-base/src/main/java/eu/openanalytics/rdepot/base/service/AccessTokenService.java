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
package eu.openanalytics.rdepot.base.service;

import eu.openanalytics.rdepot.base.api.v2.dtos.CreateAccessTokenDto;
import eu.openanalytics.rdepot.base.daos.AccessTokenDao;
import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.time.DateProvider;
import java.nio.CharBuffer;
import java.security.SecureRandom;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

@org.springframework.stereotype.Service
public class AccessTokenService extends Service<AccessToken> {

    private final AccessTokenDao accessTokenDao;
    private final PasswordEncoder encoder;

    @Value("${access-token.length}")
    private String lengthOfAccessToken;

    @Value("${access-token.allowed-characters}")
    private String allowedCharacters;

    @Value("${access-token.lifetime-default}")
    private String lifetimeOfToken;

    @Value("${access-token.lifetime-configurable}")
    private Boolean lifetimeConfigurable;

    public AccessTokenService(AccessTokenDao accessTokenDao, PasswordEncoder encoder) {
        super(accessTokenDao);
        this.accessTokenDao = accessTokenDao;
        this.encoder = encoder;
    }

    public AccessToken convertRequestBody(CreateAccessTokenDto dto, User requester) throws NumberFormatException {
        int lifetime = Integer.parseInt(lifetimeOfToken);

        if (lifetimeConfigurable
                && dto.getLifetime() != null
                && !dto.getLifetime().isEmpty())
            lifetime = Integer.parseInt(dto.getLifetime().split("\\.")[0]);

        AccessToken accessToken = new AccessToken();
        accessToken.setName(dto.getName());
        accessToken.setCreationDate(DateProvider.now());
        accessToken.setExpirationDate(DateProvider.now().plus(lifetime, ChronoUnit.DAYS));
        accessToken.setUser(requester);
        accessToken.setActive(true);

        return accessToken;
    }

    @Override
    public AccessToken create(AccessToken token) throws CreateEntityException {
        int size = Integer.parseInt(lengthOfAccessToken);
        final char[] allAllowed = allowedCharacters.toCharArray();

        char[] plainToken = new char[size];

        SecureRandom random = new SecureRandom();
        for (int i = 0; i < size; i++) {
            plainToken[i] = allAllowed[random.nextInt(Integer.MAX_VALUE) % allAllowed.length];
        }

        token.setValue(encoder.encode(CharBuffer.wrap(plainToken)));
        AccessToken accessToken = super.create(token);

        accessToken.setPlainValue(CharBuffer.wrap(plainToken).toString());

        return accessToken;
    }

    public boolean verifyToken(CharSequence token, int userId) {
        List<AccessToken> userTokens = accessTokenDao.findByUserId(userId);
        for (AccessToken userToken : userTokens) {
            if (encoder.matches(token, userToken.getValue())
                    && (DateProvider.now().isBefore(userToken.getExpirationDate())
                            || DateProvider.now().equals(userToken.getExpirationDate()))
                    && userToken.isActive()) {
                return true;
            }
        }
        return false;
    }
}
