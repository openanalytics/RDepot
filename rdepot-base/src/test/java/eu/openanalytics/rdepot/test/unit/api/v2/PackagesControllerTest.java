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
package eu.openanalytics.rdepot.test.unit.api.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2PackageController;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = {ApiTestConfig.class})
@WebMvcTest(ApiV2PackageController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({ApiTestConfig.class})
public class PackagesControllerTest extends ApiV2ControllerUnitTest {

    public static final String EXAMPLE_COMMON_PACKAGE_PATH = JSON_PATH + "/example_common_package.json";
    public static final String ERROR_PACKAGE_NOT_FOUND = JSON_PATH + "/error_package_notfound.json";

    private Optional<User> user;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MappingJackson2HttpMessageConverter jsonConverter;

    @Autowired
    MessageSource messageSource;

    @Autowired
    WebApplicationContext webApplicationContext;

    @BeforeEach
    public void initEach() {
        user = Optional.of(UserTestFixture.GET_REGULAR_USER());
    }

    @Test
    public void getAllPackages_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/packages").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getPackage() throws Exception {
        final Package packageBag = PackageTestFixture.GET_EXAMPLE_PACKAGE();
        final Integer ID = 1234;
        final PackageDto packageDto = PackageTestFixture.GET_EXAMPLE_PACKAGE_DTO();

        when(userService.findActiveByLogin("user")).thenReturn(user);
        when(commonPackageService.findById(ID)).thenReturn(Optional.of(packageBag));
        when(commonPackageDtoConverter.convertEntityToDto(any())).thenReturn(packageDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/packages/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_COMMON_PACKAGE_PATH))));
    }

    @Test
    public void getPackage_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/packages/" + 1234)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getPackage_returns404_whenRepositoryIsNotFound() throws Exception {

        when(commonRepositoryService.findById(any(Integer.class))).thenReturn(Optional.ofNullable(null));
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(UserTestFixture.GET_ADMIN()));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/packages/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_PACKAGE_NOT_FOUND))));
    }
}
