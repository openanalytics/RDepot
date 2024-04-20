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
public class SubmissionTestData {
	private String apiPackagesPath;
	private String submissionId;
	private String submissionIdRepositoryMaintainer;
	private String submissionIdPackageMaintainer;
	private String submissionIdUser;
	private String submissionIdToAccept;
	private String submissionIdAccepted;
	private String submissionIdToCancel;
	private final String packageId;
	private final String packageNameToDownload;
	private final String pdfPath;	
	private int getEndpointNewEventsAmount;
	private int deleteEndpointNewEventsAmount;
	private int changeEndpointNewEventsAmount;
	private int postEndpointNewEventsAmount;
	private List<String> states;
	private int packageIdInt;
	private List<String> technologies;
	private List<String> repositories;
	private String fromDate;
	private String toDate;
	private String search;
}
