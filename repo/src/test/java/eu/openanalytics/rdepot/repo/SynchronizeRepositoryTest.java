/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.exception.InitTransactionException;
import eu.openanalytics.rdepot.repo.exception.ProcessRequestException;
import eu.openanalytics.rdepot.repo.model.RepositoryBackup;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.storage.FileSystemStorageService;
import eu.openanalytics.rdepot.repo.storage.StorageProperties;

@RunWith(MockitoJUnitRunner.class)
@AutoConfigureMockMvc
public class SynchronizeRepositoryTest {

	private static final String TEST_PACKAGES_DIR = 
			"src/test/resources/eu/openanalytics/rdepot/repo/testpackages/";
	
    private final String TRASH_PREFIX = "TRASH_";
    private final String TRASH_DATABASE_FILE = "TRASH_DATABASE.txt";
    
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	@Spy
	QueueMap<String, SynchronizeRepositoryRequestBody> requestMap = new QueueMap<>();
	
	@Spy
	QueueMap<String, SynchronizeRepositoryResponseBody> responseMap = new QueueMap<>();
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Spy
	HashMap<String, RepositoryBackup> backupMap = new HashMap<>();
	
	private File testPackagesDir;
	
	private final String REPOSITORY = "testrepo";
	
	private final String NON_EXISTING_REPOSITORY = "testrepo1";
	
    private FileSystemStorageService storageService;
    
    private MultipartFile[] getTestPackages(boolean archive) throws IOException {
    	ArrayList<MultipartFile> files = new ArrayList<MultipartFile>();
    	
    	String subDir = archive ? "archive" : "recent";
    	
    	for(File file : testPackagesDir.toPath().resolve(subDir).toFile().listFiles()) {
    		MultipartFile multipartFile = new MockMultipartFile("files", 
    				file.getName(), null, Files.readAllBytes(file.toPath()));;
    		
    		files.add(multipartFile);
    	}
    	
    	
    	MultipartFile[] testPackages = new MultipartFile[files.size()];
    	files.toArray(testPackages);
    	
    	return testPackages;
    }
    
    private MultipartFile[] getRecentTestPackages() throws IOException {
    	return getTestPackages(false);
    }
    
    private MultipartFile[] getArchiveTestPackages() throws IOException {
    	return getTestPackages(true);
    }
    
    private String[] getPackagesToDelete(File[] files) {
    	ArrayList<String> filenameList = new ArrayList<String>();
    	
    	for(File file : files) {
    		if(!file.getName().startsWith("PACKAGES"))
    			filenameList.add(file.getName());
    	}
    	
    	String[] filenameArray = new String[filenameList.size()];
    	filenameList.toArray(filenameArray);
    	
    	return filenameArray;
    }
	
    @Before
    public void setUp() throws IOException {
    	temporaryFolder.create();
    	Path rootLocation = temporaryFolder.getRoot().toPath();
    	Files.createDirectory(rootLocation.resolve(REPOSITORY));
    	
    	StorageProperties properties = new StorageProperties();
    	properties.setLocation(rootLocation.toAbsolutePath().toString());
    	storageService = new FileSystemStorageService(properties, requestMap, responseMap, backupMap);
    	
    	testPackagesDir = new File(TEST_PACKAGES_DIR);
    	if(!testPackagesDir.exists() || !testPackagesDir.isDirectory())
    		throw new FileNotFoundException(testPackagesDir.getAbsolutePath());
    }
    
    @After
    public void tearDown() {
    	temporaryFolder.delete();
    	storageService = null;
    }
    
    private void assertFiles(File[] expectedFiles, File actualDirectory) throws IOException {
    	for(File expectedFile : expectedFiles) {
    		byte[] expectedBytes = null;
    		byte[] actualBytes = null;
    		
			expectedBytes = Files.readAllBytes(expectedFile.toPath());
    		
			Path actualDirectoryPath = actualDirectory.toPath();
			actualDirectoryPath = actualDirectory.getName().equals("Archive") 
					&& !expectedFile.getName().startsWith("PACKAGES") ? 
					actualDirectoryPath.resolve(expectedFile.getName().split("_")[0]) 
					: actualDirectoryPath;
    		Path actual = actualDirectoryPath.resolve(expectedFile.getName());
    		actualBytes = Files.readAllBytes(actual);
    		
    		assertTrue(Arrays.equals(actualBytes, expectedBytes), "Uploaded file is not correct");
    	}
    }
    
    private void assertFiles(MultipartFile[] expectedFiles, File actualDirectory) throws IOException {
    	for(MultipartFile expectedFile : expectedFiles) {
    		byte[] expectedBytes = null;
    		byte[] actualBytes = null;
    		
			expectedBytes = expectedFile.getBytes();
    		
			Path actualDirectoryPath = actualDirectory.toPath();
			actualDirectoryPath = actualDirectory.getName().equals("Archive") 
					&& !expectedFile.getOriginalFilename().startsWith("PACKAGES") ? 
					actualDirectoryPath.resolve(expectedFile.getOriginalFilename().split("_")[0]) 
					: actualDirectoryPath;
    		Path actual = actualDirectoryPath.resolve(expectedFile.getOriginalFilename());
    		actualBytes = Files.readAllBytes(actual);
    		
    		assertTrue(Arrays.equals(actualBytes, expectedBytes), "Uploaded file is not correct");
    	}
    }
    
    @Test
	public void uploadPackages() throws IOException, ProcessRequestException {
    	String randomId = RandomStringUtils.randomAlphabetic(16);
    	doNothing().when(requestMap).remove(randomId);
    	
    	MultipartFile[] recent = getRecentTestPackages();
    	MultipartFile[] archive = getArchiveTestPackages();
    	
    	SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
    			randomId, recent, archive, 
    			new String[0], new String[0], "1", "2", "1/1", REPOSITORY);
    	
    	storageService.processRequest(requestBody);
    	
    	File recentDir = temporaryFolder.getRoot().toPath()
    						.resolve(REPOSITORY).resolve("src").resolve("contrib").toFile();
    	File archiveDir = recentDir.toPath().resolve("Archive").toFile();
    	
    	assertFiles(recent, recentDir);
    	assertFiles(archive, archiveDir);
	}
    
    @Test
    public void getPackages_WhenRepositoryIsEmpty() {
    	List<File> files = storageService.getRecentPackagesFromRepository(NON_EXISTING_REPOSITORY);
    	Map<String, List<File>> archive = storageService.getArchiveFromRepository(NON_EXISTING_REPOSITORY);
    	
    	assertTrue(files.isEmpty(), "File list should be empty.");
    	assertTrue(archive.isEmpty(), "Archive map should be empty.");
    }
    
    @Test
    public void deletePackages() throws IOException, ProcessRequestException {
    	String randomId = RandomStringUtils.randomAlphabetic(16);
    	doNothing().when(requestMap).remove(randomId);
    	
    	Path trash = Files.createDirectory(
    			temporaryFolder.getRoot().toPath().resolve(TRASH_PREFIX + randomId));
    	Files.createFile(trash.resolve(TRASH_DATABASE_FILE));
    	
    	File recentTestPackagesDir = new File(TEST_PACKAGES_DIR).toPath().resolve("recent").toFile();
    	File archiveTestPackagesDir = new File(TEST_PACKAGES_DIR).toPath().resolve("archive").toFile();
    	
    	File recentDir = copyTestPackagesToTemporaryFolder(new File(TEST_PACKAGES_DIR).toPath());
    	File archiveDir = recentDir.toPath().resolve("Archive").toFile();
    	
    	MultipartFile[] recent = new MultipartFile[0];
    	MultipartFile[] archive = new MultipartFile[0];
    	
    	String[] recentToDelete = getPackagesToDelete(recentTestPackagesDir.listFiles());
    	String[] archiveToDelete = getPackagesToDelete(archiveTestPackagesDir.listFiles());
    	
    	SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
    			randomId, recent, archive, 
    			recentToDelete, archiveToDelete, "1", "2", "1/1", REPOSITORY);
    	
    	storageService.processRequest(requestBody);
    	
    	assertEquals(3, recentDir.listFiles().length);
    	assertEquals(0, archiveDir.listFiles().length);
    }
    
    @Test
    public void boostRepositoryVersion() throws ProcessRequestException, IOException {
    	String randomId = RandomStringUtils.randomAlphabetic(16);
    	doNothing().when(requestMap).remove(randomId);
    	
    	MultipartFile[] recent = getRecentTestPackages();
    	MultipartFile[] archive = getArchiveTestPackages();
    	
    	SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
    			randomId, recent, archive, 
    			new String[0], new String[0], "1", "2", "1/1", REPOSITORY);
    	
    	storageService.processRequest(requestBody);
    	
    	File versionFile = temporaryFolder.getRoot().toPath().resolve("testrepo").resolve("VERSION").toFile();
    	Scanner scanner = new Scanner(versionFile);
    	String actualVersion = scanner.nextLine();
    	scanner.close();
    	
    	assertEquals("2", actualVersion);
    }
    
    @Test
    public void processRequest_throwsProcessRequestException_WhenVersionIsIncorrect() 
    		throws IOException, ProcessRequestException {
    	String randomId = RandomStringUtils.randomAlphabetic(16);
    	doNothing().when(requestMap).remove(randomId);
    	
    	MultipartFile[] recent = getRecentTestPackages();
    	MultipartFile[] archive = getArchiveTestPackages();
    	
    	SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
    			randomId, recent, archive, 
    			new String[0], new String[0], "10", "11", "1/1", REPOSITORY);
    	
    	expectedException.expect(ProcessRequestException.class);
    	
    	storageService.processRequest(requestBody);
    }
    
    @Test
    public void initTransaction() throws IOException, InitTransactionException {
    	File recentDir = copyTestPackagesToTemporaryFolder(new File(TEST_PACKAGES_DIR).toPath());
    	File archiveDir = recentDir.toPath().resolve("Archive").toFile();
    	
    	storageService.initTransaction(REPOSITORY, "1");
    	
    	RepositoryBackup backup = backupMap.values().iterator().next();
    	
    	for(File file : recentDir.listFiles()) {
    		if(file.getName().endsWith(".tar.gz")) {
    			assertTrue(backup.getRecentPackages().contains(file.getName()));
    		}
    	}
    	
    	for(File file : archiveDir.listFiles()) {
    		if(file.isDirectory()) {
    			for(File packageFile : file.listFiles()) {
    				assertTrue(backup.getArchivePackages().contains(packageFile.getName()));
    			}
    		}
    	}
    	
    	assertTrue(backup.getTrashDirectory().exists());
    	assertTrue(backup.getTrashDirectory().toPath().resolve(TRASH_DATABASE_FILE).toFile().exists());
    	assertEquals(1, backupMap.size(), "There should be only one backup for transaction!");
    }
    
    @Test
    public void uploadPackages_toEmptyRepository() {
    	
    }
    
    @Test
    public void deletePackages_restoresRepositoryAfterFailure() throws IOException, InitTransactionException, ProcessRequestException {
    	String randomId = RandomStringUtils.randomAlphabetic(16);
    	
    	Path restoreTestResourcesDir = new File(TEST_PACKAGES_DIR).toPath().resolve("restore_test");
    	File currentStateDir = restoreTestResourcesDir.resolve("current_state").toFile();
    	File recentDir = copyTestPackagesToTemporaryFolder(currentStateDir.toPath());
    	File archiveDir = recentDir.toPath().resolve("Archive").toFile();
    	File toUpload = restoreTestResourcesDir.resolve("to_upload").toFile();
    	
    	String[] recentToDelete = {"nonexisting.tar.gz"};
    	String[] archiveToDelete = {"nonexisting.tar.gz"};
    	
    	ArrayList<MultipartFile> recentFiles = new ArrayList<MultipartFile>();
    	ArrayList<MultipartFile> archiveFiles = new ArrayList<MultipartFile>();

    	for(File subDir : toUpload.listFiles()) {
    		for(File file : subDir.listFiles()) {
    			MultipartFile multipartFile = new MockMultipartFile("files", 
        				file.getName(), null, Files.readAllBytes(file.toPath()));
    			
    			if(subDir.getName().equals("recent"))
    				recentFiles.add(multipartFile);
    			else if(subDir.getName().equals("archive"))
    				archiveFiles.add(multipartFile);
    		}
    	}
    	
    	MultipartFile[] recentMultipartFiles = new MultipartFile[recentFiles.size()];
    	MultipartFile[] archiveMultipartFiles = new MultipartFile[archiveFiles.size()];
    	recentFiles.toArray(recentMultipartFiles);
    	archiveFiles.toArray(archiveMultipartFiles);
    	
    	SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
    			randomId, recentMultipartFiles, archiveMultipartFiles, 
    			recentToDelete, archiveToDelete, "1", "2", "1/1", REPOSITORY);
    	
    	expectedException.expect(ProcessRequestException.class);
    	
    	storageService.initTransaction(REPOSITORY, "1");
    	storageService.processRequest(requestBody);
    	
    	File[] expectedRecent = currentStateDir.toPath().resolve("recent").toFile().listFiles();
    	File[] expectedArchive = currentStateDir.toPath().resolve("archive").toFile().listFiles();
    	
    	assertFiles(expectedRecent, recentDir);
    	assertFiles(expectedArchive, archiveDir);
    }
    
    private File copyTestPackagesToTemporaryFolder(Path testPackagesDirectory) throws IOException {
    	File recentDir = temporaryFolder.getRoot().toPath()
				.resolve(REPOSITORY).resolve("src").resolve("contrib").toFile();
    	File archiveDir = recentDir.toPath().resolve("Archive").toFile();
    	
    	File recentTestPackagesDir = testPackagesDirectory.resolve("recent").toFile();
    	File archiveTestPackagesDir = testPackagesDirectory.resolve("archive").toFile();
    	
    	Files.createDirectories(archiveDir.toPath());
    	
    	FileUtils.copyDirectory(recentTestPackagesDir, recentDir);
    	for(File file : archiveTestPackagesDir.listFiles()) {
    		if(!file.getName().startsWith("PACKAGES")) {
    			String packageName = file.getName().split("_")[0];
        		Path packageDedicatedDir = archiveDir.toPath().resolve(packageName);
        		
        		if(!packageDedicatedDir.toFile().exists()) {
        			Files.createDirectories(packageDedicatedDir);
        		}
        		
        		Files.copy(file.toPath(), packageDedicatedDir.resolve(file.getName()));
    		} else {
    			Files.copy(file.toPath(), archiveDir.toPath().resolve(file.getName()));
    		}
       	}
    	
    	return recentDir;
    }
}
