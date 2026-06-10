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

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(value = "oauth2.authorizationserver", havingValue = "true")
public class RDepotTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Resource
    private Environment env;

    private final OidcUserInfoService userInfoService;
    private final AuthorizationServerSettings authorizationServerSettings;

    public RDepotTokenCustomizer(
            OidcUserInfoService userInfoService, AuthorizationServerSettings authorizationServerSettings) {
        this.userInfoService = userInfoService;
        this.authorizationServerSettings = authorizationServerSettings;
    }

    @Override
    public void customize(JwtEncodingContext context) {

        String emailField = env.getProperty("oauth2.email-field", "email");
        String loginField = env.getProperty("oauth2.login-field", "preferred_username");
        String nameField = env.getProperty("oauth2.full-name-field", "name");
        String groupsField = env.getProperty("oauth2.groups-field", "groups");

        if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
            OidcUserInfo userInfo = userInfoService.loadUser(
                    (DefaultOidcUser) context.getPrincipal().getPrincipal());
            context.getClaims().claims(claims -> claims.putAll(userInfo.getClaims()));
        } else if (!context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
            return;
        }

        Authentication authentication = context.getPrincipal();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .collect(Collectors.toList());
        context.getClaims().claim("roles", roles);

        Object principalObj = authentication.getPrincipal();
        if (principalObj instanceof OidcUser oidcUser) {
            context.getClaims().claim(emailField, oidcUser.getEmail());
            context.getClaims().claim(loginField, oidcUser.getPreferredUsername());
            context.getClaims().claim(nameField, oidcUser.getFullName());
            List<String> groups = oidcUser.getClaimAsStringList(groupsField);

            if (groups != null) {

                List<String> normalizedGroups =
                        groups.stream().map(g -> g.replaceFirst("^/", "")).toList();

                context.getClaims().claim(groupsField, normalizedGroups);
            }
        } else if (principalObj instanceof OAuth2User oauth2User) {
            Map<String, Object> attrs = oauth2User.getAttributes();
            if (attrs.containsKey(emailField)) {
                context.getClaims().claim(emailField, String.valueOf(attrs.get(emailField)));
            }
            if (attrs.containsKey(loginField)) {
                context.getClaims().claim(loginField, String.valueOf(attrs.get(loginField)));
            }
            if (attrs.containsKey(nameField)) {
                context.getClaims().claim(nameField, String.valueOf(attrs.get(nameField)));
            }
        } else if (principalObj instanceof UserDetails userDetails) {
            context.getClaims().claim(emailField, userDetails.getUsername() + "@localhost");
        }

        context.getClaims().claim("iss", authorizationServerSettings.getIssuer());
    }
}
