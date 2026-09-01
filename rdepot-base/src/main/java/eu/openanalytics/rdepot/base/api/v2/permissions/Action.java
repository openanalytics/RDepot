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
package eu.openanalytics.rdepot.base.api.v2.permissions;

import lombok.Getter;

@Getter
public enum Action {
    ACCEPT("accept"),
    ACTIVATE("activate"),
    CANCEL("cancel"),
    CREATE("create"),
    DEACTIVATE("deactivate"),
    DELETE_HARD("delete.hard"),
    DELETE_SOFT("delete.soft"),
    EDIT("edit"),
    GENERATE_MANUAL("generateManual"),
    LIST("list"),
    LIST_ALL("list.all"),
    LIST_MY("list.my"),
    PUBLISH("publish"),
    REJECT("reject"),
    REPLACE_PACKAGE("replacePackage"),
    REPUBLISH("republish"),
    SUBMIT_AUTO_APPROVE("submit.autoApprove"),
    UNPUBLISH("unpublish");

    final String value;

    Action(String value) {
        this.value = value;
    }
}
