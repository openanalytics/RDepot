/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.test.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.utils.PackagesFileParser;
import eu.openanalytics.rdepot.r.utils.exceptions.ParsePackagesFileException;
import eu.openanalytics.rdepot.test.context.TestWebApplicationContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;

@ExtendWith(MockitoExtension.class)
public class PackagesFileParserTest {
    @Mock
    protected MessageSource messageSource;

    @InjectMocks
    protected StaticMessageResolver staticMessageResolver;

    private static final PackagesFileParser parser = new PackagesFileParser();

    private static final String TEST_FILES_PATH = "src/test/resources/unit/test_files/packages_file_parser_test";

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
    }

    @Test
    public void parse_emptyFile() throws Exception {
        final String testFilePath = TEST_FILES_PATH + "/empty-file";
        final File testFile = new File(testFilePath);

        final List<RPackage> results = parser.parse(testFile);
        assertEquals(0, results.size(), "Invalid number of packages parsed.");
    }

    @Test
    public void parse_emptyFileWithOneEmptyLine() throws Exception {
        final String testFilePath = TEST_FILES_PATH + "/empty-file-with-one-empty-line";
        final File testFile = new File(testFilePath);

        final List<RPackage> results = parser.parse(testFile);
        assertEquals(0, results.size(), "Invalid number of packages parsed.");
    }

    @Test
    public void parse_emptyFileWithManyEmptyLines() throws Exception {
        final String testFilePath = TEST_FILES_PATH + "/empty-file-with-many-empty-lines";
        final File testFile = new File(testFilePath);

        final List<RPackage> results = parser.parse(testFile);
        assertEquals(0, results.size(), "Invalid number of packages parsed.");
    }

    @Test
    public void parse_regularFileWithNoEmptyLineBelow() throws Exception {
        final String testFilePath = TEST_FILES_PATH + "/no-empty-lines-in-the-end";
        final File testFile = new File(testFilePath);

        final List<RPackage> results = parser.parse(testFile);
        assertEquals(5, results.size(), "Invalid number of packages parsed.");

        final RPackage package1 = new RPackage();
        package1.setName("A3");
        package1.setVersion("0.9.2");
        package1.setDepends("R (>= 2.15.0), xtable, pbapply");
        package1.setLicense("GPL (>= 2)");
        package1.setMd5sum("76d726aee8dd7c6ed94d150d5718015b");
        package1.setNeedsCompilation(false);
        assertPackage(package1, results.get(0));

        final RPackage package2 = new RPackage();
        package2.setName("Benchmarking");
        package2.setVersion("0.10");
        package2.setDepends("lpSolveAPI, ucminf");
        package2.setLicense("GPL (>= 2)");
        package2.setMd5sum("9a99c2ebefa6d49422ca7893c1f4ead8");
        package2.setNeedsCompilation(false);
        assertPackage(package2, results.get(1));

        final RPackage package3 = new RPackage();
        package3.setName("abc");
        package3.setVersion("1.3");
        package3.setDepends("R (>= 2.10), nnet, quantreg, locfit");
        package3.setLicense("GPL (>= 3)");
        package3.setMd5sum("c47d18b86b331a5023dcd62b74fedbb6");
        package3.setNeedsCompilation(false);
        assertPackage(package3, results.get(2));

        final RPackage package4 = new RPackage();
        package4.setName("bea.R");
        package4.setVersion("1.0.5");
        package4.setDepends("R (>= 3.2.1), data.table");
        package4.setImports(
                "httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2, stringr, chron, gtable, scales, htmltools, httpuv, xtable, stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace, plyr, yaml");
        package4.setLicense("CC0");
        package4.setMd5sum("5e664f320c7cc884138d64467f6b0e49");
        package4.setNeedsCompilation(false);
        assertPackage(package4, results.get(3));

        final RPackage package5 = new RPackage();
        package5.setName("usl");
        package5.setVersion("2.0.0");
        package5.setDepends("R (>= 3.0), methods");
        package5.setImports("graphics, stats, nlsr");
        package5.setLicense("BSD_2_clause + file LICENSE");
        package5.setMd5sum("868140a3c3c29327eef5d5a485aee5b6");
        package5.setNeedsCompilation(false);
        assertPackage(package5, results.get(4));
    }

    @Test
    public void parse_regularFileWithEmptyLinesInTheEnd() throws Exception {
        final String testFilePath = TEST_FILES_PATH + "/empty-lines-in-the-end";
        final File testFile = new File(testFilePath);

        final List<RPackage> results = parser.parse(testFile);
        assertEquals(5, results.size(), "Invalid number of packages parsed.");

        final RPackage package1 = new RPackage();
        package1.setName("A3");
        package1.setVersion("0.9.2");
        package1.setDepends("R (>= 2.15.0), xtable, pbapply");
        package1.setLicense("GPL (>= 2)");
        package1.setMd5sum("76d726aee8dd7c6ed94d150d5718015b");
        package1.setNeedsCompilation(false);
        assertPackage(package1, results.get(0));

        final RPackage package2 = new RPackage();
        package2.setName("Benchmarking");
        package2.setVersion("0.10");
        package2.setDepends("lpSolveAPI, ucminf");
        package2.setLicense("GPL (>= 2)");
        package2.setMd5sum("9a99c2ebefa6d49422ca7893c1f4ead8");
        package2.setNeedsCompilation(false);
        assertPackage(package2, results.get(1));

        final RPackage package3 = new RPackage();
        package3.setName("abc");
        package3.setVersion("1.3");
        package3.setDepends("R (>= 2.10), nnet, quantreg, locfit");
        package3.setLicense("GPL (>= 3)");
        package3.setMd5sum("c47d18b86b331a5023dcd62b74fedbb6");
        package3.setNeedsCompilation(false);
        assertPackage(package3, results.get(2));

        final RPackage package4 = new RPackage();
        package4.setName("bea.R");
        package4.setVersion("1.0.5");
        package4.setDepends("R (>= 3.2.1), data.table");
        package4.setImports(
                "httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2, stringr, chron, gtable, scales, htmltools, httpuv, xtable, stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace, plyr, yaml");
        package4.setLicense("CC0");
        package4.setMd5sum("5e664f320c7cc884138d64467f6b0e49");
        package4.setNeedsCompilation(false);
        assertPackage(package4, results.get(3));

        final RPackage package5 = new RPackage();
        package5.setName("usl");
        package5.setVersion("2.0.0");
        package5.setDepends("R (>= 3.0), methods");
        package5.setImports("graphics, stats, nlsr");
        package5.setLicense("BSD_2_clause + file LICENSE");
        package5.setMd5sum("868140a3c3c29327eef5d5a485aee5b6");
        package5.setNeedsCompilation(false);
        assertPackage(package5, results.get(4));
    }

    private void assertPackage(RPackage expected, RPackage actual) {
        assertEquals(expected.getName(), actual.getName(), "Invalid package name.");
        assertEquals(expected.getVersion(), actual.getVersion(), "Invalid package version.");
        assertEquals(expected.getDepends(), actual.getDepends(), "Invalid package dependency list.");
        assertEquals(expected.getLicense(), actual.getLicense(), "Invalid package license.");
        assertEquals(expected.getMd5sum(), actual.getMd5sum(), "Invalid package MD5 sum.");
        assertEquals(
                expected.isNeedsCompilation(),
                actual.isNeedsCompilation(),
                "Invalid package \"needsCompilation\" flag.");
        assertEquals(expected.getImports(), actual.getImports(), "Invalid package imports.");
    }

    @Test
    public void parse_regularFileWithManyEmptyLinesInTheEnd() throws Exception {
        final String testFilePath = TEST_FILES_PATH + "/many-empty-lines-in-the-end";
        final File testFile = new File(testFilePath);

        final List<RPackage> results = parser.parse(testFile);
        assertEquals(5, results.size(), "Invalid number of packages parsed.");

        final RPackage package1 = new RPackage();
        package1.setName("A3");
        package1.setVersion("0.9.2");
        package1.setDepends("R (>= 2.15.0), xtable, pbapply");
        package1.setLicense("GPL (>= 2)");
        package1.setMd5sum("76d726aee8dd7c6ed94d150d5718015b");
        package1.setNeedsCompilation(false);
        assertPackage(package1, results.get(0));

        final RPackage package2 = new RPackage();
        package2.setName("Benchmarking");
        package2.setVersion("0.10");
        package2.setDepends("lpSolveAPI, ucminf");
        package2.setLicense("GPL (>= 2)");
        package2.setMd5sum("9a99c2ebefa6d49422ca7893c1f4ead8");
        package2.setNeedsCompilation(false);
        assertPackage(package2, results.get(1));

        final RPackage package3 = new RPackage();
        package3.setName("abc");
        package3.setVersion("1.3");
        package3.setDepends("R (>= 2.10), nnet, quantreg, locfit");
        package3.setLicense("GPL (>= 3)");
        package3.setMd5sum("c47d18b86b331a5023dcd62b74fedbb6");
        package3.setNeedsCompilation(false);
        assertPackage(package3, results.get(2));

        final RPackage package4 = new RPackage();
        package4.setName("bea.R");
        package4.setVersion("1.0.5");
        package4.setDepends("R (>= 3.2.1), data.table");
        package4.setImports(
                "httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2, stringr, chron, gtable, scales, htmltools, httpuv, xtable, stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace, plyr, yaml");
        package4.setLicense("CC0");
        package4.setMd5sum("5e664f320c7cc884138d64467f6b0e49");
        package4.setNeedsCompilation(false);
        assertPackage(package4, results.get(3));

        final RPackage package5 = new RPackage();
        package5.setName("usl");
        package5.setVersion("2.0.0");
        package5.setDepends("R (>= 3.0), methods");
        package5.setImports("graphics, stats, nlsr");
        package5.setLicense("BSD_2_clause + file LICENSE");
        package5.setMd5sum("868140a3c3c29327eef5d5a485aee5b6");
        package5.setNeedsCompilation(false);
        assertPackage(package5, results.get(4));
    }

    @Test
    public void parse_regularFileWithManyEmptyLinesInTheBeginningAndEnd() throws Exception {
        final String testFilePath = TEST_FILES_PATH + "/many-empty-lines-in-the-beginning-and-end";
        final File testFile = new File(testFilePath);

        final List<RPackage> results = parser.parse(testFile);
        assertEquals(5, results.size(), "Invalid number of packages parsed.");

        final RPackage package1 = new RPackage();
        package1.setName("A3");
        package1.setVersion("0.9.2");
        package1.setDepends("R (>= 2.15.0), xtable, pbapply");
        package1.setLicense("GPL (>= 2)");
        package1.setMd5sum("76d726aee8dd7c6ed94d150d5718015b");
        package1.setNeedsCompilation(false);
        assertPackage(package1, results.get(0));

        final RPackage package2 = new RPackage();
        package2.setName("Benchmarking");
        package2.setVersion("0.10");
        package2.setDepends("lpSolveAPI, ucminf");
        package2.setLicense("GPL (>= 2)");
        package2.setMd5sum("9a99c2ebefa6d49422ca7893c1f4ead8");
        package2.setNeedsCompilation(false);
        assertPackage(package2, results.get(1));

        final RPackage package3 = new RPackage();
        package3.setName("abc");
        package3.setVersion("1.3");
        package3.setDepends("R (>= 2.10), nnet, quantreg, locfit");
        package3.setLicense("GPL (>= 3)");
        package3.setMd5sum("c47d18b86b331a5023dcd62b74fedbb6");
        package3.setNeedsCompilation(false);
        assertPackage(package3, results.get(2));

        final RPackage package4 = new RPackage();
        package4.setName("bea.R");
        package4.setVersion("1.0.5");
        package4.setDepends("R (>= 3.2.1), data.table");
        package4.setImports(
                "httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2, stringr, chron, gtable, scales, htmltools, httpuv, xtable, stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace, plyr, yaml");
        package4.setLicense("CC0");
        package4.setMd5sum("5e664f320c7cc884138d64467f6b0e49");
        package4.setNeedsCompilation(false);
        assertPackage(package4, results.get(3));

        final RPackage package5 = new RPackage();
        package5.setName("usl");
        package5.setVersion("2.0.0");
        package5.setDepends("R (>= 3.0), methods");
        package5.setImports("graphics, stats, nlsr");
        package5.setLicense("BSD_2_clause + file LICENSE");
        package5.setMd5sum("868140a3c3c29327eef5d5a485aee5b6");
        package5.setNeedsCompilation(false);
        assertPackage(package5, results.get(4));
    }

    @Test
    public void parse_regularFileWithBlanksLinesInBetween() throws Exception {
        final String testFilePath = TEST_FILES_PATH + "/blank-lines-in-between";
        final File testFile = new File(testFilePath);

        final List<RPackage> results = parser.parse(testFile);
        assertEquals(5, results.size(), "Invalid number of packages parsed.");

        final RPackage package1 = new RPackage();
        package1.setName("A3");
        package1.setVersion("0.9.2");
        package1.setDepends("R (>= 2.15.0), xtable, pbapply");
        package1.setLicense("GPL (>= 2)");
        package1.setMd5sum("76d726aee8dd7c6ed94d150d5718015b");
        package1.setNeedsCompilation(false);
        assertPackage(package1, results.get(0));

        final RPackage package2 = new RPackage();
        package2.setName("Benchmarking");
        package2.setVersion("0.10");
        package2.setDepends("lpSolveAPI, ucminf");
        package2.setLicense("GPL (>= 2)");
        package2.setMd5sum("9a99c2ebefa6d49422ca7893c1f4ead8");
        package2.setNeedsCompilation(false);
        assertPackage(package2, results.get(1));

        final RPackage package3 = new RPackage();
        package3.setName("abc");
        package3.setVersion("1.3");
        package3.setDepends("R (>= 2.10), nnet, quantreg, locfit");
        package3.setLicense("GPL (>= 3)");
        package3.setMd5sum("c47d18b86b331a5023dcd62b74fedbb6");
        package3.setNeedsCompilation(false);
        assertPackage(package3, results.get(2));

        final RPackage package4 = new RPackage();
        package4.setName("bea.R");
        package4.setVersion("1.0.5");
        package4.setDepends("R (>= 3.2.1), data.table");
        package4.setImports(
                "httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2, stringr, chron, gtable, scales, htmltools, httpuv, xtable, stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace, plyr, yaml");
        package4.setLicense("CC0");
        package4.setMd5sum("5e664f320c7cc884138d64467f6b0e49");
        package4.setNeedsCompilation(false);
        assertPackage(package4, results.get(3));

        final RPackage package5 = new RPackage();
        package5.setName("usl");
        package5.setVersion("2.0.0");
        package5.setDepends("R (>= 3.0), methods");
        package5.setImports("graphics, stats, nlsr");
        package5.setLicense("BSD_2_clause + file LICENSE");
        package5.setMd5sum("868140a3c3c29327eef5d5a485aee5b6");
        package5.setNeedsCompilation(false);
        assertPackage(package5, results.get(4));
    }

    @Test
    public void parse_regularFileWithWeirdImportLine() throws Exception {
        final String testFilePath = TEST_FILES_PATH + "/weird-import";
        final File testFile = new File(testFilePath);

        final List<RPackage> results = parser.parse(testFile);
        assertEquals(5, results.size(), "Invalid number of packages parsed.");

        final RPackage package1 = new RPackage();
        package1.setName("A3");
        package1.setVersion("0.9.2");
        package1.setDepends("R (>= 2.15.0), xtable, pbapply");
        package1.setLicense("GPL (>= 2)");
        package1.setMd5sum("76d726aee8dd7c6ed94d150d5718015b");
        package1.setNeedsCompilation(false);
        assertPackage(package1, results.get(0));

        final RPackage package2 = new RPackage();
        package2.setName("Benchmarking");
        package2.setVersion("0.10");
        package2.setDepends("lpSolveAPI, ucminf");
        package2.setLicense("GPL (>= 2)");
        package2.setMd5sum("9a99c2ebefa6d49422ca7893c1f4ead8");
        package2.setNeedsCompilation(false);
        assertPackage(package2, results.get(1));

        final RPackage package3 = new RPackage();
        package3.setName("abc");
        package3.setVersion("1.3");
        package3.setDepends("R (>= 2.10), nnet, quantreg, locfit");
        package3.setLicense("GPL (>= 3)");
        package3.setMd5sum("c47d18b86b331a5023dcd62b74fedbb6");
        package3.setNeedsCompilation(false);
        assertPackage(package3, results.get(2));

        final RPackage package4 = new RPackage();
        package4.setName("bea.R");
        package4.setVersion("1.0.5");
        package4.setDepends("R (>= 3.2.1), data.table");
        package4.setImports(
                "httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2, stringr, chron, gtable, scales, htmltools, httpuv, xtable, stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace, plyr, yaml");
        package4.setLicense("CC0");
        package4.setMd5sum("5e664f320c7cc884138d64467f6b0e49");
        package4.setNeedsCompilation(false);
        assertPackage(package4, results.get(3));

        final RPackage package5 = new RPackage();
        package5.setName("usl");
        package5.setVersion("2.0.0");
        package5.setDepends("R (>= 3.0), methods");
        package5.setImports("graphics, stats, nlsr");
        package5.setLicense("BSD_2_clause + file LICENSE");
        package5.setMd5sum("868140a3c3c29327eef5d5a485aee5b6");
        package5.setNeedsCompilation(false);
        assertPackage(package5, results.get(4));
    }

    @Test
    public void parse_regularFileWithInvalidLines() {
        final String testFilePath = TEST_FILES_PATH + "/invalid-file";
        final File testFile = new File(testFilePath);

        assertThrows(ParsePackagesFileException.class, () -> parser.parse(testFile));
    }
}
