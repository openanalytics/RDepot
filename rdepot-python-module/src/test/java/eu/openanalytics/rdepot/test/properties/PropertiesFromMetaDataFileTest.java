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
package eu.openanalytics.rdepot.test.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.openanalytics.rdepot.base.PropertiesParser;
import java.io.File;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PropertiesFromMetaDataFileTest {

    private static final String JSON_PATH = "src/test/resources/unit";
    private static final String PANDAS_FILE = JSON_PATH + "/test_files/properties_files/PKG-INFO_pandas";
    private static final String NUMPY_FILE = JSON_PATH + "/test_files/properties_files/PKG-INFO_numpy";

    @Test
    public void readPropertiesFromPandasFile() throws Exception {
        final Properties properties = new PropertiesParser(new File(PANDAS_FILE));
        assertEquals(12, properties.size());
        assertEquals("2.1", properties.getProperty("Metadata-Version"));
        assertEquals(
                "BSD 3-Clause License\\n\\n Copyright (c) 2008-2011, AQR Capital Management, LLC, Lambda Foundry, Inc. and PyData Development Team\\n All rights reserved.\\n\\n Copyright (c) 2011-2023, Open source contributors.\\n\\n Redistribution and use in source and binary forms, with or without\\n modification, are permitted provided that the following conditions are met:\\n\\n * Redistributions of source code must retain the above copyright notice, this\\n list of conditions and the following disclaimer.\\n\\n * Redistributions in binary form must reproduce the above copyright notice,\\n this list of conditions and the following disclaimer in the documentation\\n and/or other materials provided with the distribution.\\n\\n * Neither the name of the copyright holder nor the names of its\\n contributors may be used to endorse or promote products derived from\\n this software without specific prior written permission.\\n\\n THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"\\n AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE\\n IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\\n DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE\\n FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL\\n DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR\\n SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER\\n CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,\\n OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\\n OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.",
                properties.getProperty("License"));
        assertEquals(properties.getProperty("Requires-Python"), ">=3.8");
        assertEquals(
                properties.getProperty("Project-URL"),
                "homepage, https://pandas.pydata.org, documentation, https://pandas.pydata.org/docs/, repository, https://github.com/pandas-dev/pandas");
        assertEquals(
                properties.getProperty("Classifier"),
                "Development Status :: 5 - Production/Stable, Environment :: Console, Intended Audience :: Science/Research, License :: OSI Approved :: BSD License, Operating System :: OS Independent, Programming Language :: Cython, Programming Language :: Python, Programming Language :: Python :: 3, Programming Language :: Python :: 3 :: Only, Programming Language :: Python :: 3.8, Programming Language :: Python :: 3.9, Programming Language :: Python :: 3.10, Programming Language :: Python :: 3.11, Topic :: Scientific/Engineering");
        assertEquals(properties.getProperty("Description-Content-Type"), "text/markdown");
        assertEquals(properties.getProperty("Name"), "pandas");
        assertEquals(properties.getProperty("Author-email"), "The Pandas Development Team <pandas-dev@python.org>");
        assertEquals(
                properties.getProperty("Provides-Extra"),
                "test, performance, computation, fss, aws, gcp, excel, parquet, feather, hdf5, spss, postgresql, mysql, sql-other, html, xml, plot, output_formatting, clipboard, compression, all");
        assertEquals(properties.getProperty("Version"), "2.0.1");
        assertEquals(
                properties.getProperty("Summary"),
                "Powerful data structures for data analysis, time series, and statistics");
        assertEquals(properties.getProperty("License-File"), "LICENSE, AUTHORS.md");
    }

    @Test
    public void readPropertiesFromNumpyFile() throws Exception {
        final Properties properties = new PropertiesParser(new File(NUMPY_FILE));
        assertEquals(properties.size(), 14);
        assertEquals(properties.getProperty("Metadata-Version"), "2.1");
        assertEquals(
                properties.getProperty("Description"),
                "# numpy-threading-extensions\\n Faster loops for NumPy using multithreading and other tricks. The first release\\n will target NumPy binary and unary ufuncs. Eventually we will enable overriding\\n other NumPy functions, and provide an C-based (non-Python) API for extending\\n via third-party functions.\\n\\n [![Travis CI Build Status](https://api.travis-ci.org/Quansight/numpy-threading-extensions.svg)](https://travis-ci.org/Quansight/numpy-threading-extensions)\\n\\n [![Coverage Status](https://codecov.io/gh/Quansight/numpy-threading-extensions/branch/main/graphs/badge.svg)](https://codecov.io/github/Quansight/numpy-threading-extensions)\\n\\n [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)\\n\\n ## Installation\\n ```\\n pip install accelerated_numpy\\n ```\\n\\n You can also install the in-development version 0.0.1 with:\\n ```\\n pip install https://github.com/Quansight/numpy-threading-extensions/archive/v0.0.1.zip\\n ```\\n or latest with\\n ```\\n pip install https://github.com/Quansight/numpy-threading-extensions/archive/main.zip\\n ```\\n\\n ## Documentation\\n\\n To use the project:\\n\\n ```python\\n import accelerated_numpy\\n accelerated_numpy.initialize()\\n ```\\n\\n ## Development\\n\\n To run all the tests run::\\n\\n ```\\n tox\\n ```\\n\\n Note, to combine the coverage data from all the tox environments run:\\n\\n OS | Command\\n ----|----\\n Windows | `set PYTEST_ADDOPTS=--cov-append`\\n | | `tox`\\n Other | `PYTEST_ADDOPTS=--cov-append tox`");
        assertEquals(properties.getProperty("Platform"), "UNKNOWN");
        assertEquals(properties.getProperty("License"), "MIT");
        assertEquals(properties.getProperty("Requires-Python"), ">=3.6");
        assertEquals(
                properties.getProperty("Project-URL"),
                "Changelog, https://github.com/Quansight/numpy-threading-extensions/blob/master/CHANGELOG.rst, Issue Tracker, https://github.com/Quansight/numpy-threading-extensions/issues");
        assertEquals(
                properties.getProperty("Classifier"),
                "Development Status :: 4 - Beta, Intended Audience :: Developers, License :: OSI Approved :: MIT License, Operating System :: Unix, Operating System :: POSIX, Operating System :: Microsoft :: Windows, Programming Language :: Python, Programming Language :: Python :: 3, Programming Language :: Python :: 3.6, Programming Language :: Python :: 3.7, Programming Language :: Python :: 3.8, Programming Language :: Python :: Implementation :: CPython, Programming Language :: Python :: Implementation :: PyPy, Topic :: Utilities");
        assertEquals(properties.getProperty("Home-page"), "https://github.com/Quansight/numpy-threading-extensions");
        assertEquals(properties.getProperty("Description-Content-Type"), "text/markdown");
        assertEquals(properties.getProperty("Name"), "accelerated-numpy");
        assertEquals(properties.getProperty("Author-email"), "mattigit@picus.org.il");
        assertEquals(properties.getProperty("Version"), "0.1.0");
        assertEquals(properties.getProperty("Author"), "Matti Picus");
        assertEquals(properties.getProperty("Summary"), "Faster loops for NumPy using multithreading and other tricks");
    }
}
