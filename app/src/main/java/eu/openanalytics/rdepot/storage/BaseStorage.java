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
package eu.openanalytics.rdepot.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Locale;

import javax.annotation.Resource;

//import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.exception.CopyException;
import eu.openanalytics.rdepot.exception.CreateFolderStructureException;
import eu.openanalytics.rdepot.exception.DeleteFileException;
import eu.openanalytics.rdepot.exception.ExtractFileException;
import eu.openanalytics.rdepot.exception.GetFileInBytesException;
import eu.openanalytics.rdepot.exception.GzipFileException;
import eu.openanalytics.rdepot.exception.LinkFoldersException;
import eu.openanalytics.rdepot.exception.Md5SumCalculationException;
import eu.openanalytics.rdepot.exception.MoveFileException;
import eu.openanalytics.rdepot.exception.StorageException;
import eu.openanalytics.rdepot.exception.WriteToDiskFromMultipartException;

@Component
public class BaseStorage {
	
	private String separator = FileSystems.getDefault().getSeparator();
	
	@Resource
	private MessageSource messageSource;
	
	private Locale locale = LocaleContextHolder.getLocale();
	
	private Logger logger = LoggerFactory.getLogger(BaseStorage.class);
	
	/**
	 * This method creates a new directory in application's storage or overrides the existing one.
	 * @param path Path to the directory
	 * @return Created directory
	 * @throws CreateFolderStructureException
	 */
	public File createFolderStructure(String path) throws CreateFolderStructureException {
		File newFolder = new File(path);
		try {
			if(!newFolder.exists()) {
				Files.createDirectories(newFolder.toPath());
			} else {
				FileUtils.cleanDirectory(newFolder);
			}
			
		} catch(IOException e) {
			throw new CreateFolderStructureException(messageSource, locale, e.getMessage());
		}
		return newFolder;
	}
	
	/**
	 * This method creates a symbolic link to the given directory.
	 * @param targetPath Path to the directory we are linking to
	 * @param linkPath Path to the link
	 * @return created link
	 * @throws LinkFoldersException
	 */
	public File linkTwoFolders(String targetPath, String linkPath) throws LinkFoldersException {
		File target = new File(targetPath);
		File link = new File(linkPath);
		
		ProcessBuilder processBuilder = new ProcessBuilder(
				"ln", "-fsn", target.getAbsolutePath(), link.getAbsolutePath())
				.redirectErrorStream(true);
		
		logger.info("Creating a link to " + targetPath + " from " + linkPath);

		Process process = null;
		
		try {
			process = processBuilder.start();
//			BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
//			String outputLine = null;
//			
//			while((outputLine = output.readLine()) != null)
//				logger.info(outputLine);
			
			int exitValue = process.waitFor();
			
			if(exitValue != 0) {
				throw new LinkFoldersException(messageSource, locale, targetPath, linkPath, 
						"ln failed with exit code: " + process.exitValue());
			}
		} catch (IOException | InterruptedException e) {
			throw new LinkFoldersException(messageSource, locale, targetPath, linkPath, e.getMessage());
		} finally {
			if(process != null) {
				if(process.isAlive())
					process.destroyForcibly();
			}
		}

		return link;
	}
	
	/**
	 * This is a basic implementation of file copying.
	 * @param targetPath File to be copied
	 * @param destinationPath Destination file
	 * @return Destination file
	 * @throws CopyException
	 */
	public File copy(String targetPath, String destinationPath) throws CopyException {
		File target = new File(targetPath);
		File destination = new File(destinationPath);

		try {
			Files.copy(target.toPath(), destination.toPath());
		} catch(IOException e) {
			throw new CopyException(messageSource, locale, targetPath, destinationPath, e.getMessage());
		}
		
		return destination;
	}
	
	/**
	 * This method creates a compressed copy of a given file.
	 * @param path Path to target file
	 * @return Compressed file
	 * @throws StorageException
	 */
	public File gzipFile(String path) throws GzipFileException {
		File target = new File(path);
		
		ProcessBuilder processBuilder = new ProcessBuilder(
				"gzip", "-fk", target.getAbsolutePath())
				.redirectErrorStream(true);
		
		logger.info("Running gzip for " + path);
		
		Process process = null;
		
		try {
			process = processBuilder.start();
			BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String outputLine = null;
			
			while((outputLine = output.readLine()) != null)
				logger.info(outputLine);
			
			int exitValue = process.waitFor();
			
			if(exitValue != 0)
				throw new GzipFileException(messageSource, locale, path, 
						"Gzip failed with exit code: " + exitValue);
			
		} catch (IOException | InterruptedException e) {
			throw new GzipFileException(messageSource, locale, path, e.getMessage());
		} finally {
			if(process != null) {
				if(process.isAlive())
					process.destroyForcibly();
			}
		}
		
		return new File(path + ".gz");
	}
	
	/**
	 * This method calculates file's md5 sum.
	 * @param path Path to the target file
	 * @return Calculated MD5 sum
	 * @throws StorageException
	 */
	public String calculateMd5Sum(String path) throws Md5SumCalculationException {
		File target = new File(path);
		byte[] bytes;
		
		try {
			bytes = Files.readAllBytes(target.toPath());
		} catch(IOException e) {
			throw new Md5SumCalculationException(messageSource, locale, path, e.getMessage());
		}
		
		return DigestUtils.md5DigestAsHex(bytes);
	}
	
	
//	protected void delete(String path) throws StorageException {
//		try {
//			FileUtils.forceDelete(new File(path));
//		} catch(IOException e) {
//			throw new StorageException(e.getMessage());
//		}
//	}
//	
	/**
	 * This method reads a file from given source as a byte array.
	 * @param source Path to the file
	 * @return byte array
	 * @throws GetFileInBytesException
	 * @throws FileNotFoundException
	 */
	public byte[] getFileInBytes(String source) throws GetFileInBytesException, FileNotFoundException {
		byte[] bytes = null;
		File file = new File(source);
		FileSystemResource fsResource = new FileSystemResource(file);
		if(file != null && file.exists()) {
			try {
				bytes = Files.readAllBytes(fsResource.getFile().toPath());
			} catch(FileNotFoundException e) {
				throw e;
			} catch (IOException e) {
				throw new GetFileInBytesException(messageSource, locale, source, e.getMessage());
			}
		}
		
		return bytes;
	}

	public File extractFile(File file) throws ExtractFileException {
		ProcessBuilder processBuilder = new ProcessBuilder(
				"tar", "-zxf", file.getAbsolutePath(), "-C", file.getParent());
		
		logger.info("Running tar for " + file.getAbsolutePath());
		
		Process process = null;
		try {
			process = processBuilder.start();
			
			int exitValue = process.waitFor();
			if(exitValue != 0) {
				throw new ExtractFileException(messageSource, locale, 
						file.getAbsolutePath(), "Tar failed with exit code " + exitValue);
			}
			
		} catch (IOException | InterruptedException e) {
			try {
			if(file.getParentFile().exists())
				deleteFile(file.getParentFile().getAbsolutePath());
			} catch (DeleteFileException dfe) {
				logger.error(dfe.getClass() + ": " + dfe.getMessage());
			}
			throw new ExtractFileException(messageSource, locale, file.getAbsolutePath(), e.getMessage());
		} finally {
			if(process != null) {
				if(process.isAlive())
					process.destroyForcibly();
			}
		}

		return file;
	}
	
	/**
	 * This method deletes file or directory.
	 * @param path Path to deleted file.
	 * @throws StorageException
	 */
	public void deleteFile(String path) throws DeleteFileException {
		File file = new File(path);
		if(file.exists()) {
			try {
				FileUtils.forceDelete(file);
			} catch (IOException e) {
				throw new DeleteFileException(messageSource, locale, file.getAbsolutePath(), e.getMessage());
			}
		}
	}
	
	/**
	 * This method moves a file or directory using mv command.
	 * It is created to enable moving files between different volumes.
	 * @param source - Source file or directory
	 * @param destination - Destination file or directory
	 * @return Destination file or directory
	 * @throws CreateFolderStructureException 
	 * @throws StorageException
	 */
	public File move(File source, File destination) throws MoveFileException, CreateFolderStructureException {
		if(!destination.getParentFile().exists())
			createFolderStructure(destination.getParent());
		
		ProcessBuilder processBuilder = new ProcessBuilder(
				"mv", source.getAbsolutePath(), destination.getAbsolutePath());
		
		logger.info("Running mv for " + source.getAbsolutePath() + " and "
				+ destination.getAbsolutePath());
		
		Process process = null;
		try {
			process = processBuilder.start();
			
//			BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
//			String outputLine = null;
//			
//			while((outputLine = output.readLine()) != null)
//				logger.info(outputLine);
			
			int exitValue = process.waitFor();
			
			if(exitValue != 0) {
				throw new MoveFileException(messageSource, locale, 
						source.getAbsolutePath(), destination.getAbsolutePath(),
						"Mv failed with exit code: " + exitValue);
			}
			
		} catch (IOException | InterruptedException e) {
			throw new MoveFileException(messageSource, locale, source.getAbsolutePath(), destination.getAbsolutePath(), e.getMessage());
		} finally {
			if(process != null) {
				if(process.isAlive())
					process.destroyForcibly();
			}
		}
		
		return destination;
	}
	
	/**
	 * This method reads data from Multipart and saves them on the disk.
	 * @param multipartFile - Multipart to read
	 * @param destinationDir - directory where file will be saved
	 * @return saved file
	 * @throws WriteToDiskFromMultipartException
	 */
	public File writeToDiskFromMultipart(MultipartFile multipartFile, File destinationDir) 
			throws WriteToDiskFromMultipartException {
		//File destinationDir = new File(destinationDirPath);
		File file = new File(destinationDir.getAbsolutePath() + separator + multipartFile.getOriginalFilename());
		
		try {
			FileUtils.forceMkdir(destinationDir);
			multipartFile.transferTo(file);
			
			return file;
		} catch (IOException | IllegalStateException e) {
			try {
				deleteFile(destinationDir.getAbsolutePath());
			} catch (DeleteFileException dfe) {
				logger.error(dfe.getMessage());
			}
			throw new WriteToDiskFromMultipartException(messageSource, locale, 
					multipartFile, destinationDir.getAbsolutePath(), e.getMessage());
		}
	}
}
