/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.storage.implementations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.keycloak.adapters.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateFolderStructureException;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateTemporaryFolderException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.DownloadFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.ExtractFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.GzipFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.LinkFoldersException;
import eu.openanalytics.rdepot.base.storage.exceptions.Md5SumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.MoveFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceFileDeleteException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceNotFoundException;

public abstract class CommonLocalStorage<R extends Repository<R, ?>, P extends Package<P, ?>> 
	implements Storage<R, P> {
	
	public Logger logger = LoggerFactory.getLogger(getClass());
	protected final String separator =  FileSystems.getDefault().getSeparator();
	
	@Override
	public String extractTarGzPackageFile(String storedFilePath) throws ExtractFileException {
		logger.debug("Extracting package file: " + storedFilePath);
		final File storedFile = new File(storedFilePath);
		final File outputDir = storedFile.getParentFile();
		File unGzippedFile = null;
		
		try {
			unGzippedFile = unGzip(storedFile, outputDir);
			unTar(unGzippedFile, outputDir);
		} catch(IOException | ArchiveException e) {
			try {
				if(storedFile.getParentFile().exists())
					deleteFile(storedFile.getParentFile().getAbsoluteFile());
			} catch(DeleteFileException dfe) {
				logger.error(dfe.getMessage(), dfe);
			}
			logger.error(e.getMessage(), e);
			throw new ExtractFileException();
		} finally {
			try {
				if(unGzippedFile != null && unGzippedFile.exists()) {
					deleteFile(unGzippedFile);
				}
			} catch(DeleteFileException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return new File(storedFile.getParent() + separator + storedFile.getName().split("_")[0])
				.getAbsolutePath();
	}
	
	protected void cleanDirectory(File directory) throws DeleteFileException {
		if(directory.exists() && directory.isDirectory()) {
			try {
				FileUtils.cleanDirectory(directory);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new DeleteFileException();
			}
		}
	}
	
	protected void deleteFile(File file) throws DeleteFileException {
		if(file.exists()) {
			try {
				FileUtils.forceDelete(file);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new DeleteFileException();
			}
		}
	}

	/** Untar an input file into an output file.
	 * The output file is created in the output folder, having the same name
	 * as the input file, minus the '.tar' extension. 
	 * 
	 * @param inputFile     the input .tar file
	 * @param outputDir     the output directory file. 
	 * @throws IOException 
	 * @throws ArchiveException 
	 */
	private void unTar(final File inputFile, final File outputDir) throws IOException, ArchiveException {
		logger.debug(String.format("Untarring %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));
		//TODO: Log or debug?
	    final InputStream is = new FileInputStream(inputFile); 
	    final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
	    TarArchiveEntry entry = null; 
	    
	    while ((entry = (TarArchiveEntry)debInputStream.getNextEntry()) != null) {
	        final File outputFile = new File(outputDir, entry.getName());
	        File outputFileParentDir = outputFile.getParentFile();
	        
	        if(!outputFileParentDir.exists()) {
	        	outputFileParentDir.mkdirs();
	        }
	        
	        if (entry.isDirectory()) {	            
	            if (!outputFile.exists()) {
	                if (!outputFile.mkdirs()) {
	                    throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
	                }
	            }
	        } else {
	            final OutputStream outputFileStream = new FileOutputStream(outputFile); 
	            IOUtils.copy(debInputStream, outputFileStream);
	            outputFileStream.close();
	        }
	    }
	    debInputStream.close();
	}
	/**
	 * Ungzip an input file into an output file.
	 * The output file is created in the output folder, having the same name
	 * as the input file, minus the '.gz' extension. 	 * 
	 * @param inputFile     the input .gz file
	 * @param outputDir     the output directory file.
	 * @return  The {@File} with the ungzipped content. 
	 * @throws IOException 
	 */
	private File unGzip(final File inputFile, final File outputDir) throws IOException {
		logger.debug(String.format("Ungzipping %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));
		
		final File outputFile = new File(outputDir, inputFile.getName().substring(0, inputFile.getName().length() - 3));

	    final GZIPInputStream in = new GZIPInputStream(new FileInputStream(inputFile));
	    final FileOutputStream out = new FileOutputStream(outputFile);

	    IOUtils.copy(in, out);

	    in.close();
	    out.close();

	    return outputFile;
	}

	protected File move(File source, File destination) throws MoveFileException {
		try {
			if(!destination.getParentFile().exists())
				createFolderStructure(destination.getParent());
			
			logger.debug("Moving from " + source.getAbsolutePath() + " to "
					+ destination.getAbsolutePath());
			
			if(source.isDirectory()) {
			  FileUtils.moveDirectory(source, destination);
			} else {
			  FileUtils.moveFile(source, destination);
			}
		} catch (NullPointerException | IOException | CreateFolderStructureException e) {
			e.printStackTrace();
			throw new MoveFileException();
		}
		
		return destination;
	}
	
	/**
	 * This method creates a new directory in application's storage or overrides the existing one.
	 * @param path Path to the directory
	 * @return Created directory
	 * @throws CreateFolderStructureException
	 */
	protected File createFolderStructure(String path) throws CreateFolderStructureException {
		File newFolder = new File(path);
		try {
			if(!newFolder.exists()) {
				Files.createDirectories(newFolder.toPath());
			} else {
				FileUtils.cleanDirectory(newFolder);
			}
			
		} catch(IOException e) {
			logger.error(e.getMessage(), e);
			throw new CreateFolderStructureException();
		}
		return newFolder;
	}

	@Override
	public void removeFileIfExists(final String sourceFilePath) throws DeleteFileException {
		deleteFile(new File(sourceFilePath));
	}

	@Override
	public void removePackageSource(P packageBag) throws SourceFileDeleteException {
		logger.info("Removing source for package: " + packageBag.toString());
		removePackageSource(packageBag.getSource());
	}
	
	@Override
	public void removePackageSource(String path) throws SourceFileDeleteException {
		final File targzFile = new File(path);
		logger.info("Removing source file: " + targzFile.getAbsolutePath());
		try {
			deleteFile(targzFile);
		} catch (DeleteFileException e) {
			logger.error(e.getMessage(), e);
			throw new SourceFileDeleteException();
		}
	}
	
	/**
	 * This method creates a compressed copy of a given file.
	 * @param source Source file
	 * @return Compressed file
	 * @throws GzipFileException 
	 */
	protected File gzipFile(final File source) throws GzipFileException {
		File destination = new File(source.getAbsolutePath() + ".gz");
		try (GzipCompressorOutputStream compressor = new GzipCompressorOutputStream(new FileOutputStream(destination))){
            IOUtils.copy(new FileInputStream(source), compressor);
        } catch (IOException e) {
			throw new GzipFileException();
		}	
		return destination;
	}
	
	/**
	 * This method calculates MD5 sum of a file.
	 * @param target
	 * @return
	 * @throws Md5SumCalculationException
	 */
	protected String calculateMd5Sum(File target) throws Md5SumCalculationException {
		byte[] bytes;
		
		try {
			bytes = Files.readAllBytes(target.toPath());
		} catch(IOException e) {
			logger.error(e.getMessage(), e);
			throw new Md5SumCalculationException();
		}
		
		return DigestUtils.md5DigestAsHex(bytes);
	}
	
	/**
	 * This method creates a symbolic link to the given directory.
	 * @param targetPath Path to the directory we are linking to
	 * @param linkPath Path to the link
	 * @return created link
	 * @throws LinkFoldersException
	 */
	protected File linkTwoFolders(String targetPath, String linkPath) throws LinkFoldersException {
		File link = new File(linkPath);
		
		try {
			if(Files.exists(Paths.get(linkPath))) {
				Files.delete(Paths.get(linkPath));
			}
			Files.createSymbolicLink(Paths.get(linkPath), Paths.get(targetPath));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new LinkFoldersException();
		}		

		return link;
	}
	
	/**
	 * Downloads file from a given URL and saves it in a temporary directory.
	 * @param url
	 * @return downloaded file
	 * @throws DownloadFileException
	 */
	public File downloadFile(String url) throws DownloadFileException {
		File tempFile;
		try {
			tempFile = Files.createTempFile(null, null).toFile();
		} catch (IOException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DownloadFileException(url);
		}
		downloadFileToDestination(url, tempFile);
		return tempFile;
	}
	
	private void downloadFileToDestination(String url, File destination) throws DownloadFileException {
		try {
			final HttpClient httpClient = new HttpClientBuilder().build();
			final HttpGet httpGet = new HttpGet(url);
			final HttpResponse response = httpClient.execute(httpGet);
			final HttpEntity entity = response.getEntity();
			
			if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new DownloadFileException(url);
			}
			
			if(entity != null) {
				final FileOutputStream os = new FileOutputStream(destination);
				try {
					entity.writeTo(os);
				} catch(IOException e) {
					throw e;
				} finally {
					os.close();
				}
			}
		} catch(IOException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DownloadFileException(url);
		}
	}

	/**
	 * Downloads file from a given url.
	 * @param url
	 * @param destination
	 * @return
	 * @throws DownloadFileException
	 */
	public MultipartFile downloadFile(String url, File destination) throws DownloadFileException {
		downloadFileToDestination(url, destination);
		return new MultipartFile() {
			
			@Override
			public void transferTo(File dest) throws IOException, IllegalStateException {
				FileCopyUtils.copy(getInputStream(), Files.newOutputStream(dest.toPath()));
			}
			
			@Override
			public boolean isEmpty() {
				return false;
			}
			
			@Override
			public long getSize() {
				try {
					return Files.size(destination.toPath());
				} catch (IOException e) {
					logger.error(e.getMessage(),e);
					return -1;
				}
			}
			
			@Override
			public String getOriginalFilename() {
				String[] tokens = url.split("/");
				if(tokens.length == 0)
					return "downloaded";
				return tokens[tokens.length - 1];
			}
			
			@Override
			public String getName() {
				return getOriginalFilename();
			}
			
			@Override
			public InputStream getInputStream() throws IOException {
				return new FileInputStream(destination);
			}
			
			@Override
			public String getContentType() {
				return "application/gzip";
			}
			
			@Override
			public byte[] getBytes() throws IOException {
				return FileUtils.readFileToByteArray(destination);
			}
		};
	}
	
	/**
	 * Creates temporary folder with a given prefix.
	 * @param prefix
	 * @return
	 * @throws CreateTemporaryFolderException
	 */
	public File createTemporaryFolder(String prefix) throws CreateTemporaryFolderException {
		try {
			return Files.createTempDirectory(prefix).toFile();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new CreateTemporaryFolderException(prefix);
		}
	}
	
	/**
	 * Reads file to a byte array.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	protected byte[] readFile(File file) throws IOException {
		final FileSystemResource fsResource = new FileSystemResource(file);
		if(fsResource != null && fsResource.exists()) {
			return Files.readAllBytes(fsResource.getFile().toPath());			
		} else {
			throw new FileNotFoundException();
		}
	}
	
	@Override
	public byte[] getPackageInBytes(P packageBag) throws SourceNotFoundException {
		try {
			return readFile(new File(packageBag.getSource()));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new SourceNotFoundException();
		}
	}
}
