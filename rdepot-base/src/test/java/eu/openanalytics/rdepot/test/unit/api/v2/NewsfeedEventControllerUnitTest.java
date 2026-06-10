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
package eu.openanalytics.rdepot.test.unit.api.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2NewsfeedEventController;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.newsfeed.NewsfeedEventsRolesFiltration;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import eu.openanalytics.rdepot.test.fixture.NewsfeedEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ContextConfiguration(classes = {ApiTestConfig.class})
@WebMvcTest(ApiV2NewsfeedEventController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({ApiTestConfig.class})
public class NewsfeedEventControllerUnitTest extends ApiV2ControllerUnitTest {

    private static final String EXAMPLE_NEWSFEED_EVENTS_PATH = JSON_PATH + "/example_newsfeed_events.json";
    private static final String EXAMPLE_NEWSFEED_EVENT_PATH = JSON_PATH + "/example_newsfeed_event.json";
    private static final String ERROR_EVENT_NOT_FOUND_PATH = JSON_PATH + "/error_event_notfound.json";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NewsfeedEventsRolesFiltration eventsRolesFiltering;

    private User user;

    @BeforeEach
    public void initEach() {
        user = UserTestFixture.GET_REGULAR_USER();
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getEvents() throws Exception {
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));

        Page<NewsfeedEvent> eventsPage = NewsfeedEventTestFixture.GET_EXAMPLE_EVENTS_PAGED(user);
        when(newsfeedEventService.findEventsByParameters(
                        any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(eventsPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/events").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(java.nio.file.Path.of(EXAMPLE_NEWSFEED_EVENTS_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getEvent() throws Exception {
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));

        NewsfeedEvent event = NewsfeedEventTestFixture.GET_FIXTURE_EVENT(user);
        final int ID = event.getId();

        when(newsfeedEventService.findById(ID)).thenReturn(Optional.of(event));
        when(securityMediator.canSeeEvent(event, user)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/events/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(java.nio.file.Path.of(EXAMPLE_NEWSFEED_EVENT_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void getRepositoryMaintainer_returns404_whenRepositoryMaintainerIsNotFound() throws Exception {
        final int ID = 123;

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(newsfeedEventService.findById(ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/events/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_EVENT_NOT_FOUND_PATH))));
    }

    @Test
    public void getRepositoryMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/events/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser
    public void getEvents_returns403_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/events/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }
}
