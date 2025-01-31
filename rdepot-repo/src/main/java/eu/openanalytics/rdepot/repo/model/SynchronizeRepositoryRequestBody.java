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
package eu.openanalytics.rdepot.repo.model;

import eu.openanalytics.rdepot.repo.exception.InvalidRequestPageNumberException;
import eu.openanalytics.rdepot.repo.hash.model.HashMethod;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO of Synchronization request retrieved from the client
 */
@Getter
public abstract class SynchronizeRepositoryRequestBody {
    String page;
    String repository;

    @Setter
    String id;

    String versionBefore;
    String versionAfter;
    Map<String, String> checksums;
    HashMethod hashMethod;
    MultipartFile[] filesToUpload;
    String[] filesToDelete;

    public abstract Technology getTechnology();

    protected SynchronizeRepositoryRequestBody(
            String page,
            String repository,
            String id,
            String versionBefore,
            String versionAfter,
            Map<String, String> checksums,
            HashMethod hashMethod,
            MultipartFile[] filesToUpload,
            String[] filesToDelete) {
        this.page = page;
        this.repository = repository;
        this.id = id;
        this.versionAfter = versionAfter;
        this.versionBefore = versionBefore;
        this.checksums = checksums;
        this.hashMethod = hashMethod;
        this.filesToDelete = filesToDelete == null ? new String[0] : filesToDelete;
        this.filesToUpload = filesToUpload == null ? new MultipartFile[0] : filesToUpload;
    }

    public boolean isFirstChunk() throws InvalidRequestPageNumberException {
        String[] pageStr = getPage().split("/");
        if (pageStr.length == 2) {
            return Objects.equals(pageStr[0], "1");
        } else {
            throw new InvalidRequestPageNumberException(getPage());
        }
    }

    public boolean isLastChunk() throws InvalidRequestPageNumberException {
        String[] pageStr = getPage().split("/");
        if (pageStr.length == 2) {
            return Objects.equals(pageStr[0], pageStr[1]);
        } else {
            throw new InvalidRequestPageNumberException(getPage());
        }
    }
}
