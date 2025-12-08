/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.test.unit;

import static org.mockito.ArgumentMatchers.any;

import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.test.context.TestWebApplicationContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;

@ExtendWith(MockitoExtension.class)
public abstract class UnitTest {

    @Mock
    protected MessageSource messageSource;

    @BeforeEach
    public void setUp() throws Exception {
        // This piece of code is used mainly to provide mock message source for static methods
        MockServletContext msc = new MockServletContext();
        msc.addInitParameter(ContextLoader.CONTEXT_CLASS_PARAM, TestWebApplicationContext.class.getName());
        ServletContextListener listener = new ContextLoaderListener();
        ServletContextEvent event = new ServletContextEvent(msc);
        listener.contextInitialized(event);
        Mockito.lenient()
                .when(messageSource.getMessage(any(), any(), any(), any()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(0));
        new StaticMessageResolver(messageSource);
        backupTestPackages();
    }

    protected void backupTestPackages() throws Exception {
        final File main = new File("src/test/resources/unit/test_packages");
        final File backup = new File("src/test/resources/unit/test_packages_backup");
        if (!main.exists()) FileUtils.copyDirectory(backup, main);
        if (!backup.exists()) FileUtils.copyDirectory(main, backup);
    }

    @AfterEach
    public void restoreTestPackages() throws Exception {
        FileUtils.forceDelete(new File("src/test/resources/unit/test_packages"));
        FileUtils.moveDirectory(
                new File("src/test/resources/unit/test_packages_backup"),
                new File("src/test/resources/unit/test_packages"));
    }
}
