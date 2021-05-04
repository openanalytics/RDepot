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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import eu.openanalytics.rdepot.exception.WriteToDiskException;
import eu.openanalytics.rdepot.exception.WriteToDiskFromMultipartException;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.r.RDescription;

public class PackageStorageLocalImpl implements PackageStorage {
	
    Logger logger = LoggerFactory.getLogger(PackageStorageLocalImpl.class);
    
	private String separator =  FileSystems.getDefault().getSeparator();
	
	@Resource(name="packageUploadDirectory")
	private File packageUploadDirectory;
	
	@Resource
	private BaseStorage baseStorage;
	
	@Resource
	private MessageSource messageSource;
	
	private Locale locale = LocaleContextHolder.getLocale();
	
	
	public void createManual(Package packageBag) throws ManualCreateException {
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

	public byte[] getPackageInBytes(Package packageBag) 
			throws GetFileInBytesException, FileNotFoundException {	
		return baseStorage.getFileInBytes(packageBag.getSource());
	}
	
	public byte[] getReferenceManualFileInBytes(Package packageBag) 
			throws ManualCreateException, GetFileInBytesException, FileNotFoundException {
		String manualPath = new File(packageBag.getSource()).getParent() + separator 
				+ packageBag.getName() + separator + packageBag.getName() + ".pdf";
		File manualFile = new File(manualPath);
		byte[] bytes = null;
				
		bytes = baseStorage.getFileInBytes(manualFile.getAbsolutePath());
		
		return bytes;
	}
	
	public String calculatePackageMd5Sum(Package packageBag) throws Md5SumCalculationException {
		return baseStorage.calculateMd5Sum(packageBag.getSource());
	}

	public byte[] readVignette(Package packageBag, String filename) 
			throws GetFileInBytesException, FileNotFoundException {
		return baseStorage.getFileInBytes(new File(packageBag.getSource()).getParent() 
				+ separator + packageBag.getName() + "/inst/doc/" + filename);
	}

	public File writeToWaitingRoom(MultipartFile packageFile, Repository repository) 
			throws WriteToDiskFromMultipartException {
		File waitingRoom = generateWaitingRoom(packageUploadDirectory, repository);
		
		return baseStorage.writeToDiskFromMultipart(packageFile, waitingRoom);
	}
	
	public File writeToWaitingRoom(File packageFile, Repository repository) 
			throws WriteToDiskException {
		File waitingRoom = generateWaitingRoom(packageUploadDirectory, repository);
		
		return baseStorage.writeToDisk(packageFile, waitingRoom);
	}
	
	private File generateWaitingRoom(File packageUploadDirectory, Repository repository) {
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
		
		return waitingRoom;
	}
	
	private File generateRandomUploadDir(File packageUploadDirectory, Repository repository) {
		File randomDir = new File(packageUploadDirectory.getAbsolutePath() + separator 
				+ "repositories" + separator + repository.getId() + separator 
				+ (new Random()).nextInt(100000000));
		
		while(randomDir.exists())
			randomDir = new File(randomDir.getParent() + separator + (new Random()).nextInt(100000000));

		return randomDir;
	}

	public File writeToDisk(MultipartFile multipartFile, Repository repository) 
			throws WriteToDiskFromMultipartException, DeleteFileException {
		File randomDir = generateRandomUploadDir(packageUploadDirectory, repository);
		
		return baseStorage.writeToDiskFromMultipart(multipartFile, randomDir);
	}
	
	public File writeToDisk(File packageFile, Repository repository)
		throws WriteToDiskException, DeleteFileException {
		File randomDir = generateRandomUploadDir(packageUploadDirectory, repository);
				
		return baseStorage.writeToDisk(packageFile, randomDir);
	}

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

	@Override
	public List<File> getVignetteFiles(Package packageBag) {
		List<File> files = new ArrayList<>();
		
		if(packageBag == null) {
			return files;
		}
		
		File vignettesFolder = new File(new File(packageBag.getSource()).getParent(), 
				packageBag.getName() + "/inst/doc/");
		
		if(vignettesFolder.exists() && vignettesFolder.isDirectory()) {
			File[] vignetteFiles = vignettesFolder.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return (name != null && (
							name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".pdf")));
				}
			});
			
			files = Collections.unmodifiableList(Arrays.asList(vignetteFiles));
		}
		
		return files;
	}

	@Override
	public Optional<String> getReferenceManualFilename(Package packageBag) {
		//This is a temporary solution.
		String manualPath = new File(packageBag.getSource()).getParent() + separator 
				+ packageBag.getName() + separator + packageBag.getName() + ".pdf";
		File manualFile = new File(manualPath);
		
		if(manualFile.exists()) {
			return Optional.of(manualFile.getName());
		}
		
		return Optional.empty();
	}
}
