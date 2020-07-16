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
package eu.openanalytics.rdepot.repo;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import eu.openanalytics.rdepot.repo.storage.StorageService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StorageService storageService;

    @Test
    public void shouldSaveUploadedFile() throws Exception {
        MockMultipartFile[] multipartFiles =
                {new MockMultipartFile("files", "test.txt", "text/plain", "A Text File".getBytes())};
        this.mvc.perform(fileUpload("/").file(multipartFiles[0]))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        then(this.storageService).should().store(multipartFiles, "");
    }
    
    @Test
    public void shouldSaveUploadedFileInArchive() throws Exception {
    	MockMultipartFile[] multipartFiles =
    		{new MockMultipartFile("files", "test.txt", "text/plain", "A Text File".getBytes())};
    	this.mvc.perform(fileUpload("/archive").file(multipartFiles[0]))
    			.andExpect(status().isOk())
    			.andExpect(content().string("OK"));
    	
    	then(this.storageService).should().storeInArchive(multipartFiles, "");
    }

}
