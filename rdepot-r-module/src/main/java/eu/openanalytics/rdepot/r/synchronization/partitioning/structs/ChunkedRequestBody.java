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
package eu.openanalytics.rdepot.r.synchronization.partitioning.structs;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RequiredArgsConstructor
public class ChunkedRequestBody {
    @NonNull
    final RequestBodyFirstChunk firstChunk;

    @NonNull
    final List<RequestBodyChunk> secondAndOtherChunks;

    @Getter
    @NonNull
    private final Map<File, String> originalFileNames;

    final int versionBefore;

    public List<MultiValueMap<String, Object>> otherChunksToMaps(String id) {
        final List<MultiValueMap<String, Object>> maps = new LinkedList<>();

        int versionForCurrentChunk = versionBefore + 1; // version +1 because the first chunk has already elevated it
        int currentPage = 2;
        for (final RequestBodyChunk chunk : secondAndOtherChunks) {
            maps.add(chunkToMap(chunk, id, versionForCurrentChunk++, currentPage++));
        }

        return maps;
    }

    private MultiValueMap<String, Object> chunkToMap(
            RequestBodyChunk chunk, String id, int currentVersion, int currentPage) {
        final int allPages = secondAndOtherChunks.size() + 1;
        final MultiValueMap<String, Object> chunkMap = new LinkedMultiValueMap<>();
        chunkMap.add("id", id);
        chunkMap.add("version_before", String.valueOf(currentVersion));
        chunkMap.add("version_after", String.valueOf(currentVersion + 1));
        chunkMap.add("page", currentPage + "/" + allPages);
        chunkMap.put("files", Arrays.asList(chunk.files.toArray()));
        chunkMap.put("files_archive", Arrays.asList(chunk.filesArchive.toArray()));
        chunkMap.add("paths", chunk.paths);
        chunkMap.add("paths_archive", chunk.pathsArchive);
        chunkMap.add("checksums", chunk.checksums);

        return chunkMap;
    }

    public MultiValueMap<String, Object> firstChunkToMap() {
        final String id = ""; // Will be known only for second and later chunks
        final MultiValueMap<String, Object> chunkMap = chunkToMap(firstChunk, id, versionBefore, 1);

        chunkMap.put(
                "to_delete",
                firstChunk.getToDelete().stream().map(f -> (Object) f).toList());
        chunkMap.put(
                "to_delete_archive",
                firstChunk.getToDeleteArchive().stream().map(f -> (Object) f).toList());
        chunkMap.add("to_delete_paths", firstChunk.getToDeletePaths());
        chunkMap.add("to_delete_paths_archive", firstChunk.getToDeletePathsArchive());

        return chunkMap;
    }
}
