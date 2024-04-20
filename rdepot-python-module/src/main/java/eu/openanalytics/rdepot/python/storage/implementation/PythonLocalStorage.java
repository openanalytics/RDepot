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
package eu.openanalytics.rdepot.python.storage.implementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import eu.openanalytics.rdepot.python.storage.exceptions.ReadPythonPackagePkgInfoException;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.PropertiesParser;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.CleanUpAfterSynchronizationException;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateFolderStructureException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.GenerateManualException;
import eu.openanalytics.rdepot.base.storage.exceptions.GetReferenceManualException;
import eu.openanalytics.rdepot.base.storage.exceptions.InvalidSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.LinkFoldersException;
import eu.openanalytics.rdepot.base.storage.exceptions.Md5MismatchException;
import eu.openanalytics.rdepot.base.storage.exceptions.OrganizePackagesException;
import eu.openanalytics.rdepot.base.storage.exceptions.PackageFolderPopulationException;
import eu.openanalytics.rdepot.base.storage.exceptions.ReadPackageDescriptionException;
import eu.openanalytics.rdepot.base.storage.exceptions.ReadPackageVignetteException;
import eu.openanalytics.rdepot.base.storage.implementations.CommonLocalStorage;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.entities.Vignette;
import eu.openanalytics.rdepot.python.mediator.hash.HashCalculator;
import eu.openanalytics.rdepot.python.storage.PythonStorage;
import eu.openanalytics.rdepot.python.storage.indexes.PackageIndexGenerator;
import eu.openanalytics.rdepot.python.storage.indexes.RepositoryIndexGenerator;
import eu.openanalytics.rdepot.python.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.python.synchronization.SynchronizeRepositoryRequestBody;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Local storage implementation for Python.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class PythonLocalStorage extends CommonLocalStorage<PythonRepository, PythonPackage> implements PythonStorage {
	
	@Resource(name="repositoryGenerationDirectory")
	private File repositoryGenerationDirectory;
	
	@Value("${repository-snapshots}")
	private String snapshot;
	
	private final PackageIndexGenerator packageIndexGenerator;
	private final RepositoryIndexGenerator repositoryIndexGenerator;
	
	private Path current;
	
    private final Pattern pythonPackageVersion = Pattern.compile("-([0-9]+[.][0-9]+[.][0-9])");
    
	@Override
	public Properties getPropertiesFromExtractedFile(final String extractedFile) 
			throws ReadPackageDescriptionException {
		try {
			return new PropertiesParser(new File(extractedFile + separator + "PKG-INFO"));
		} catch (IOException e) {
			try {
				deleteFile(new File(extractedFile).getParentFile());
			} catch (DeleteFileException dfe) {
				log.error(dfe.getMessage(), dfe);
			}
			log.error(e.getMessage(), e);
			throw new ReadPythonPackagePkgInfoException();
		}
	}
	
	@Override
	public String generateSubmissionWaitingRoomLocation(File file) {
		return new File(file.getParent() + separator +  StringUtils.substringBeforeLast(file.getName(),".tar.gz"))
		.getAbsolutePath();
	}
	
	public String getRepositoryGeneratedPath(File dateStampFolder, String separator) {
		return dateStampFolder.getAbsolutePath();
	}
	
	private void populateGeneratedFolder(List<PythonPackage> packages, PythonRepository repository, String dateStamp)
			throws PackageFolderPopulationException {
		String folderPath = repositoryGenerationDirectory.getAbsolutePath()
				+ separator + repository.getId()
				+ separator + dateStamp;
		
		for(PythonPackage packageBag : packages ) {
			populatePackage(packageBag, folderPath);
		}
	}
	
	private void createPackageIndexTemplate(String path, PythonPackage packageBag) throws IOException {
	    packageIndexGenerator.createIndexFile(packageBag, path + separator + packageBag.getName());
	    repositoryIndexGenerator.addPackageToIndex(packageBag, path);
	}

	
    private void createFolderStructureForGeneration(List<PythonPackage> packages, PythonRepository repository, String dateStamp)
            throws CreateFolderStructureException {
        File dateStampFolder = null;
        try {
            dateStampFolder = createFolderStructure(
                    repositoryGenerationDirectory.getAbsolutePath()
                            + separator + repository.getId()
                            + separator + dateStamp);

            createFolderStructure(
                    dateStampFolder.getAbsolutePath());
            repositoryIndexGenerator.createIndexFile(repository, dateStampFolder.getAbsolutePath());

            for (PythonPackage packageBag : packages) {
                createFolderStructure(dateStampFolder + separator + packageBag.getName());
                createPackageIndexTemplate(dateStampFolder.toString(), packageBag);
            }


        } catch (CreateFolderStructureException e) {
            if (dateStampFolder != null) {
                try {
                    deleteFile(dateStampFolder);
                } catch (DeleteFileException dfe) {
                    log.error(dfe.getMessage(), dfe);
                }
            }

            throw e;
        } catch (IOException e1) {
            log.error("Could not create index file", e1);
            throw new CreateFolderStructureException();
        }
    }


	@Override
	public void verifySource(PythonPackage packageBag, String newSource) throws InvalidSourceException {}
	
    @Override
    public SynchronizeRepositoryRequestBody buildSynchronizeRequestBody(
            PopulatedRepositoryContent populatedRepositoryContent, 
            List<String> remotePackages,
            PythonRepository repository, String versionBefore) {

        final List<File> packagesToUpload = selectPackagesToUpload(remotePackages, populatedRepositoryContent.getPackages());
        final List<String> packagesToDelete = selectPackagesToDelete(remotePackages, populatedRepositoryContent.getPackages(), packagesToUpload);

        return new SynchronizeRepositoryRequestBody(packagesToUpload, packagesToDelete, versionBefore, repository.getName());
    }

	
    private void addPackageFiles(Package packageBag, List<File> toUpload) {
        String packageFilePath = this.current + separator + packageBag.getName() + separator + packageBag.getFileName();
        String indexFilePath = this.current + separator + packageBag.getName() + separator + "index.html";
        File indexFile = new File(indexFilePath);
        toUpload.add(new File(packageFilePath));
        if (!toUpload.contains(indexFile)) {
            toUpload.add(new File(indexFilePath));
        }
    }
	
    private void addRepositoryIndexFile(List<File> toUpload, List<String> remotePackages, List<PythonPackage> localPackages) {
        Set<String> localPackagesNames = localPackages.stream().map(Package::getName).collect(Collectors.toSet());

        Set<String> remotePackagesNames = remotePackages.stream().map(remotePackage -> {
            Matcher matcher = pythonPackageVersion.matcher(remotePackage);
            if (matcher.find()) {
                return remotePackage.substring(0, matcher.start());
            }
            return remotePackage;
        }).collect(Collectors.toSet());

        if (!remotePackagesNames.containsAll(localPackagesNames) || !localPackagesNames.containsAll(remotePackagesNames)) {
            String indexFilePath = this.current + separator + "index.html";
            File indexFile = new File(indexFilePath);
            if (!toUpload.contains(indexFile)) {
                toUpload.add(indexFile);
            }
        }
    }
	
	protected List<File> selectPackagesToUpload(List<String> remotePackages, List<PythonPackage> localPackages){
		
		List<String> remotePackagesNames = remotePackages.stream().map(packageDetails -> 
			packageDetails.substring(packageDetails.indexOf('/') + 1)
		).collect(Collectors.toList());
		
		List<File> toUpload = new ArrayList<>();
		for(Package packageBag : localPackages) {
			if(!remotePackagesNames.contains(packageBag.getFileName())) {
				addPackageFiles(packageBag, toUpload);
			}
		}
		addRepositoryIndexFile(toUpload, remotePackagesNames, localPackages);		
		return toUpload;
	}
	
	protected List<String> selectPackagesToDelete(List<String> remotePackages, List<PythonPackage> localPackages, List<File> toUpload) {
		List<String> toDelete = new ArrayList<>();
		
		for(String packagePath: remotePackages) {
			boolean found = false;
			for(Package packageBag: localPackages) {
				if((packageBag.getName() + "/" +  packageBag.getFileName()).equals(packagePath)) {
					found = true;
					break;
				}
			}
			
			if(!found) {
				toDelete.add(packagePath);
				if(!localPackages.isEmpty()) {
					addIndexFilesWithoutDeletedPackages(localPackages, packagePath, toUpload);
				}
			}
		}
		return toDelete;
	}
	
	private void addIndexFilesWithoutDeletedPackages(List<PythonPackage> localPackages, String packagePath, List<File> toUpload) {
		String packageName = packagePath.substring(0, packagePath.indexOf('/'));
		
		File packageIndexFile =  new File(this.current + separator + packageName + separator  + "index.html");
		File repositoryIndexFile = new File(this.current + separator + "index.html");
	
		if(packageIndexFile.exists()) {
			toUpload.add(packageIndexFile);
		}
		
		if(repositoryIndexFile.exists() && !toUpload.contains(repositoryIndexFile)){
			boolean uploadIndexFile = true;
			for(Package packageBag: localPackages) {
				if(packageBag.getName().equals(packageName)) {
					uploadIndexFile = false;
					break;
				}
			}
			
			if(uploadIndexFile) {
				toUpload.add(repositoryIndexFile);
			}
		}
	}

	@Override
	public PopulatedRepositoryContent organizePackagesInStorage(String dateStamp, List<PythonPackage> packages, 
			PythonRepository repository) 
					throws OrganizePackagesException {
		try {
            createFolderStructureForGeneration(packages, repository, dateStamp);
            populateGeneratedFolder(packages, repository, dateStamp);
            File current = linkCurrentFolderToGeneratedFolder(repository, dateStamp);
            this.current = Files.readSymbolicLink(Paths.get(current.getAbsolutePath()));
            return new PopulatedRepositoryContent(packages, "");

		} catch (CreateFolderStructureException | PackageFolderPopulationException | LinkFoldersException | IOException e) {
			log.error(e.getMessage(), e);
			throw new OrganizePackagesException();
		}
	}
	
    @Override
    public void populatePackage(PythonPackage packageBag, String folderPath) throws PackageFolderPopulationException {
        if (packageBag.isActive()) {
            String targetFilePath = packageBag.getSource();
            String destinationFilePath = folderPath + separator + packageBag.getName() + separator + packageBag.getName() + "-" + packageBag.getVersion() + ".tar.gz";
            try {
                checkHash(packageBag);
                packageIndexGenerator.addPackageToIndex(packageBag, folderPath + separator + packageBag.getName());
                Files.copy(new File(targetFilePath).toPath(), new File(destinationFilePath).toPath());
            } catch (IOException | Md5MismatchException | CheckSumCalculationException e) {
                log.error("{}: {}", e.getClass(), e.getMessage());
                throw new PackageFolderPopulationException();
            }
        }
    }
	
	private void checkHash(PythonPackage packageBag) throws Md5MismatchException, CheckSumCalculationException {
		HashCalculator hashCalculator = new HashCalculator(packageBag);
		if(!packageBag.getHash().equals(hashCalculator.calculateHash())) {
			throw new Md5MismatchException();
		}
	}

    @Override
    public void cleanUpAfterSynchronization(PopulatedRepositoryContent populatedRepositoryContent)
            throws CleanUpAfterSynchronizationException {
        try {
            if (!Boolean.parseBoolean(snapshot)) {
            	cleanDirectory(repositoryGenerationDirectory);
            }
        } catch (DeleteFileException e) {
            log.error(e.getMessage(), e);
            throw new CleanUpAfterSynchronizationException();
        }
    }

    @Override
    public byte[] getReferenceManual(PythonPackage packageBag) throws GetReferenceManualException {
        final String manualPath = new File(packageBag.getSource()).getParent()
                + separator + packageBag.getName()
                + separator + packageBag.getName() + ".pdf";
        final File manualFile = new File(manualPath);

        try {
            return readFile(manualFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new GetReferenceManualException(e);
        }
    }

    @Override
    public List<Vignette> getAvailableVignettes(PythonPackage packageBag) {
        if (packageBag == null) {
            return List.of();
        }
        final List<Vignette> vignettes = new ArrayList<>();

        File vignettesFolder = new File(new File(packageBag.getSource()).getParent(),
                packageBag.getName()
                        + separator + "inst" + separator + "doc" + separator);

        File[] vignetteFiles = new File[0];
        if(vignettesFolder.exists() && vignettesFolder.isDirectory()) {
            vignetteFiles = vignettesFolder.listFiles((File dir, String name) -> (name != null
                    && (name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".pdf"))));
        }

        for(File vignetteFile : ArrayUtils.nullToEmpty(vignetteFiles, File[].class)) {
            if (FileNameUtils.getExtension(vignetteFile.getName()).equals("html")) {
                try {
                    Document htmlDoc = Jsoup.parse(vignetteFile, "UTF-8");

                    vignettes.add(new Vignette(htmlDoc.title(), vignetteFile.getName()));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                vignettes.add(new Vignette(
                        FileNameUtils.getBaseName(vignetteFile.getName()), vignetteFile.getName()));
            }
        }
        return vignettes;
    }

    @Override
    public byte[] readVignette(PythonPackage packageBag, String filename) throws ReadPackageVignetteException {
        final String vignetteFilename = new File(packageBag.getSource()).getParent()
                + separator + packageBag.getName()
                + separator + "inst" + separator + "doc"
                + separator + filename;
        final File file = new File(vignetteFilename);

        try {
            return readFile(file);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ReadPackageVignetteException(e);
        }
    }

    @Override
    public boolean isReferenceManualAvailable(PythonPackage packageBag) {
        final String manualPath = new File(packageBag.getSource()).getParent()
                + separator + packageBag.getName()
                + separator + packageBag.getName() + ".pdf";
        final File manualFile = new File(manualPath);

        return manualFile.exists();
    }

    @Override
    public void calculateCheckSum(PythonPackage packageBag) throws CheckSumCalculationException {
        log.debug("Calculating checksum for package: {}", packageBag.toString());
        HashCalculator hashCalculator = new HashCalculator(packageBag);
        String hash = hashCalculator.calculateHash();
        packageBag.setHash(hash);
    }

    @Override
    public void generateManual(PythonPackage packageBag) throws GenerateManualException {
        final File targzFile = new File(packageBag.getSource());

        if (!targzFile.exists()
                || targzFile.getParentFile() == null || !targzFile.getParentFile().exists()) {
            log.error("Invalid package source!");
            throw new GenerateManualException(packageBag);
        }
        final String packageName = packageBag.getName().replaceAll("[^a-zA-Z0-9-_]", "");
        final File manualPdf =
                new File(targzFile.getParent(), packageName + separator + packageName + ".pdf");

        if (manualPdf.getParentFile() != null &&
                manualPdf.getParentFile().exists()) {
            if (manualPdf.exists()) {
                log.warn("Manual already exists!");
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
                String outputLine;

                log.debug("Rd2pdf output: ");
                while ((outputLine = out.readLine()) != null)
                    log.debug(outputLine);

                int exitValue = process.waitFor();
                if (exitValue != 0) {
                    log.error("Rd2pdf failed with exit code: {}", exitValue);
                    throw new GenerateManualException(packageBag);
                }
            } catch (IOException | InterruptedException e) {
                log.error(e.getMessage(), e);
                throw new GenerateManualException(packageBag);
            } finally {
                if (process != null && process.isAlive())
                    process.destroyForcibly();
            }
        }
    }
}
