/**
 * RDepot
 *
 * Copyright (C) 2012-2017 Open Analytics NV
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
package eu.openanalytics.rdepot.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class FileService 
{
	public void linkFileTo(File original, File link) throws IOException, InterruptedException
	{
//		if(!link.exists())
//		{
//			Files.createSymbolicLink(link.toPath(), original.toPath());
//		}
		Process p;
		p = Runtime.getRuntime().exec("ln -frs " + original.getAbsolutePath() + " " + link.getAbsolutePath());
		p.waitFor();
        if(p.exitValue() != 0)
        	throw new IOException("file.exception.link");
        p.destroy();
	}
	
	public String calculateMd5Sum(File file) throws IOException
	{
		FileInputStream fip = new FileInputStream(file);
		byte[] bytes = IOUtils.toByteArray(fip);
		return DigestUtils.md5DigestAsHex(bytes);
	}

	public void gzipFile(File packagesFile) throws IOException, InterruptedException 
	{
		Process p;
		p = Runtime.getRuntime().exec("gzip -f " + packagesFile.getAbsolutePath());
		p.waitFor();
        if(p.exitValue() != 0)
        	throw new IOException("file.exception.gzip");
        p.destroy();
	}
}
