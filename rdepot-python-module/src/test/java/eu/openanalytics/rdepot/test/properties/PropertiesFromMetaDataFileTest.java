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

import eu.openanalytics.rdepot.python.PythonPropertiesParser;
import java.io.File;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PropertiesFromMetaDataFileTest {

    private static final String RESOURCES = "src/test/resources/unit";
    private static final String PANDAS_FILE = RESOURCES + "/test_files/properties_files/PKG-INFO_pandas";
    private static final String NUMPY_FILE = RESOURCES + "/test_files/properties_files/PKG-INFO_numpy";
    private static final String DATE_UTIL_FILE = RESOURCES + "/test_files/properties_files/PKG-INFO_python-dateutil";

    @Test
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public void readPropertiesFromPandasFile() throws Exception {
        final Properties properties = new PythonPropertiesParser(new File(PANDAS_FILE));
        assertEquals(12, properties.size());
        assertEquals("2.1", properties.getProperty("Metadata-Version"));
        assertEquals(
                "BSD 3-Clause License\\n\\nCopyright (c) 2008-2011, AQR Capital Management, LLC, Lambda Foundry, Inc. and PyData Development Team\\nAll rights reserved.\\n\\nCopyright (c) 2011-2023, Open source contributors.\\n\\nRedistribution and use in source and binary forms, with or without\\nmodification, are permitted provided that the following conditions are met:\\n\\n* Redistributions of source code must retain the above copyright notice, this\\n  list of conditions and the following disclaimer.\\n\\n* Redistributions in binary form must reproduce the above copyright notice,\\n  this list of conditions and the following disclaimer in the documentation\\n  and/or other materials provided with the distribution.\\n\\n* Neither the name of the copyright holder nor the names of its\\n  contributors may be used to endorse or promote products derived from\\n  this software without specific prior written permission.\\n\\nTHIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\"\\nAND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE\\nIMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\\nDISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE\\nFOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL\\nDAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR\\nSERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER\\nCAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,\\nOR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\\nOF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.",
                properties.getProperty("License"));
        assertEquals(">=3.8", properties.getProperty("Requires-Python"));
        assertEquals(
                "homepage, https://pandas.pydata.org, documentation, https://pandas.pydata.org/docs/, repository, https://github.com/pandas-dev/pandas",
                properties.getProperty("Project-URL"));
        assertEquals(
                "Development Status :: 5 - Production/Stable, Environment :: Console, Intended Audience :: Science/Research, License :: OSI Approved :: BSD License, Operating System :: OS Independent, Programming Language :: Cython, Programming Language :: Python, Programming Language :: Python :: 3, Programming Language :: Python :: 3 :: Only, Programming Language :: Python :: 3.8, Programming Language :: Python :: 3.9, Programming Language :: Python :: 3.10, Programming Language :: Python :: 3.11, Topic :: Scientific/Engineering",
                properties.getProperty("Classifier"));
        assertEquals("text/markdown", properties.getProperty("Description-Content-Type"));
        assertEquals("pandas", properties.getProperty("Name"));
        assertEquals("The Pandas Development Team <pandas-dev@python.org>", properties.getProperty("Author-email"));
        assertEquals(
                "test, performance, computation, fss, aws, gcp, excel, parquet, feather, hdf5, spss, postgresql, mysql, sql-other, html, xml, plot, output_formatting, clipboard, compression, all",
                properties.getProperty("Provides-Extra"));
        assertEquals("2.0.1", properties.getProperty("Version"));
        assertEquals(
                "Powerful data structures for data analysis, time series, and statistics",
                properties.getProperty("Summary"));
        assertEquals("LICENSE, AUTHORS.md", properties.getProperty("License-File"));
    }

    @Test
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public void readPropertiesFromNumpyFile() throws Exception {
        final Properties properties = new PythonPropertiesParser(new File(NUMPY_FILE));
        assertEquals(14, properties.size());
        assertEquals("2.1", properties.getProperty("Metadata-Version"));
        assertEquals(
                "# numpy-threading-extensions\\nFaster loops for NumPy using multithreading and other tricks. The first release\\nwill target NumPy binary and unary ufuncs. Eventually we will enable overriding\\nother NumPy functions, and provide an C-based (non-Python) API for extending\\nvia third-party functions.\\n\\n[![Travis CI Build Status](https://api.travis-ci.org/Quansight/numpy-threading-extensions.svg)](https://travis-ci.org/Quansight/numpy-threading-extensions)\\n\\n[![Coverage Status](https://codecov.io/gh/Quansight/numpy-threading-extensions/branch/main/graphs/badge.svg)](https://codecov.io/github/Quansight/numpy-threading-extensions)\\n\\n[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)\\n\\n## Installation\\n```\\npip install accelerated_numpy\\n```\\n\\nYou can also install the in-development version 0.0.1 with:\\n```\\npip install https://github.com/Quansight/numpy-threading-extensions/archive/v0.0.1.zip\\n```\\nor latest with\\n```\\npip install https://github.com/Quansight/numpy-threading-extensions/archive/main.zip\\n```\\n\\n## Documentation\\n\\nTo use the project:\\n\\n```python\\n    import accelerated_numpy\\n    accelerated_numpy.initialize()\\n```\\n\\n## Development\\n\\nTo run all the tests run::\\n\\n```\\n    tox\\n```\\n\\nNote, to combine the coverage data from all the tox environments run:\\n\\n OS | Command\\n----|----\\nWindows | `set PYTEST_ADDOPTS=--cov-append`\\n|        | `tox`\\nOther   | `PYTEST_ADDOPTS=--cov-append tox`",
                properties.getProperty("Description"));
        assertEquals("UNKNOWN", properties.getProperty("Platform"));
        assertEquals("MIT", properties.getProperty("License"));
        assertEquals(">=3.6", properties.getProperty("Requires-Python"));
        assertEquals(
                "Changelog, https://github.com/Quansight/numpy-threading-extensions/blob/master/CHANGELOG.rst, Issue Tracker, https://github.com/Quansight/numpy-threading-extensions/issues",
                properties.getProperty("Project-URL"));
        assertEquals(
                "Development Status :: 4 - Beta, Intended Audience :: Developers, License :: OSI Approved :: MIT License, Operating System :: Unix, Operating System :: POSIX, Operating System :: Microsoft :: Windows, Programming Language :: Python, Programming Language :: Python :: 3, Programming Language :: Python :: 3.6, Programming Language :: Python :: 3.7, Programming Language :: Python :: 3.8, Programming Language :: Python :: Implementation :: CPython, Programming Language :: Python :: Implementation :: PyPy, Topic :: Utilities",
                properties.getProperty("Classifier"));
        assertEquals("https://github.com/Quansight/numpy-threading-extensions", properties.getProperty("Home-page"));
        assertEquals("text/markdown", properties.getProperty("Description-Content-Type"));
        assertEquals("accelerated-numpy", properties.getProperty("Name"));
        assertEquals("mattigit@picus.org.il", properties.getProperty("Author-email"));
        assertEquals("0.1.0", properties.getProperty("Version"));
        assertEquals("Matti Picus", properties.getProperty("Author"));
        assertEquals("Faster loops for NumPy using multithreading and other tricks", properties.getProperty("Summary"));
    }

    @Test
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public void readPropertiesFromDateUtilFile() throws Exception {
        final Properties properties = new PythonPropertiesParser(new File(DATE_UTIL_FILE));
        assertEquals(
                "dateutil - powerful extensions to datetime\\n==========================================\\n\\n|pypi| |support| |licence|\\n\\n|gitter| |readthedocs|\\n\\n|travis| |appveyor| |pipelines| |coverage|\\n\\n.. |pypi| image:: https://img.shields.io/pypi/v/python-dateutil.svg?style=flat-square\\n    :target: https://pypi.org/project/python-dateutil/\\n    :alt: pypi version\\n\\n.. |support| image:: https://img.shields.io/pypi/pyversions/python-dateutil.svg?style=flat-square\\n    :target: https://pypi.org/project/python-dateutil/\\n    :alt: supported Python version\\n\\n.. |travis| image:: https://img.shields.io/travis/dateutil/dateutil/master.svg?style=flat-square&label=Travis%20Build\\n    :target: https://travis-ci.org/dateutil/dateutil\\n    :alt: travis build status\\n\\n.. |appveyor| image:: https://img.shields.io/appveyor/ci/dateutil/dateutil/master.svg?style=flat-square&logo=appveyor\\n    :target: https://ci.appveyor.com/project/dateutil/dateutil\\n    :alt: appveyor build status\\n\\n.. |pipelines| image:: https://dev.azure.com/pythondateutilazure/dateutil/_apis/build/status/dateutil.dateutil?branchName=master\\n    :target: https://dev.azure.com/pythondateutilazure/dateutil/_build/latest?definitionId=1&branchName=master\\n    :alt: azure pipelines build status\\n\\n.. |coverage| image:: https://codecov.io/github/dateutil/dateutil/coverage.svg?branch=master\\n    :target: https://codecov.io/github/dateutil/dateutil?branch=master\\n    :alt: Code coverage\\n\\n.. |gitter| image:: https://badges.gitter.im/dateutil/dateutil.svg\\n   :alt: Join the chat at https://gitter.im/dateutil/dateutil\\n   :target: https://gitter.im/dateutil/dateutil\\n\\n.. |licence| image:: https://img.shields.io/pypi/l/python-dateutil.svg?style=flat-square\\n    :target: https://pypi.org/project/python-dateutil/\\n    :alt: licence\\n\\n.. |readthedocs| image:: https://img.shields.io/readthedocs/dateutil/latest.svg?style=flat-square&label=Read%20the%20Docs\\n   :alt: Read the documentation at https://dateutil.readthedocs.io/en/latest/\\n   :target: https://dateutil.readthedocs.io/en/latest/\\n\\nThe `dateutil` module provides powerful extensions to\\nthe standard `datetime` module, available in Python.\\n\\nInstallation\\n============\\n`dateutil` can be installed from PyPI using `pip` (note that the package name is\\ndifferent from the importable name)::\\n\\n    pip install python-dateutil\\n\\nDownload\\n========\\ndateutil is available on PyPI\\nhttps://pypi.org/project/python-dateutil/\\n\\nThe documentation is hosted at:\\nhttps://dateutil.readthedocs.io/en/stable/\\n\\nCode\\n====\\nThe code and issue tracker are hosted on GitHub:\\nhttps://github.com/dateutil/dateutil/\\n\\nFeatures\\n========\\n\\n* Computing of relative deltas (next month, next year,\\n  next Monday, last week of month, etc);\\n* Computing of relative deltas between two given\\n  date and/or datetime objects;\\n* Computing of dates based on very flexible recurrence rules,\\n  using a superset of the `iCalendar <https://www.ietf.org/rfc/rfc2445.txt>`_\\n  specification. Parsing of RFC strings is supported as well.\\n* Generic parsing of dates in almost any string format;\\n* Timezone (tzinfo) implementations for tzfile(5) format\\n  files (/etc/localtime, /usr/share/zoneinfo, etc), TZ\\n  environment string (in all known formats), iCalendar\\n  format files, given ranges (with help from relative deltas),\\n  local machine timezone, fixed offset timezone, UTC timezone,\\n  and Windows registry-based time zones.\\n* Internal up-to-date world timezone information based on\\n  Olson's database.\\n* Computing of Easter Sunday dates for any given year,\\n  using Western, Orthodox or Julian algorithms;\\n* A comprehensive test suite.\\n\\nQuick example\\n=============\\nHere's a snapshot, just to give an idea about the power of the\\npackage. For more examples, look at the documentation.\\n\\nSuppose you want to know how much time is left, in\\nyears/months/days/etc, before the next easter happening on a\\nyear with a Friday 13th in August, and you want to get today's\\ndate out of the \"date\" unix system command. Here is the code:\\n\\n.. code-block:: python3\\n\\n    >>> from dateutil.relativedelta import *\\n    >>> from dateutil.easter import *\\n    >>> from dateutil.rrule import *\\n    >>> from dateutil.parser import *\\n    >>> from datetime import *\\n    >>> now = parse(\"Sat Oct 11 17:13:46 UTC 2003\")\\n    >>> today = now.date()\\n    >>> year = rrule(YEARLY,dtstart=now,bymonth=8,bymonthday=13,byweekday=FR)[0].year\\n    >>> rdelta = relativedelta(easter(year), today)\\n    >>> print(\"Today is: %s\" % today)\\n    Today is: 2003-10-11\\n    >>> print(\"Year with next Aug 13th on a Friday is: %s\" % year)\\n    Year with next Aug 13th on a Friday is: 2004\\n    >>> print(\"How far is the Easter of that year: %s\" % rdelta)\\n    How far is the Easter of that year: relativedelta(months=+6)\\n    >>> print(\"And the Easter of that year is: %s\" % (today+rdelta))\\n    And the Easter of that year is: 2004-04-11\\n\\nBeing exactly 6 months ahead was **really** a coincidence :)\\n\\nContributing\\n============\\n\\nWe welcome many types of contributions - bug reports, pull requests (code, infrastructure or documentation fixes). For more information about how to contribute to the project, see the ``CONTRIBUTING.md`` file in the repository.\\n\\n\\nAuthor\\n======\\nThe dateutil module was written by Gustavo Niemeyer <gustavo@niemeyer.net>\\nin 2003.\\n\\nIt is maintained by:\\n\\n* Gustavo Niemeyer <gustavo@niemeyer.net> 2003-2011\\n* Tomi Pieviläinen <tomi.pievilainen@iki.fi> 2012-2014\\n* Yaron de Leeuw <me@jarondl.net> 2014-2016\\n* Paul Ganssle <paul@ganssle.io> 2015-\\n\\nStarting with version 2.4.1, all source and binary distributions will be signed\\nby a PGP key that has, at the very least, been signed by the key which made the\\nprevious release. A table of release signing keys can be found below:\\n\\n===========  ============================\\nReleases     Signing key fingerprint\\n===========  ============================\\n2.4.1-       `6B49 ACBA DCF6 BD1C A206 67AB CD54 FCE3 D964 BEFB`_ (|pgp_mirror|_)\\n===========  ============================\\n\\n\\nContact\\n=======\\nOur mailing list is available at `dateutil@python.org <https://mail.python.org/mailman/listinfo/dateutil>`_. As it is hosted by the PSF, it is subject to the `PSF code of\\nconduct <https://www.python.org/psf/codeofconduct/>`_.\\n\\nLicense\\n=======\\n\\nAll contributions after December 1, 2017 released under dual license - either `Apache 2.0 License <https://www.apache.org/licenses/LICENSE-2.0>`_ or the `BSD 3-Clause License <https://opensource.org/licenses/BSD-3-Clause>`_. Contributions before December 1, 2017 - except those those explicitly relicensed - are released only under the BSD 3-Clause License.\\n\\n\\n.. _6B49 ACBA DCF6 BD1C A206 67AB CD54 FCE3 D964 BEFB:\\n   https://pgp.mit.edu/pks/lookup?op=vindex&search=0xCD54FCE3D964BEFB\\n\\n.. |pgp_mirror| replace:: mirror\\n.. _pgp_mirror: https://sks-keyservers.net/pks/lookup?op=vindex&search=0xCD54FCE3D964BEFB",
                properties.getProperty("Description"));
    }
}
