/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.annotation.Resource;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
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
import eu.openanalytics.rdepot.exception.CreateTemporaryFolderException;
import eu.openanalytics.rdepot.exception.DeleteFileException;
import eu.openanalytics.rdepot.exception.DownloadFileException;
import eu.openanalytics.rdepot.exception.ExtractFileException;
import eu.openanalytics.rdepot.exception.GetFileInBytesException;
import eu.openanalytics.rdepot.exception.GzipFileException;
import eu.openanalytics.rdepot.exception.LinkFoldersException;
import eu.openanalytics.rdepot.exception.Md5SumCalculationException;
import eu.openanalytics.rdepot.exception.MoveFileException;
import eu.openanalytics.rdepot.exception.StorageException;
import eu.openanalytics.rdepot.exception.WriteToDiskException;
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
		File link = new File(linkPath);
		
		try {
			if(Files.exists(Paths.get(linkPath))) {
				Files.delete(Paths.get(linkPath));
			}
			Files.createSymbolicLink(Paths.get(linkPath), Paths.get(targetPath));
		} catch (IOException e) {
			throw new LinkFoldersException(messageSource, locale, targetPath, linkPath, e.getMessage());
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
		File source = new File(path);
		File destination = new File(path + ".gz");
		try (GzipCompressorOutputStream compressor = new GzipCompressorOutputStream(new FileOutputStream(destination))){
            IOUtils.copy(new FileInputStream(source), compressor);
        } catch (IOException e) {
			throw new GzipFileException(messageSource, locale, path, e.getMessage());
		}	
		return destination;
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
				
				return bytes;
			} catch (IOException e) {
				throw new GetFileInBytesException(messageSource, locale, source, e.getMessage());
			}
		} else {
			throw new FileNotFoundException();
		}
	}

	public File extractFile(File file) throws ExtractFileException {
		final File outputDir = file.getParentFile();
		File unGzippedFile = null;
		try {
			unGzippedFile = unGzip(file, outputDir);
			unTar(unGzippedFile, outputDir);
		} catch (IOException | ArchiveException | IllegalStateException e) {
			try {
				if(file.getParentFile().exists())
					deleteFile(file.getParentFile().getAbsolutePath());
			} catch (DeleteFileException dfe) {
					logger.error(dfe.getClass() + ": " + dfe.getMessage());
			}
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new ExtractFileException(messageSource, locale, file.getAbsolutePath(), e.getMessage());
		} finally {
			try {
				if(unGzippedFile != null && unGzippedFile.exists()) {
					deleteFile(unGzippedFile.getAbsolutePath());
				}
			} catch (DeleteFileException e) {
				logger.error(e.getClass() + ": " + e.getMessage(), e);
			}
		}
		return file;
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
		logger.info(String.format("Ungzipping %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));
		
		final File outputFile = new File(outputDir, inputFile.getName().substring(0, inputFile.getName().length() - 3));

	    final GZIPInputStream in = new GZIPInputStream(new FileInputStream(inputFile));
	    final FileOutputStream out = new FileOutputStream(outputFile);

	    IOUtils.copy(in, out);

	    in.close();
	    out.close();

	    return outputFile;
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
		logger.info(String.format("Untarring %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));
		
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
		
		logger.info("Moving from " + source.getAbsolutePath() + " to "
				+ destination.getAbsolutePath());
		
		try {
			if(source.isDirectory()) {
			  FileUtils.moveDirectory(source, destination);
			} else {
			  FileUtils.moveFile(source, destination);
			}
		} catch (NullPointerException | IOException e) {
		  e.printStackTrace();
			throw new MoveFileException(messageSource, locale, source.getAbsolutePath(), destination.getAbsolutePath(), e.getMessage());
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
	
	/**
	 * This method reads data from a file and saves it on the disk.
	 * @param file
	 * @param destinationDir
	 * @param filename
	 * @return
	 * @throws WriteToDiskException
	 */
	public File writeToDisk(File file, File destinationDir) throws WriteToDiskException {
		return writeToDisk(file, destinationDir, file.getName());
	}
	
	/**
	 * This method reads data from a file and saves it on the disk.
	 * @param file
	 * @param destinationDir
	 * @param filename
	 * @return
	 * @throws WriteToDiskException
	 */
	public File writeToDisk(File file, File destinationDir, String filename) throws WriteToDiskException {
		File destinationFile = new File(destinationDir.getAbsolutePath() + separator + filename);
		
		try {
			FileUtils.forceMkdir(destinationDir);
			FileUtils.moveFile(file, destinationFile);
			
			return destinationFile;
		} catch (IOException e) {
			try {
				deleteFile(destinationDir.getAbsolutePath());
			} catch (DeleteFileException dfe) {
				logger.error(dfe.getMessage());
			}
			throw new WriteToDiskException(messageSource, locale, 
					file, destinationDir.getAbsolutePath(), e.getMessage());
		}
	}
	
	/**
	 * Downloads file from a given URL.
	 * @param url
	 * @param destinationFile
	 * @return downloaded file
	 * @throws DownloadFileException 
	 */
	public File downloadFile(String url, File destinationFile) throws DownloadFileException {
		try {
			FileUtils.copyURLToFile(new URL(url), destinationFile);
		} catch (IOException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DownloadFileException(messageSource, locale, url, destinationFile.getAbsolutePath());
		}		
		return destinationFile;
	}

	/**
	 * Downloads file from a given URL and saves it in a temporary directory.
	 * @param url
	 * @return downloaded file
	 * @throws DownloadFileException
	 */
	public File downloadFile(String url) throws DownloadFileException {
		try {
			File tempFile = Files.createTempFile(null, null).toFile();
			return downloadFile(url, tempFile);
		} catch (IOException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DownloadFileException(messageSource, locale, url, "Could not create temporary file");
		}
	}
	
	/**
	 * Creates a temporary folder with a given prefix.
	 * @param prefix
	 * @return
	 * @throws CreateTemporaryFolderException
	 */
	public File createTemporaryFolder(String prefix) throws CreateTemporaryFolderException {
		try {
			return Files.createTempDirectory(prefix).toFile();
		} catch (IOException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new CreateTemporaryFolderException(messageSource, locale, prefix);
		}
	}
}
