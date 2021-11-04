/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
package eu.openanalytics.rdepot.test.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import eu.openanalytics.rdepot.exception.CreateFolderStructureException;
import eu.openanalytics.rdepot.exception.ExtractFileException;
import eu.openanalytics.rdepot.exception.GzipFileException;
import eu.openanalytics.rdepot.exception.LinkFoldersException;
import eu.openanalytics.rdepot.exception.MoveFileException;
import eu.openanalytics.rdepot.storage.BaseStorage;

@RunWith(MockitoJUnitRunner.class)
public class BaseStorageTest {

	@Mock
	private MessageSource messageSource;
		
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	private String separator =  FileSystems.getDefault().getSeparator();
	
	@InjectMocks
	BaseStorage baseStorage;

	@Test
	public void test_createFolderStructure() throws CreateFolderStructureException {
		String path = temporaryFolder.getRoot().getAbsolutePath() + separator + "test_folder" + separator + "sub_folder";
		
		baseStorage.createFolderStructure(path);
		
		assertTrue("Folders were not created.", new File(path).exists());
	}
	
	@Test
	public void test_linkTwoFolders() throws LinkFoldersException, IOException {
		String targetPath = temporaryFolder.getRoot().getAbsolutePath() + separator + "test_folder";
		String linkPath = temporaryFolder.getRoot().getAbsolutePath() + separator + "test_link";
		
		baseStorage.linkTwoFolders(targetPath, linkPath);
		
		File link = new File(linkPath);
		assertTrue("Created file is not a symbolic link.", Files.isSymbolicLink(link.toPath()));
		
		Path result = Files.readSymbolicLink(link.toPath());
		assertEquals("Created symlink is not correct.", targetPath, 
				temporaryFolder.getRoot().toPath().resolve(result).toString());
	}
	
	@Test
	public void test_gzipFile() throws IOException, GzipFileException {
		File testFile = temporaryFolder.newFile();
		
		File gzip = baseStorage.gzipFile(testFile.getAbsolutePath());
		
		InputStream in = new BufferedInputStream(new FileInputStream(gzip));
		in.mark(2);
		int magic = 0;
		magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
		in.reset();
		in.close();
		
		assertTrue("File was not gzipped correctly.", magic == GZIPInputStream.GZIP_MAGIC);
	}
	
	@Test
	public void test_extractFile() throws IOException, ExtractFileException {
		String extractedFilename = "example.txt";
		String expectedFilename = "expected.txt";
		String archiveFilename = "example.tar.gz";
		String testFolderPath = "src/test/resources/unit/test_files/test_extractFile/";
		
		File archive = new File(testFolderPath + archiveFilename);
		File expected = new File(testFolderPath + expectedFilename);
		
		if(!archive.exists() || !expected.exists())
			fail("There is no example files.");
		
		baseStorage.extractFile(archive);
		
		File extracted = new File(testFolderPath + extractedFilename);
		assertTrue("Extracted file's is not correct.", FileUtils.contentEquals(expected, extracted));
		
		FileUtils.forceDelete(extracted);
	}
	
	@Test
	public void test_move() throws IOException, MoveFileException, CreateFolderStructureException {
		File from = temporaryFolder.newFile();
		File to = new File(temporaryFolder.newFolder().getAbsolutePath() + from.getName());
		
		baseStorage.move(from, to);
		
		assertTrue("File was not moved correctly.", to.exists());
	}
}
