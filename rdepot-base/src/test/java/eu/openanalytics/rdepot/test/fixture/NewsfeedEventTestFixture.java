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
package eu.openanalytics.rdepot.test.fixture;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class NewsfeedEventTestFixture {

    public static List<NewsfeedEvent> GET_FIXTURE_EVENTS(User user) {
        List<NewsfeedEvent> events = new ArrayList<>();
        Package packageBag = PackageTestFixture.GET_EXAMPLE_PACKAGE();

        events.add(new NewsfeedEvent(user, NewsfeedEventType.CREATE, packageBag));
        events.add(new NewsfeedEvent(user, NewsfeedEventType.UPLOAD, packageBag));
        events.add(new NewsfeedEvent(user, NewsfeedEventType.UPDATE, packageBag));
        events.add(new NewsfeedEvent(user, NewsfeedEventType.REPUBLISH, packageBag.getRepository()));

        for (int i = 0; i < events.size(); i++) events.get(i).setId(i + 1);

        return events;
    }

    public static Page<NewsfeedEvent> GET_EXAMPLE_EVENTS_PAGED(User user) {
        return new PageImpl<>(GET_FIXTURE_EVENTS(user));
    }

    public static NewsfeedEvent GET_FIXTURE_EVENT(User user) {
        return GET_FIXTURE_EVENTS(user).get(0);
    }
}
