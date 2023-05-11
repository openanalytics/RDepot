/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.r.storage.implementations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.CleanUpAfterSynchronizationException;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateFolderStructureException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.GzipFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.InvalidSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.LinkFoldersException;
import eu.openanalytics.rdepot.base.storage.exceptions.Md5MismatchException;
import eu.openanalytics.rdepot.base.storage.exceptions.Md5SumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.MoveFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.MovePackageSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.PackageFolderPopulationException;
import eu.openanalytics.rdepot.base.storage.exceptions.ReadPackageDescriptionException;
import eu.openanalytics.rdepot.base.storage.exceptions.RepositoryDirectoryDeleteException;
import eu.openanalytics.rdepot.base.storage.exceptions.WriteToWaitingRoomException;
import eu.openanalytics.rdepot.base.storage.implementations.CommonLocalStorage;
import eu.openanalytics.rdepot.r.RDescription;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.entities.Vignette;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.storage.exceptions.GenerateManualException;
import eu.openanalytics.rdepot.r.storage.exceptions.GetReferenceManualException;
import eu.openanalytics.rdepot.r.storage.exceptions.OrganizePackagesException;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadPackageVignetteException;
import eu.openanalytics.rdepot.r.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;

/**
 * Local storage implementation for R.
 */
@Component
public class RLocalStorage extends CommonLocalStorage<RRepository, RPackage> implements RStorage {
	
	private static final Logger logger = LoggerFactory.getLogger(RLocalStorage.class);
	
	@Resource(name = "packageUploadDirectory")
	private File packageUploadDirectory;
	
	@Resource(name="repositoryGenerationDirectory")
	private File repositoryGenerationDirectory;
	
	@Value("${repository-snapshots}")
	private String snapshot;
	
	@Override
	public String writeToWaitingRoom(final MultipartFile fileData, final RRepository repository) 
			throws WriteToWaitingRoomException {
		try {
			final File waitingRoom = generateWaitingRoom(packageUploadDirectory, repository);
			final File file = new File(waitingRoom.getAbsolutePath() + separator + fileData.getOriginalFilename());
			
			fileData.transferTo(file);
			
			return file.getAbsolutePath();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new WriteToWaitingRoomException();
		}
	}

    private File generateWaitingRoom(final File packageUploadDirectory, final RRepository repository) throws IOException {
    	File waitingRoom = new File(packageUploadDirectory.getAbsolutePath() 
				+ separator + "new" + separator + (new Random()).nextInt(100000000));
    	
    	while(waitingRoom.exists()) {
			waitingRoom = new File(packageUploadDirectory.getAbsolutePath() 
					+ separator + "new" + separator + repository.getId() 
					+ (new Random()).nextInt(100000000));
		}
    	
    	FileUtils.forceMkdir(waitingRoom);
		return waitingRoom;
	}

	@Override
	public Properties getPropertiesFromExtractedFile(final String extractedFile) 
			throws ReadPackageDescriptionException {
		try {
			return new RDescription(new File(extractedFile + separator + "DESCRIPTION"));
		} catch (IOException e) {
			try {
				deleteFile(new File(extractedFile).getParentFile());
			} catch (DeleteFileException dfe) {
				logger.error(dfe.getMessage(), dfe);
			}
			logger.error(e.getMessage(), e);
			throw new ReadPackageDescriptionException();
		}
	}

	@Override
	public String moveToMainDirectory(final RPackage packageBag) throws InvalidSourceException, MovePackageSourceException {
		logger.debug("Moving package to the main directory...");
		final RRepository repository = packageBag.getRepository();
		File mainDir = new File(packageUploadDirectory.getAbsolutePath() + separator +
				"repositories" + separator + repository.getId() + separator + 
				(new Random().nextInt(100000000)));
		
		if(mainDir.exists())
			mainDir = new File(packageUploadDirectory.getAbsolutePath() + separator +
					"repositories" + separator + repository.getId() + separator + 
					(new Random().nextInt(100000000)));
		
		final File current = new File(packageBag.getSource());
		if(!current.exists()) {
			logger.error("Source [" + packageBag.getSource() + "] for package "
					+ packageBag.toString() + " does not exist.");
			throw new InvalidSourceException();
		}
		
		File newDirectory = null;
		try {
			newDirectory = move(current.getParentFile(), mainDir);
		} catch(MoveFileException e) {
			if(mainDir.exists()) {
				try {
					deleteFile(mainDir);
				} catch (DeleteFileException dfe) {
					logger.error(dfe.getMessage(), dfe);
				} 
			}
			logger.error(e.getMessage(), e);
			throw new MovePackageSourceException();
		}
		
		final String packageFilename = current.getName();
		try {
			deleteFile(current);
		} catch(DeleteFileException e) {
			logger.error(e.getMessage(), e);
			throw new MovePackageSourceException();
		}
		logger.debug("Package moved to the following location: " 
				+ newDirectory.getAbsolutePath() + separator + packageFilename);
		return new File(newDirectory.getAbsolutePath() + separator + packageFilename).getAbsolutePath();
	}

	private void createTemporaryFoldersForLatestAndArchive(String path) throws CreateFolderStructureException {
		File latest = createFolderStructure(path + separator + "latest");
		File archive = createFolderStructure(path + separator + "Archive");
		
		try {
			File packagesLatest = Files.createFile(latest.toPath().resolve("PACKAGES")).toFile();
			File packagesArchive = Files.createFile(archive.toPath().resolve("PACKAGES")).toFile();
			
			gzipFile(packagesLatest);
			gzipFile(packagesArchive);
		} catch(IOException | GzipFileException e) {
			logger.error("Could not create PACKAGES file", e);
			throw new CreateFolderStructureException();
		}
		
	}

	private File linkCurrentFolderToGeneratedFolder(RRepository repository, String dateStamp) 
			throws LinkFoldersException {
		return linkTwoFolders(
				repositoryGenerationDirectory.getAbsolutePath() 
				+ separator + repository.getId() + separator + dateStamp,
			repositoryGenerationDirectory.getAbsolutePath() 
				+ separator + repository.getId() + separator + "current");
	}

	private void populateGeneratedFolder(List<RPackage> packages, RRepository repository, String dateStamp)
			throws PackageFolderPopulationException {
		String folderPath = repositoryGenerationDirectory.getAbsolutePath()
				+ separator + repository.getId()
				+ separator + dateStamp
				+ separator+ "src"
				+ separator + "contrib";
		
		populatePackageFolder(packages, folderPath);
	}

	private void createFolderStructureForGeneration(RRepository repository, String dateStamp) 
			throws CreateFolderStructureException {
		File dateStampFolder = null;
		try {
			dateStampFolder = createFolderStructure(
					repositoryGenerationDirectory.getAbsolutePath()
					+ separator + Integer.toString(repository.getId())
					+ separator + dateStamp);
			
			createFolderStructure(
					dateStampFolder.getAbsolutePath() + separator + "src" + separator + "contrib");
			
		} catch (CreateFolderStructureException e) {
			if(dateStampFolder != null) {
				try {
					deleteFile(dateStampFolder);
				} catch (DeleteFileException dfe) {
					logger.error(dfe.getMessage(), dfe);
				}
			}
			
			throw e;
		}
		
	}

	@Override
	public void verifySource(RPackage packageBag, String newSource) throws InvalidSourceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRepositoryDirectory(RRepository repository) throws RepositoryDirectoryDeleteException {
		try {
			deleteFile(new File(packageUploadDirectory.getAbsolutePath() + separator + "repositories" + separator + Integer.toString(repository.getId())));
			deleteFile(new File(repositoryGenerationDirectory.getAbsolutePath() + separator + Integer.toString(repository.getId())));
		} catch (DeleteFileException e) {
			logger.error(e.getMessage(), e);
			throw new RepositoryDirectoryDeleteException();
		}
	}

	@Override
	public String moveToTrashDirectory(RPackage packageBag) throws MovePackageSourceException {
		File trashDir = new File(packageUploadDirectory.getAbsolutePath() + separator 
				+ "trash" + separator + packageBag.getRepository().getId() 
				+ separator + (new Random()).nextInt(100000000));
		
		while(trashDir.exists()) {
			trashDir = new File(packageUploadDirectory.getAbsolutePath() + separator 
					+ "trash" + separator + packageBag.getRepository().getId() 
					+ separator + (new Random()).nextInt(100000000));
		}
		
		return moveSource(packageBag, trashDir.getAbsolutePath());
	}

	public String moveSource(RPackage packageBag, String destinationDir) throws MovePackageSourceException {
		if(packageBag.getSource().isEmpty()) {
			return ""; //TODO: Is it the correct behavior?		
		}
		File current = new File(packageBag.getSource());
		
		if(!current.exists()) {
			throw new IllegalStateException("Source for package " 
					+ packageBag.toString() + " [" + packageBag.getSource() + "] not found.");
		}
		
		final File destinationDirFile = new File(destinationDir);
		File newDirectory = null;
		
		try {
			newDirectory = move(current.getParentFile(), destinationDirFile);
		} catch (MoveFileException e) {
			if(destinationDirFile.exists()) {
				try {
					deleteFile(destinationDirFile);
				} catch (DeleteFileException dfe) {
					logger.error(dfe.getMessage(), dfe);
				}
			}
			logger.error(e.getMessage(), e);
			throw new MovePackageSourceException();
		}
		
		String packageFilename = current.getName();
		try {
			deleteFile(current);
		} catch (DeleteFileException e) {
			logger.error(e.getMessage(), e);
			throw new MovePackageSourceException();
			//TODO: And what about what already happened? Maybe it would be a better idea to reverse it?
		}
		final String newSource = newDirectory.getAbsolutePath()+ separator + packageFilename;
		return newSource;
	}

	@Override
	public SynchronizeRepositoryRequestBody buildSynchronizeRequestBody(
			PopulatedRepositoryContent populatedRepositoryContent, 
			List<String> remoteLatestPackages, List<String> remoteArchivePackages,
			RRepository repository, String versionBefore) {
		final List<File> latestToUpload = selectPackagesToUpload(remoteLatestPackages, populatedRepositoryContent.getLatestPackages());

		final File currentDirectory = new File(repositoryGenerationDirectory.getAbsolutePath() +
				separator + repository.getId() 
				+ separator + "current" 
				+ separator + "src" + separator + "contrib" + separator + "latest");
		final File packagesFile = new File(currentDirectory.getAbsolutePath() + separator + "PACKAGES");
		final File packagesGzFile = new File(currentDirectory.getAbsolutePath() + separator + "PACKAGES.gz");
		
		final List<String> latestToDelete = selectPackagesToDelete(remoteLatestPackages, populatedRepositoryContent.getLatestPackages());
		final List<File> archiveToUpload = selectPackagesToUpload(remoteArchivePackages, populatedRepositoryContent.getArchivePackages());

		final File archiveDirectory = new File(repositoryGenerationDirectory.getAbsolutePath() +
		separator + repository.getId() 
		+ separator + "current" 
		+ separator + "src" + separator + "contrib" + separator + "Archive");
		final File packagesFileFromArchive = new File(archiveDirectory.getAbsolutePath() + separator + "PACKAGES");
		final File packagesGzFileFromArchive = new File(archiveDirectory.getAbsolutePath() + separator + "PACKAGES.gz");

		final List<String> archiveToDelete = selectPackagesToDelete(remoteArchivePackages, populatedRepositoryContent.getArchivePackages());
		
		return new SynchronizeRepositoryRequestBody(latestToUpload, archiveToUpload,
					latestToDelete, archiveToDelete, versionBefore, packagesFile, 
					packagesGzFile, packagesFileFromArchive, packagesGzFileFromArchive);
	}

	@Override
	public PopulatedRepositoryContent organizePackagesInStorage(String dateStamp, List<RPackage> packages, 
			List<RPackage> latestPackages, List<RPackage> archivePackages, RRepository repository) 
					throws OrganizePackagesException {
		try {
			createFolderStructureForGeneration(repository, dateStamp);
			populateGeneratedFolder(packages, repository, dateStamp);
			
			final File target = linkCurrentFolderToGeneratedFolder(repository, dateStamp);
			final Path repoPath = target.toPath().resolve("src").resolve("contrib");
			final String latestFolderPath = repoPath.toString() + separator + "latest";
			final String archiveFolderPath = repoPath.toString() + separator + "Archive";
			
			createTemporaryFoldersForLatestAndArchive(repoPath.toString());
			populatePackageFolder(latestPackages, latestFolderPath);
			populatePackageFolder(archivePackages, archiveFolderPath);
			
			return new PopulatedRepositoryContent(latestPackages, archivePackages, 
					latestFolderPath, archiveFolderPath);
		} catch (CreateFolderStructureException | PackageFolderPopulationException | LinkFoldersException e) {
			logger.error(e.getMessage(), e);
			throw new OrganizePackagesException();
		}
	}
	
	private void populatePackageFolder(List<RPackage> latestPackages, String latestFolderPath) 
			throws PackageFolderPopulationException {
		for(RPackage packageBag : latestPackages) {
			populatePackage(packageBag, latestFolderPath);
		}
	}

	private void populatePackage(RPackage packageBag, String folderPath) throws PackageFolderPopulationException {
		if(packageBag.isActive()) {
			String targetFilePath = packageBag.getSource();
			String destinationFilePath = folderPath + separator + packageBag.getName() + "_" + packageBag.getVersion() + ".tar.gz";
			
			try {
				File packagesFile = new File(folderPath + separator + "PACKAGES");
				
				Files.copy(new File(targetFilePath).toPath(), new File(destinationFilePath).toPath());
				if(!packageBag.getMd5sum().equals(calculateMd5Sum(new File(destinationFilePath)))) {
					throw new Md5MismatchException();
				}
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(packagesFile, true));
				writer.append(generatePackageString(packageBag));
				writer.close();
				
				gzipFile(packagesFile);
			} catch(IOException | GzipFileException | 
					Md5MismatchException | Md5SumCalculationException e) {
				logger.error(e.getClass() + ": " + e.getMessage());
				throw new PackageFolderPopulationException();
			}
		}
	}

	private String generatePackageString(RPackage packageBag) {
		String packageString = "";
		String lineSeparator = System.lineSeparator();
		
		packageString += "Package: " + packageBag.getName() + lineSeparator;
		packageString += "Version: " + packageBag.getVersion() + lineSeparator;
		if(packageBag.getDepends() != null && !packageBag.getDepends().trim().isEmpty())
			packageString += "Depends: " + packageBag.getDepends() + lineSeparator;
		if(packageBag.getImports() != null && !packageBag.getImports().trim().isEmpty())
			packageString += "Imports: " + packageBag.getImports() + lineSeparator;
		packageString += "License: " + packageBag.getLicense() + lineSeparator;
		packageString += "MD5Sum: " + packageBag.getMd5sum() + lineSeparator;
		packageString += "NeedsCompilation: no" + lineSeparator;
		packageString += lineSeparator;
		
		return packageString;
	}

	private List<File> selectPackagesToUpload(List<String> remotePackages, List<RPackage> localPackages) {
		List<File> toUpload = new ArrayList<File>();
		
		for(RPackage packageBag : localPackages) {
			if(!remotePackages.contains(packageBag.getFileName())) {
				toUpload.add(new File(packageBag.getSource()));
			}
		}
		
		return toUpload;
	}
	
	private List<String> selectPackagesToDelete(List<String> remotePackages, List<RPackage> localPackages) {
		List<String> toDelete = new ArrayList<String>();
		
		for(String packageName: remotePackages) {
			Boolean found = false;
			for(RPackage packageBag: localPackages) {
				if(packageBag.getFileName().equals(packageName)) {
					found = true;
					break;
				}
			}
			
			if(!found) {
				toDelete.add(packageName);
			}
		}
		
		return toDelete;
	}

	@Override
	public void cleanUpAfterSynchronization(PopulatedRepositoryContent populatedRepositoryContent) 
			throws CleanUpAfterSynchronizationException{
		try {
			deleteFile(new File(populatedRepositoryContent.getLatestDirectoryPath()));
			deleteFile(new File(populatedRepositoryContent.getArchiveDirectoryPath()));
			
			if(!Boolean.valueOf(snapshot)) {
				cleanDirectory(repositoryGenerationDirectory);
			}
		} catch(DeleteFileException e) {
			logger.error(e.getMessage(), e);
			throw new CleanUpAfterSynchronizationException();
		}
	}

	@Override
	public byte[] getReferenceManual(RPackage packageBag) throws GetReferenceManualException {
		final String manualPath = new File(packageBag.getSource()).getParent() 
				+ separator + packageBag.getName() 
				+ separator + packageBag.getName() + ".pdf";
		final File manualFile = new File(manualPath);
		
		try {
			return readFile(manualFile);
		} catch(IOException e) {
			logger.error(e.getMessage(), e);
			throw new GetReferenceManualException(e);
		}
	}

	@Override
	public List<Vignette> getAvailableVignettes(RPackage packageBag) {
		if(packageBag == null) {
			return List.of();
		}
		final List<Vignette> vignettes = new ArrayList<>();
		
		File vignettesFolder = new File(new File(packageBag.getSource()).getParent(),
			packageBag.getName() 
			+ separator + "inst" + separator + "doc" + separator);
		
		//TODO: iteration over it throws NPE
		File[] vignetteFiles = new File[0];
		if(vignettesFolder.exists() && vignettesFolder.isDirectory()) {
			vignetteFiles = vignettesFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return (name != null 
							&& (name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".pdf")));
				}
			});
		}
			
		for(File vignetteFile : vignetteFiles) {
			if(FileNameUtils.getExtension(vignetteFile.getName()).equals("html")) {
				try {
					Document htmlDoc = Jsoup.parse(vignetteFile, "UTF-8");
					
					vignettes.add(new Vignette(htmlDoc.title(), vignetteFile.getName()));
				} catch(IOException e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				vignettes.add(new Vignette(
						FileNameUtils.getBaseName(vignetteFile.getName()), vignetteFile.getName()));
			}
		}
		return vignettes;
	}

	@Override
	public byte[] readVignette(RPackage packageBag, String filename) throws ReadPackageVignetteException {
		final String vignetteFilename = new File(packageBag.getSource()).getParent() 
				+ separator + packageBag.getName() 
				+ separator + "inst" + separator + "doc" 
				+ separator + filename;
		final File file = new File(vignetteFilename);
		
		try {
			return readFile(file);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ReadPackageVignetteException(e);
		}
	}

	@Override
	public boolean isReferenceManualAvailable(RPackage packageBag) {
		final String manualPath = new File(packageBag.getSource()).getParent() 
				+ separator + packageBag.getName() 
				+ separator + packageBag.getName() + ".pdf";
		final File manualFile = new File(manualPath);
		
		return manualFile.exists();
	}

	@Override
	public void calculateCheckSum(RPackage packageBag) throws CheckSumCalculationException {
		logger.debug("Calculating checksum for package: " + packageBag.toString());
		try {
			packageBag.setMd5sum(DigestUtils.md5Hex(new FileInputStream(new File(packageBag.getSource()))));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new CheckSumCalculationException();
		}
	}

	@Override
	public void generateManual(RPackage packageBag) throws GenerateManualException {
		final File targzFile = new File(packageBag.getSource());
		
		if(targzFile == null || !targzFile.exists() 
				|| targzFile.getParentFile() == null || !targzFile.getParentFile().exists()) {
			logger.error("Invalid package source!");
			throw new GenerateManualException(packageBag);
		}
		final String packageName = packageBag.getName().replaceAll("[^a-zA-Z0-9-_]", "");
		final File manualPdf = 
				new File(targzFile.getParent(), packageName + separator + packageName + ".pdf");
		
		if(manualPdf != null && manualPdf.getParentFile() != null && 
				manualPdf.getParentFile().exists()) {
			if(manualPdf.exists()) {
				logger.warn("Manual already exists!");
				return;
			}
			
			ProcessBuilder pb = new ProcessBuilder("R");
			
			pb.command().add("CMD");
			pb.command().add("Rd2pdf");
			pb.command().add("--no-preview");
			pb.command().add("--title=" + packageName);
			pb.command().add("--output=" + packageName + ".pdf");
			pb.command().add(".");
			pb.directory(manualPdf.getParentFile()).redirectErrorStream(true);
			
			Process process = null;
			try {
				process = pb.start();
				BufferedReader out = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				String outputLine = null;
				
				logger.debug("Rd2pdf output: ");
				while((outputLine = out.readLine()) != null)
					logger.debug(outputLine);
				
				int exitValue = process.waitFor();
				if(exitValue != 0) {
					logger.error("Rd2pdf failed with exit code: " + exitValue);
					throw new GenerateManualException(packageBag);
				}
			} catch (IOException | InterruptedException e) {
				logger.error(e.getMessage(), e);
				throw new GenerateManualException(packageBag);
			} finally {
				if(process != null && process.isAlive())
					process.destroyForcibly();
			}
		}
	}
}
