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

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2StatusController;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ContextConfiguration(classes = {ApiTestConfig.class})
@WebMvcTest(ApiV2StatusController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({ApiTestConfig.class})
public class ApiV2StatusControllerUnitTest extends ApiV2ControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MessageSource messageSource;

    @Autowired
    ApiV2StatusController apiV2StatusController;

    private static final String TEST_REPO_URL = "http://localhost:8017/testrepo1";
    private static final String JSON_PATH = "src/test/resources/unit/jsons";
    private static final String MALFORMED_POST = JSON_PATH + "/error_malformed_address.json";
    private static final String UNHEALTHY_SERVERADDRESS = JSON_PATH + "/error_unhealthy_serveraddress.json";
    private static final String USED_ADDRESS = JSON_PATH + "/error_used_address.json";

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void checkServerAddress_returnsSuccess_whenAddressIsValid() throws Exception {
        doReturn(Optional.empty()).when(repositoryService).findByServerAddress(TEST_REPO_URL);
        doReturn(true).when(serverAddressHealthcheckService).isHealthy(new URL(TEST_REPO_URL));
        final String EXAMPLE_HEALTHCHECK_OK = JSON_PATH + "/example_serveraddress_ok.json";
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/check-server-address")
                        .content("{\"serverAddress\": \"" + TEST_REPO_URL + "\"}")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_HEALTHCHECK_OK))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void checkServerAddress_returns422_whenServerAddressIsInvalid() throws Exception {
        doReturn(Optional.empty()).when(repositoryService).findByServerAddress(TEST_REPO_URL);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/check-server-address")
                        .content("{\"serverAddress\": \"testtestestjfldks\"}")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(Files.readString(Path.of(MALFORMED_POST))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void checkServerAddress_returns422_whenRemoteCheckDoesNotPass() throws Exception {
        doReturn(Optional.empty()).when(repositoryService).findByServerAddress(TEST_REPO_URL);
        doReturn(false).when(serverAddressHealthcheckService).isHealthy(new URL("http://localhost:8017/testrepo1"));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/check-server-address")
                        .content("{\"serverAddress\": \"" + TEST_REPO_URL + "\"}")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(Files.readString(Path.of(UNHEALTHY_SERVERADDRESS))));
    }

    @Test
    @WithMockUser(authorities = {"user"})
    public void checkServerAddress_returns403_whenUserIsNotAnAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/check-server-address")
                        .content("{\"serverAddress\": \"" + TEST_REPO_URL + "\"}")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());
    }
}
