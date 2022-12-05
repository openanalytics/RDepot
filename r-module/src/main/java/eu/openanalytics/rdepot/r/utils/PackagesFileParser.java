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
package eu.openanalytics.rdepot.r.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.openanalytics.rdepot.r.utils.exceptions.ParsePackagesFileException;
import eu.openanalytics.rdepot.r.entities.RPackage;

/**
 * Parses PACKAGES file which indicates what file are stored in CRAN repository.
 */
public class PackagesFileParser {
	
	Logger logger = LoggerFactory.getLogger(PackagesFileParser.class);
	
	private void setValue(RPackage packageBag, String key, String value) {
		if(key.equals("Package")) {
			packageBag.setName(value);
		} else if(key.equals("Version")) {
			packageBag.setVersion(value);
		} else if(key.equals("Depends")) {
			packageBag.setDepends(value);
		} else if(key.equals("Imports")) {
			packageBag.setImports(value);
		} else if(key.equals("License")) {
			packageBag.setLicense(value);
		} else if(key.equals("MD5sum")) {
			packageBag.setMd5sum(value);
		}
	}

	/**
	 * Parses PACKAGES file.
	 * @param packagesFile
	 * @return list of packages assumed to be in the repository
	 * @throws ParsePackagesFileException
	 */
	public List<RPackage> parse(File packagesFile) throws ParsePackagesFileException {
		List<RPackage> packages = new ArrayList<>();
		String line = null;
		String key = "";
		String value = "";
		
		try(BufferedReader br = new BufferedReader(new FileReader(packagesFile))) {
			RPackage packageBag = new RPackage();
			
			
			while((line = br.readLine()) != null) {
				if(line.equals("")) {
					packages.add(packageBag);
					packageBag = new RPackage();
				} else if(line.startsWith(" ")) {
					String trimmed = line.trim();
					value += " " + trimmed;
					
					setValue(packageBag, key, value);
				} else {
					String[] parsed = line.split(": ");
					key = parsed[0];
					if(parsed.length == 2) {
						value = parsed[1];
					} else {
						value = "";
					}
					
					setValue(packageBag, key, value);
				}
			}
			
			packages.add(packageBag);
			
		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new ParsePackagesFileException(line);
		}
		
		return packages;
	}
	
}
