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
package eu.openanalytics.rdepot.repo.model;

public class Upload {
	private String fileName = "";
	private String md5Sum = "";
	
	public String getFileName() {
		return fileName;
	}
	
	public String getMd5Sum() {
		return md5Sum;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setMd5Sum(String md5Sum) {
		this.md5Sum = md5Sum;
	}
	
	public Upload(String fileName, String md5Sum) {
		super();
		this.fileName = fileName;
		this.md5Sum = md5Sum;
	}
	
	public Upload() {}
}
