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

import java.io.File;
import java.util.Properties;

import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.exception.DeleteFileException;
import eu.openanalytics.rdepot.exception.ExtractFileException;
import eu.openanalytics.rdepot.exception.GetFileInBytesException;
import eu.openanalytics.rdepot.exception.ManualCreateException;
import eu.openanalytics.rdepot.exception.Md5SumCalculationException;
import eu.openanalytics.rdepot.exception.MovePackageSourceException;
import eu.openanalytics.rdepot.exception.PackageDescriptionNotFound;
import eu.openanalytics.rdepot.exception.PackageSourceNotFoundException;
import eu.openanalytics.rdepot.exception.PackageStorageException;
import eu.openanalytics.rdepot.exception.SourceFileDeleteException;
import eu.openanalytics.rdepot.exception.WriteToDiskFromMultipartException;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;

public interface PackageStorage {
	
	/**
	 * This method creates manuals for given package.
	 * @param packageBag Package which manuals are created for
	 * @throws ManualCreateException 
	 * @throws PackageStorageException
	 */
	public void createManuals(Package packageBag) throws ManualCreateException;
	
	/**
	 * This method deletes source of given package.
	 * @param packageBag Package to delete
	 * @throws SourceFileDeleteException 
	 * @throws PackageStorageException
	 */
	public void deleteSource(Package packageBag) throws SourceFileDeleteException;
	
	/**
	 * This method returns package file in raw bytes.
	 * @param packageBag
	 * @return package file in raw bytes.
	 * @throws GetFileInBytesException
	 */
	public byte[] getPackageInBytes(Package packageBag) throws GetFileInBytesException;
	
	/**
	 * This method returns and creates if necessary reference manual for a given package.
	 * Output format is simply raw bytes.
	 * @param packageBag Package whose manual is returned
	 * @return Manual in bytes
	 * @throws ManualCreateException 
	 * @throws GetFileInBytesException 
	 */
	public byte[] getReferenceManualFileInBytes(Package packageBag) throws ManualCreateException, GetFileInBytesException;
	
	/**
	 * This method calculates package's md5 sum.
	 * @param packageBag package to calculate
	 * @return String containing md5 sum
	 * @throws Md5SumCalculationException 
	 */
	public String calculatePackageMd5Sum(Package packageBag) throws Md5SumCalculationException;
	
	/**
	 * This method reads existing vignette.
	 * @param packageBag Package whose vignette is read.
	 * @param filename Name of vignette's file
	 * @return Vignette in bytes
	 * @throws GetFileInBytesException 
	 */
	public byte[] readVignette(Package packageBag, String filename) throws GetFileInBytesException;
	
	/**
	 * This method saves package in so-called "waiting room" where it stays 
	 * until it's accepted by administrator
	 * @param multipartFile Multipart containing uploaded file.
	 * @param packageId ID of uploaded package - needed to provide uniqueness
	 * @return Package file
	 * @throws DeleteFileException 
	 * @throws WriteToDiskFromMultipartException 
	 */
	public File writeToWaitingRoom(MultipartFile muiltipartFile, Repository repository) throws WriteToDiskFromMultipartException;

	/**
	 * This method saves uploaded package in the storage.
	 * @param multipartFile Multipart containing uploaded file.
	 * @param repository Repository which package should be uploaded to
	 * @return Package file
	 * @throws DeleteFileException 
	 * @throws WriteToDiskFromMultipartException 
	 */
	public File writeToDisk(MultipartFile multipartFile, Repository repository) throws WriteToDiskFromMultipartException, DeleteFileException;
	
	/**
	 * This method moves a package source from waiting directory to the main one.
	 * It should be called after submission is accepted.
	 * @param packageBag Package to be moved
	 * @return new package source directory
	 * @throws MovePackageSourceException 
	 * @throws PackageSourceNotFoundException
	 */
	public File moveToMainDirectory(Package packageBag) throws PackageSourceNotFoundException, MovePackageSourceException;
	
	/**
	 * This method moves a package from its current directory to the new one.
	 * @param packageBag Package to be moved
	 * @param destination New package source directory
	 * @return New package source directory
	 * @throws PackageSourceNotFoundException
	 * @throws MovePackageSourceException
	 */
	public File moveSource(Package packageBag, String destinationDir) throws PackageSourceNotFoundException, MovePackageSourceException;
	
	/**
	 * This method verifies if package source path is correct.
	 * @param packageBag Related package
	 * @param source New package source path
	 * @throws PackageStorageException when directory is incorrect.
	 */
	
	public File extractPackageFile(File file) throws ExtractFileException;
	public void verifySource(Package packageBag, String source) throws PackageStorageException;
	public Properties readPackageDescription(File file) throws PackageStorageException;
	public File getDescriptionFile(File extracted, String packageName) throws PackageDescriptionNotFound;
	public String calculateFileMd5Sum(String path) throws Md5SumCalculationException;
}
