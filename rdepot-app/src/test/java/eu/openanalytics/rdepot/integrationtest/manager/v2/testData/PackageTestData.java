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
package eu.openanalytics.rdepot.integrationtest.manager.v2.testData;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PackageTestData {
    private List<String> technologies;
    private String examplePackageId;
    private String deletedPackageId;
    private String exampleNotExistsPackageId;
    private String toBeActivatedPackageId;
    private List<String> repositories;
    private List<String> submissionStates;
    private String search;
    private List<String> maintainer;
    private boolean deleted;
    private int getEndpointNewEventsAmount;
    private int deleteEndpointNewEventsAmount;
    private int changeEndpointNewEventsAmount;
}
