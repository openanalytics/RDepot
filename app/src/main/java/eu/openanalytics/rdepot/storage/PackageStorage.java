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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.exception.CreateFolderStructureException;
import eu.openanalytics.rdepot.exception.DeleteFileException;
import eu.openanalytics.rdepot.exception.ExtractFileException;
import eu.openanalytics.rdepot.exception.GetFileInBytesException;
import eu.openanalytics.rdepot.exception.ManualCreateException;
import eu.openanalytics.rdepot.exception.Md5SumCalculationException;
import eu.openanalytics.rdepot.exception.MoveFileException;
import eu.openanalytics.rdepot.exception.MovePackageSourceException;
import eu.openanalytics.rdepot.exception.PackageDescriptionNotFound;
import eu.openanalytics.rdepot.exception.PackageSourceNotFoundException;
import eu.openanalytics.rdepot.exception.PackageStorageException;
import eu.openanalytics.rdepot.exception.ReadPackageDescriptionException;
import eu.openanalytics.rdepot.exception.SourceFileDeleteException;
import eu.openanalytics.rdepot.exception.StorageException;
import eu.openanalytics.rdepot.exception.WriteToDiskFromMultipartException;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.r.RDescription;

@Component
public class PackageStorage {
	
    Logger logger = LoggerFactory.getLogger(PackageStorage.class);
    
	private String separator =  FileSystems.getDefault().getSeparator();
	
	@Resource(name="packageUploadDirectory")
	private File packageUploadDirectory;
	
	@Resource
	private BaseStorage baseStorage;
	
	@Resource
	private MessageSource messageSource;
	
	private Locale locale = LocaleContextHolder.getLocale();
	
	/**
	 * This method creates manuals for given package.
	 * @param packageBag Package which manuals are created for
	 * @throws PackageStorageException
	 */
	public void createManuals(Package packageBag) throws ManualCreateException {
		File targzFile = new File(packageBag.getSource());
		
		if(targzFile != null && targzFile.exists() && targzFile.getParentFile() != null && 
				targzFile.getParentFile().exists()) {
			
			String packageName = packageBag.getName();
			File manualPdf = new File(targzFile.getParent(), packageName 
					+ separator + packageName + ".pdf");
			
			if(manualPdf != null && manualPdf.getParentFile() != null && 
					manualPdf.getParentFile().exists() && !manualPdf.exists()) {
				
				ProcessBuilder processBuilder = new ProcessBuilder(
						"R", "CMD", "Rd2pdf",
	                      "--no-preview",
	                      "--title=" + packageName,
	                      "--output=" + packageName + ".pdf",
	                      ".").directory(manualPdf.getParentFile()).redirectErrorStream(true);
				
				logger.info("Running Rd2pdf for package " + packageName);
				
				Process process = null;
				try {
					process = processBuilder.start();
					BufferedReader output = new BufferedReader(
							new InputStreamReader(process.getInputStream()));
					String outputLine = null;
					
					while((outputLine = output.readLine()) != null)
						// too much noise for info, use debug instead
						logger.debug(outputLine);
					
					Integer exitValue = process.waitFor();
					if(exitValue != 0) {
						logger.error("Rd2pdf failed with exit code " + exitValue);
						throw new ManualCreateException(messageSource, locale, packageBag);
					}
					
				} catch (IOException | InterruptedException e) {
					throw new ManualCreateException(messageSource, locale, packageBag, e.getMessage());
				} finally {
					if(process != null) {
						if(process.isAlive())
							process.destroyForcibly();
					}
				}
				
			}
		}
	}
	
	/**
	 * This method deletes source of given package.
	 * @param packageBag Package to delete
	 * @throws PackageStorageException
	 */
	public void deleteSource(Package packageBag) throws SourceFileDeleteException {
		File targzFile = new File(packageBag.getSource());
		
		if(targzFile != null && targzFile.exists() && targzFile.getParentFile() != null && 
				targzFile.getParentFile().exists()) {
			ProcessBuilder processBuilder = new ProcessBuilder("rm", "-rf", targzFile.getParent())
					.directory(targzFile.getParentFile())
					.redirectErrorStream(true);
			
			Process process = null;
			try {
				process = processBuilder.start();
				
				int exitValue = process.waitFor();
				
				if(exitValue != 0) {
					logger.error("rm failed with exit code " + exitValue);
					throw new SourceFileDeleteException(messageSource, locale, packageBag);
				}
			} catch(IOException | InterruptedException e) {
				throw new SourceFileDeleteException(messageSource, locale, packageBag, 
						e.getClass() + ": " + e.getMessage());
			} finally {
				if(process != null) {
					if(process.isAlive())
						process.destroyForcibly();
				}
			}
		}
	}
	
	/**
	 * This method returns package file in raw bytes.
	 * @param packageBag
	 * @return package file in raw bytes.
	 * @throws GetFileInBytesException
	 */
	public byte[] getPackageInBytes(Package packageBag) throws GetFileInBytesException {	
		return baseStorage.getFileInBytes(packageBag.getSource());
	}
	
	/**
	 * This method returns and creates if necessary reference manual for a given package.
	 * Output format is simply raw bytes.
	 * @param packageBag Package whose manual is returned
	 * @return Manual in bytes
	 * @throws ManualCreateException 
	 * @throws GetFileInBytesException 
	 */
	public byte[] getReferenceManualFileInBytes(Package packageBag) 
			throws ManualCreateException, GetFileInBytesException {
		String manualPath = new File(packageBag.getSource()).getParent() + separator 
				+ packageBag.getName() + separator + packageBag.getName() + ".pdf";
		File manualFile = new File(manualPath);
		byte[] bytes = null;
		
		if(manualFile == null || !manualFile.exists()) {
			createManuals(packageBag);
			manualFile = new File(manualPath);
		}
		
		bytes = baseStorage.getFileInBytes(manualFile.getAbsolutePath());

		
		return bytes;
	}
	
	/**
	 * This method calculates package's md5 sum.
	 * @param packageBag package to calculate
	 * @return String containing md5 sum
	 * @throws Md5SumCalculationException 
	 */
	public String calculatePackageMd5Sum(Package packageBag) throws Md5SumCalculationException {
		return baseStorage.calculateMd5Sum(packageBag.getSource());
	}
	
	/**
	 * This method reads existing vignette.
	 * @param packageBag Package whose vignette is read.
	 * @param filename Name of vignette's file
	 * @return Vignette in bytes
	 * @throws GetFileInBytesException 
	 */
	public byte[] readVignette(Package packageBag, String filename) throws GetFileInBytesException {
		return baseStorage.getFileInBytes(new File(packageBag.getSource()).getParent() 
				+ packageBag.getName() + "/inst/doc/" + filename);
	}
	
	/**
	 * This method saves package in so-called "waiting room" where it stays 
	 * until it's accepted by administrator
	 * @param multipartFile Multipart containing uploaded file.
	 * @param packageId ID of uploaded package - needed to provide uniqueness
	 * @return Package file
	 * @throws DeleteFileException 
	 * @throws WriteToDiskFromMultipartException 
	 */
	public File writeToWaitingRoom(MultipartFile multipartFile, Repository repository) 
			throws WriteToDiskFromMultipartException {
		File waitingRoom = new File(packageUploadDirectory.getAbsolutePath() 
				+ separator + "new" + separator + (new Random()).nextInt(100000000));
		
//		if(waitingRoom.exists()) {
//			throw new PackageStorageException(MessageCodes.ERROR_PACKAGE_ALREADY_UPLOADED);
//		}
		while(waitingRoom.exists()) {
			waitingRoom = new File(packageUploadDirectory.getAbsolutePath() 
					+ separator + "new" + separator + repository.getId() 
					+ (new Random()).nextInt(100000000));
		}
		
		return baseStorage.writeToDiskFromMultipart(multipartFile, waitingRoom);
	}

	/**
	 * This method saves uploaded package on the local disk.
	 * @param multipartFile Multipart containing uploaded file.
	 * @param repository Repository which package should be uploaded to
	 * @return Package file
	 * @throws DeleteFileException 
	 * @throws WriteToDiskFromMultipartException 
	 */
	public File writeToDisk(MultipartFile multipartFile, Repository repository) 
			throws WriteToDiskFromMultipartException, DeleteFileException {
		File randomDir = new File(packageUploadDirectory.getAbsolutePath() + separator 
				+ "repositories" + separator + repository.getId() + separator 
				+ (new Random()).nextInt(100000000));
		
		while(randomDir.exists())
			randomDir = new File(randomDir.getParent() + separator + (new Random()).nextInt(100000000));
		
		return baseStorage.writeToDiskFromMultipart(multipartFile, randomDir);
	}
	
	/**
	 * This method moves a package source from waiting directory to the main one.
	 * It should be called after submission is accepted.
	 * @param packageBag Package to be moved
	 * @return new package source directory
	 * @throws MovePackageSourceException 
	 * @throws PackageSourceNotFoundException
	 */
	public File moveToMainDirectory(Package packageBag)
			throws PackageSourceNotFoundException, MovePackageSourceException {
		Repository repository = packageBag.getRepository();
		File mainDir = new File(packageUploadDirectory.getAbsolutePath() + separator +
				"repositories" + separator + repository.getId() + separator + 
				(new Random().nextInt(100000000)));
		
		if(mainDir.exists())
			mainDir = new File(packageUploadDirectory.getAbsolutePath() + separator +
					"repositories" + separator + repository.getId() + separator + 
					(new Random().nextInt(100000000)));
		
		return moveSource(packageBag, mainDir.getAbsolutePath());
	}
	
	/**
	 * This method moves a package from its current directory to the new one.
	 * @param packageBag Package to be moved
	 * @param destination New package source directory
	 * @return New package source directory
	 * @throws PackageSourceNotFoundException
	 * @throws MovePackageSourceException
	 */
	public File moveSource(Package packageBag, String destinationDir) 
			throws PackageSourceNotFoundException, MovePackageSourceException {
		File current = new File(packageBag.getSource());
		
		if(!current.exists())
			throw new PackageSourceNotFoundException(messageSource, locale, packageBag);
		
		File destinationDirFile = new File(destinationDir);
		File newDirectory;
		
		try {
			newDirectory = baseStorage.move(current.getParentFile(), destinationDirFile);
		} catch (MoveFileException | CreateFolderStructureException e) {
			if(destinationDirFile.exists()) {
				try {
					baseStorage.deleteFile(destinationDirFile.getAbsolutePath());
				} catch (DeleteFileException dfe) {
					logger.error(dfe.getMessage(), dfe);
				}
			}
			logger.error(e.getMessage(), e);
			throw new MovePackageSourceException(messageSource, locale, packageBag);
		}
		
		String packageFilename = current.getName();
		try {
			baseStorage.deleteFile(packageFilename);
		} catch (DeleteFileException e) {
			logger.error(e.getMessage(), e);
			throw new MovePackageSourceException(messageSource, locale, packageBag);
		}
		
		return new File(newDirectory.getAbsolutePath() + separator + packageFilename);
	}
	
	/**
	 * This method verifies if package source path is correct.
	 * @param packageBag Related package
	 * @param source New package source path
	 * @throws PackageStorageException when directory is incorrect.
	 */
	public void verifySource(Package packageBag, String source) throws PackageStorageException {
		if(!(new File(source).exists()))
			throw new PackageSourceNotFoundException(messageSource, locale, packageBag);
		//TODO: more detailed verification, i.e. if directory structure is correct
	}
	
	public File extractPackageFile(File file) throws ExtractFileException {
		return baseStorage.extractFile(file);
	}
	
	public Properties readPackageDescription(File file) throws PackageStorageException {
		try {
			return new RDescription(file);
		} //catch (FileNotFoundException e) {
//			try {
//				baseStorage.deleteFile(file.getParentFile().getAbsolutePath());
//			} catch (StorageException e1) {
//				throw new PackageStorageException(e1.getMessage());
//			}
//			
//			throw new ReadPackageDescriptionException(messageSource, locale, file);
/*		} */catch (IOException e) {
			
			try {
				baseStorage.deleteFile(file.getParentFile().getAbsolutePath());
			} catch (DeleteFileException dfe) {
				logger.error(dfe.getMessage(), dfe);
			}
			logger.error(e.getClass() + ": " + e.getMessage());
			throw new ReadPackageDescriptionException(messageSource, locale, file, 
					"IOException was thrown while trying to read a file.");
		} 
	}
	
	public File getDescriptionFile(File extracted, String packageName) throws PackageDescriptionNotFound {
		File description = new File(extracted.getParent() + separator + packageName 
				+ separator + "DESCRIPTION");
		
		if(description.exists()) {
			return description;
		} else {
			throw new PackageDescriptionNotFound(messageSource, locale, extracted, packageName);
		}
	}
	
	public String calculateFileMd5Sum(String path) throws Md5SumCalculationException {
			return baseStorage.calculateMd5Sum(path);
	}
	
}
