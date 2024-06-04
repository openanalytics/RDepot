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
package eu.openanalytics.rdepot.repo.test.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import eu.openanalytics.rdepot.repo.model.Technology;
import eu.openanalytics.rdepot.repo.python.storage.PythonFileSystemStorageService;
import eu.openanalytics.rdepot.repo.r.api.CranFileUploadController;
import eu.openanalytics.rdepot.repo.r.storage.implementations.CranFileSystemStorageService;
import eu.openanalytics.rdepot.repo.r.transaction.backup.CranRepositoryBackup;
import eu.openanalytics.rdepot.repo.repository.Repository;
import eu.openanalytics.rdepot.repo.transaction.Transaction;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(CranFileUploadController.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles({"test"})
public class TransactionManagementTest {

    @MockBean
    private CranFileSystemStorageService cranFileSystemStorageService;

    @MockBean
    private PythonFileSystemStorageService pythonFileSystemStorageService;

    @Autowired
    MockMvc mockMvc;

    static final Map<String, String> checksums = new HashMap<>();

    static {
        checksums.put("Benchmarking_0.10.tar.gz", "9a99c2ebefa6d49422ca7893c1f4ead8");
        checksums.put("PACKAGES", "b9edcc805d4a7f435c18ba670d488d19");
        checksums.put("PACKAGES.gz", "c4fd38fcf43cce22fa16f61add39960b");
        checksums.put("usl_2.0.0.tar.gz", "868140a3c3c29327eef5d5a485aee5b6");
        checksums.put("abc_1.3.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.put("accrued_1.2.tar.gz", "70d295115295a4718593f6a39d77add9");
        checksums.put("accrued_1.3.tar.gz", "a05e4ca44438c0d9e7d713d7e3890423");
        checksums.put("PACKAGES_ARCHIVE", "11a6d192748004e42797862505409102");
        checksums.put("PACKAGES_ARCHIVE.gz", "d198474d73e5cc542aa7a0348b104d72");
    }

    private class SubmitFirstChunkCallable implements Callable<MvcResult> {

        private final String filename;
        private final byte[] file;
        private final CountDownLatch latch;

        private final Repository testRepo;
        private final Map<String, String> checksums = new HashMap<>();

        public SubmitFirstChunkCallable(String filename, byte[] file, CountDownLatch latch) {
            this.filename = filename;
            this.file = file;
            this.latch = latch;
            this.testRepo = TEST_REPO;
        }

        public SubmitFirstChunkCallable(String filename, byte[] file, CountDownLatch latch, Repository testRepo) {
            this.filename = filename;
            this.file = file;
            this.latch = latch;
            this.testRepo = testRepo;
        }

        @Override
        public MvcResult call() throws Exception {
            latch.await();
            return mockMvc.perform(MockMvcRequestBuilders.multipart("/r/" + testRepo.getName())
                            .file(new MockMultipartFile(filename, file))
                            .file(new MockMultipartFile(
                                    "checksums",
                                    "",
                                    "application/json",
                                    new JSONObject(checksums).toString().getBytes(StandardCharsets.UTF_8)))
                            .param("version_before", "23")
                            .param("version_after", "24")
                            .param("page", "1/5")
                            .param("id", ""))
                    .andReturn();
        }
    }

    @Test
    public void fourFirstChunksAtOnceForTheSameRepo_onlyOneShouldSucceed() throws Exception {
        final Map<String, byte[]> packages = Map.of(
                "Benchmarking_0.10.tar.gz", TestUtils.readTestPackage("recent/Benchmarking_0.10.tar.gz"),
                "usl_2.0.0.tar.gz", TestUtils.readTestPackage("recent/usl_2.0.0.tar.gz"),
                "accrued_1.3.tar.gz", TestUtils.readTestPackage("archive/accrued_1.3.tar.gz"),
                "abc_1.3.tar.gz", TestUtils.readTestPackage("archive/abc_1.3.tar.gz"));

        doReturn(List.of(TEST_REPO.getName()))
                .when(cranFileSystemStorageService)
                .getAllRepositoryDirectories();
        doReturn(List.of()).when(pythonFileSystemStorageService).getAllRepositoryDirectories();
        doReturn(List.of()).when(cranFileSystemStorageService).getRecentPackagesFromRepository(anyString());
        doReturn(Map.of()).when(cranFileSystemStorageService).getArchiveFromRepository(anyString());
        doReturn(File.createTempFile("TRASH_", "430482309482309"))
                .when(cranFileSystemStorageService)
                .initTrashDirectory(anyString());
        doReturn("23").when(cranFileSystemStorageService).getRepositoryVersion(TEST_REPO.getName());
        doNothing().when(cranFileSystemStorageService).storeAndDeleteFiles(any());
        doNothing().when(cranFileSystemStorageService).boostRepositoryVersion(TEST_REPO.getName());
        doReturn(Technology.R).when(cranFileSystemStorageService).getTechnology();
        doReturn(Technology.PYTHON).when(pythonFileSystemStorageService).getTechnology();

        final CountDownLatch latch = new CountDownLatch(1);
        final ExecutorService executorService = Executors.newFixedThreadPool(packages.size());
        final List<Future<MvcResult>> futures = new LinkedList<>();

        for (Map.Entry<String, byte[]> entry : packages.entrySet()) {
            futures.add(executorService.submit(new SubmitFirstChunkCallable(entry.getKey(), entry.getValue(), latch)));
        }
        latch.countDown();
        int succeded = 0;
        int failed = 0;

        for (Future<MvcResult> f : futures) {
            final MvcResult result = f.get();

            if (result.getResponse().getStatus() == MockHttpServletResponse.SC_OK) succeded++;
            else if (result.getResponse().getStatus() == MockHttpServletResponse.SC_BAD_REQUEST) failed++;
            else fail("Wrong status code returned.");
        }

        assertEquals(1, succeded, "One and only one request should have succeeded.");
        assertEquals(3, failed, "Three requests should have failed.");

        verify(cranFileSystemStorageService).initTrashDirectory(anyString());
        verify(cranFileSystemStorageService, times(2)).getRepositoryVersion(TEST_REPO.getName());
        verify(cranFileSystemStorageService).storeAndDeleteFiles(any());
        verify(cranFileSystemStorageService).boostRepositoryVersion(TEST_REPO.getName());
    }

    @Test
    public void fourFirstChunksAtOnceForDifferentRepos_allShouldSucceed_andBeProcessed() throws Exception {
        final Map<String, byte[]> packages = Map.of(
                "Benchmarking_0.10.tar.gz", TestUtils.readTestPackage("recent/Benchmarking_0.10.tar.gz"),
                "usl_2.0.0.tar.gz", TestUtils.readTestPackage("recent/usl_2.0.0.tar.gz"),
                "accrued_1.3.tar.gz", TestUtils.readTestPackage("archive/accrued_1.3.tar.gz"),
                "abc_1.3.tar.gz", TestUtils.readTestPackage("archive/abc_1.3.tar.gz"));

        final List<Repository> repositories = List.of(
                new Repository(Technology.R, "testrepo123"),
                new Repository(Technology.R, "testrepo234"),
                new Repository(Technology.R, "TESTREPO111"),
                new Repository(Technology.R, "TESTTTTTREPO000"));

        doReturn(repositories.stream().map(Repository::getName).toList())
                .when(cranFileSystemStorageService)
                .getAllRepositoryDirectories();
        doReturn(List.of()).when(pythonFileSystemStorageService).getAllRepositoryDirectories();
        doReturn(Technology.R).when(cranFileSystemStorageService).getTechnology();
        doReturn(Technology.PYTHON).when(pythonFileSystemStorageService).getTechnology();
        doReturn(List.of()).when(cranFileSystemStorageService).getRecentPackagesFromRepository(anyString());
        doReturn(Map.of()).when(cranFileSystemStorageService).getArchiveFromRepository(anyString());
        doReturn(File.createTempFile("TRASH_", "430482309482309"))
                .when(cranFileSystemStorageService)
                .initTrashDirectory(anyString());
        doReturn("23").when(cranFileSystemStorageService).getRepositoryVersion(anyString());
        doNothing().when(cranFileSystemStorageService).storeAndDeleteFiles(any());
        doNothing().when(cranFileSystemStorageService).boostRepositoryVersion(anyString());

        final CountDownLatch latch = new CountDownLatch(1);
        final ExecutorService executorService = Executors.newFixedThreadPool(packages.size());
        final List<Future<MvcResult>> futures = new LinkedList<>();

        int i = 0;
        for (Map.Entry<String, byte[]> entry : packages.entrySet()) {
            futures.add(executorService.submit(
                    new SubmitFirstChunkCallable(entry.getKey(), entry.getValue(), latch, repositories.get(i))));
            i++;
        }

        latch.countDown();
        int succeded = 0;

        for (Future<MvcResult> f : futures) {
            final MvcResult result = f.get();

            if (result.getResponse().getStatus() == MockHttpServletResponse.SC_OK) succeded++;
            else fail("Wrong status code returned.");
        }

        assertEquals(4, succeded, "All 4 requests should have succeeded.");
        verify(cranFileSystemStorageService, times(4)).initTrashDirectory(anyString());
        verify(cranFileSystemStorageService, times(8)).getRepositoryVersion(anyString());
        verify(cranFileSystemStorageService, times(4)).storeAndDeleteFiles(any());
        verify(cranFileSystemStorageService, times(4)).boostRepositoryVersion(anyString());
    }

    @Test
    public void secondDetachedChunkForFreeRepo_shouldFail() throws Exception {
        final String transactionTestId = "test321498320948";

        mockMvc.perform(MockMvcRequestBuilders.multipart("/r/testrepo1")
                        .file(new MockMultipartFile(
                                "Benchmarking_0.10.tar.gz",
                                TestUtils.readTestPackage("recent/Benchmarking_0.10.tar.gz")))
                        .param("version_before", "23")
                        .param("version_after", "24")
                        .param("page", "3/5")
                        .param("id", transactionTestId))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void firstChunkForBusyRepoWithExpiredTransaction_shouldSucceed_andAbortOngoingTransaction()
            throws Exception {
        final Transaction transaction = new Transaction(TEST_REPO.getName(), TEST_ID, 2);
        transaction.incrementChunkNumber();
        TestConfig.ACTIVE_BY_ID.put(TEST_ID, transaction);
        TestConfig.ACTIVE_BY_REPO.put(TEST_REPO, transaction);
        FieldUtils.writeField(
                transaction,
                "lastChunkArrivalTime",
                Instant.now().atZone(ZoneOffset.UTC).withYear(2018).toInstant(),
                true);
        TestConfig.CRAN_BACKUPS.put(
                transaction,
                new CranRepositoryBackup(List.of(), List.of(), File.createTempFile("TRASH_", "OLD1234"), "23"));

        // abort previous transaction
        doNothing().when(cranFileSystemStorageService).removeNonExistingArchivePackagesFromRepo(anyList(), anyString());
        doNothing().when(cranFileSystemStorageService).removeNonExistingPackagesFromRepo(anyList(), anyString());
        doNothing().when(cranFileSystemStorageService).restoreTrash(any());
        doNothing().when(cranFileSystemStorageService).setRepositoryVersion(eq(TEST_REPO.getName()), anyString());
        doNothing().when(cranFileSystemStorageService).generateArchiveRds(TEST_REPO.getName());

        // handle chunk
        doReturn(List.of(TEST_REPO.getName()))
                .when(cranFileSystemStorageService)
                .getAllRepositoryDirectories();
        doReturn(List.of()).when(pythonFileSystemStorageService).getAllRepositoryDirectories();
        doReturn(List.of()).when(cranFileSystemStorageService).getRecentPackagesFromRepository(anyString());
        doReturn(Map.of()).when(cranFileSystemStorageService).getArchiveFromRepository(anyString());
        doReturn(File.createTempFile("TRASH_", "430482309482309"))
                .when(cranFileSystemStorageService)
                .initTrashDirectory(anyString());
        doReturn("23").when(cranFileSystemStorageService).getRepositoryVersion(TEST_REPO.getName());
        doNothing().when(cranFileSystemStorageService).storeAndDeleteFiles(any());
        doNothing().when(cranFileSystemStorageService).boostRepositoryVersion(TEST_REPO.getName());
        doReturn(Technology.R).when(cranFileSystemStorageService).getTechnology();
        doReturn(Technology.PYTHON).when(pythonFileSystemStorageService).getTechnology();

        mockMvc.perform(MockMvcRequestBuilders.multipart("/r/" + TEST_REPO.getName())
                        .file(new MockMultipartFile(
                                "Benchmarking_0.10.tar.gz",
                                TestUtils.readTestPackage("recent/Benchmarking_0.10.tar.gz")))
                        .file(new MockMultipartFile(
                                "checksums",
                                "",
                                "application/json",
                                new JSONObject(checksums).toString().getBytes(StandardCharsets.UTF_8)))
                        .param("version_before", "23")
                        .param("version_after", "24")
                        .param("page", "1/5")
                        .param("id", ""))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        // abort previous transaction - verifications
        verify(cranFileSystemStorageService).removeNonExistingArchivePackagesFromRepo(anyList(), anyString());
        verify(cranFileSystemStorageService).removeNonExistingPackagesFromRepo(anyList(), anyString());
        verify(cranFileSystemStorageService).restoreTrash(any());
        verify(cranFileSystemStorageService).setRepositoryVersion(eq(TEST_REPO.getName()), anyString());
        verify(cranFileSystemStorageService).generateArchiveRds(TEST_REPO.getName());

        // process chunk
        verify(cranFileSystemStorageService).initTrashDirectory(anyString());
        verify(cranFileSystemStorageService, times(2)).getRepositoryVersion(TEST_REPO.getName());
        verify(cranFileSystemStorageService).storeAndDeleteFiles(any());
        verify(cranFileSystemStorageService).boostRepositoryVersion(TEST_REPO.getName());
    }

    private static final String TEST_ID = "abc1234";
    private static final Repository TEST_REPO = new Repository(Technology.R, "testrepo123");

    @BeforeEach
    public void setUp() {
        TestConfig.ACTIVE_BY_REPO.clear();
        TestConfig.ACTIVE_BY_ID.clear();
    }

    @Test
    public void secondDetachedChunkForBusyRepo_shouldFail() throws Exception {
        final String transactionTestId = "2323323223";
        mockMvc.perform(MockMvcRequestBuilders.multipart("/r/" + TEST_REPO)
                        .file(new MockMultipartFile(
                                "filesToUpload",
                                "Benchmarking_0.10.tar.gz",
                                "application/gzip",
                                TestUtils.readTestPackage("recent/Benchmarking_0.10.tar.gz")))
                        .param("version_before", "23")
                        .param("version_after", "24")
                        .param("page", "3/5")
                        .param("id", transactionTestId))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void thirdOutOfOrderAttachedChunkForBusyRepo_shouldFail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/r/" + TEST_REPO.getName())
                        .file(new MockMultipartFile(
                                "Benchmarking_0.10.tar.gz",
                                TestUtils.readTestPackage("recent/Benchmarking_0.10.tar.gz")))
                        .param("version_before", "23")
                        .param("version_after", "24")
                        .param("page", "4/5")
                        .param("id", TEST_ID))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void lastAttachedChunkForBusyRepo_shouldSucceed_andBeProcessedAndFinishTransaction() throws Exception {
        final Transaction transaction = new Transaction(TEST_REPO.getName(), TEST_ID, 2);
        transaction.incrementChunkNumber();
        TestConfig.ACTIVE_BY_ID.put(TEST_ID, transaction);
        TestConfig.ACTIVE_BY_REPO.put(TEST_REPO, transaction);

        doReturn("23").when(cranFileSystemStorageService).getRepositoryVersion(TEST_REPO.getName());
        doNothing().when(cranFileSystemStorageService).storeAndDeleteFiles(any());
        doNothing().when(cranFileSystemStorageService).boostRepositoryVersion(TEST_REPO.getName());
        doNothing().when(cranFileSystemStorageService).handleLastChunk(any(), anyString());
        doNothing().when(cranFileSystemStorageService).emptyTrash(TEST_REPO.getName(), TEST_ID);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/r/" + TEST_REPO.getName())
                        .file(new MockMultipartFile(
                                "filesToUpload",
                                "Benchmarking_0.10.tar.gz",
                                "application/gzip",
                                TestUtils.readTestPackage("recent/Benchmarking_0.10.tar.gz")))
                        .file(new MockMultipartFile(
                                "checksums",
                                "",
                                "application/json",
                                new JSONObject(checksums).toString().getBytes(StandardCharsets.UTF_8)))
                        .param("version_before", "23")
                        .param("version_after", "24")
                        .param("page", "2/2")
                        .param("id", TEST_ID))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        verify(cranFileSystemStorageService).storeAndDeleteFiles(any());
        verify(cranFileSystemStorageService).boostRepositoryVersion(TEST_REPO.getName());
        verify(cranFileSystemStorageService).handleLastChunk(any(), anyString());
        verify(cranFileSystemStorageService).emptyTrash(TEST_REPO.getName(), TEST_ID);
    }

    @Test
    public void secondAttachedChunkForBusyRepo_shouldSucceed_andBeProcessedAndNotFinishTransaction() throws Exception {
        final Transaction transaction = new Transaction(TEST_REPO.getName(), TEST_ID, 3);
        transaction.incrementChunkNumber();
        TestConfig.ACTIVE_BY_ID.put(TEST_ID, transaction);
        TestConfig.ACTIVE_BY_REPO.put(TEST_REPO, transaction);

        doReturn("23").when(cranFileSystemStorageService).getRepositoryVersion(TEST_REPO.getName());
        doNothing().when(cranFileSystemStorageService).storeAndDeleteFiles(any());
        doNothing().when(cranFileSystemStorageService).boostRepositoryVersion(TEST_REPO.getName());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/r/" + TEST_REPO.getName())
                        .file(new MockMultipartFile(
                                "files",
                                "Benchmarking_0.10.tar.gz",
                                "application/gzip",
                                TestUtils.readTestPackage("recent/Benchmarking_0.10.tar.gz")))
                        .file(new MockMultipartFile(
                                "checksums",
                                "",
                                "application/json",
                                new JSONObject(checksums).toString().getBytes(StandardCharsets.UTF_8)))
                        .param("version_before", "23")
                        .param("version_after", "24")
                        .param("page", "2/3")
                        .param("id", TEST_ID))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

        verify(cranFileSystemStorageService).storeAndDeleteFiles(any());
        verify(cranFileSystemStorageService).boostRepositoryVersion(TEST_REPO.getName());
    }
}
