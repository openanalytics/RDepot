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
    private static final String ACCRUED_FILE = JSON_PATH + "/test_files/properties_files/DESCRIPTION_accrued";
    private static final String DATA_TABLE_FILE = JSON_PATH + "/test_files/properties_files/DESCRIPTION_data_table";
    private static final String MATRIX_FILE = JSON_PATH + "/test_files/properties_files/DESCRIPTION_matrix";
    private static final String GGALLY_FILE = JSON_PATH + "/test_files/properties_files/DESCRIPTION_ggally";
    private static final String ACTUAR_FILE = JSON_PATH + "/test_files/properties_files/DESCRIPTION_actuar";

    @Test
    public void readPropertiesFromAccruedFile() throws Exception {
        final Properties properties = new PropertiesParser(new File(ACCRUED_FILE));
        assertEquals(14, properties.size());
        assertEquals(
                "Package for visualizing data quality of partially accruing data.",
                properties.getProperty("Description"));
        assertEquals("2015-07-20 18:44:41 UTC; jreaton", properties.getProperty("Packaged"));
        assertEquals("GPL-3", properties.getProperty("License"));
        assertEquals("Data Quality Visualization Tools for Partially Accruing Data", properties.getProperty("Title"));
        assertEquals("2015-07-20", properties.getProperty("Date"));
        assertEquals("Julie Eaton <jreaton@uw.edu>", properties.getProperty("Maintainer"));
        assertEquals("Package", properties.getProperty("Type"));
        assertEquals("CRAN", properties.getProperty("Repository"));
        assertEquals("1.3.5", properties.getProperty("Version"));
        assertEquals("Julie Eaton and Ian Painter", properties.getProperty("Author"));
        assertEquals("no", properties.getProperty("NeedsCompilation"));
        assertEquals("accrued", properties.getProperty("Package"));
        assertEquals("R (>= 2.14.1), grid", properties.getProperty("Depends"));
        assertEquals("2015-07-20 22:40:00", properties.getProperty("Date/Publication"));
    }

    @Test
    public void readPropertiesFromDataTableFile() throws Exception {
        final Properties properties = new PropertiesParser(new File(DATA_TABLE_FILE));
        assertEquals(20, properties.size());
        assertEquals(
                "Fast aggregation of large data (e.g. 100GB in RAM), fast ordered joins, fast add/modify/delete of columns by group using no copies at all, list columns, friendly and fast character-separated-value read/write. Offers a natural and flexible syntax, for faster development.",
                properties.getProperty("Description"));
        assertEquals("2023-02-16 16:37:18 UTC; mdowle", properties.getProperty("Packaged"));
        assertEquals("MPL-2.0 | file LICENSE", properties.getProperty("License"));
        assertEquals("Extension of `data.frame`", properties.getProperty("Title"));
        assertEquals("methods", properties.getProperty("Imports"));
        assertEquals("zlib", properties.getProperty("SystemRequirements"));
        assertEquals(
                "https://r-datatable.com, https://Rdatatable.gitlab.io/data.table,\\n https://github.com/Rdatatable/data.table",
                properties.getProperty("URL"));
        assertEquals("https://github.com/Rdatatable/data.table/issues", properties.getProperty("BugReports"));
        assertEquals("TRUE", properties.getProperty("ByteCompile"));
        assertEquals("knitr", properties.getProperty("VignetteBuilder"));
        assertEquals("Matt Dowle <mattjdowle@gmail.com>", properties.getProperty("Maintainer"));
        assertEquals("CRAN", properties.getProperty("Repository"));
        assertEquals("1.14.8", properties.getProperty("Version"));
        assertEquals(
                "bit64 (>= 4.0.0), bit (>= 4.0.4), curl, R.utils, xts,\\n nanotime, zoo (>= 1.8-1), yaml, knitr, rmarkdown",
                properties.getProperty("Suggests"));
        assertEquals("yes", properties.getProperty("NeedsCompilation"));
        assertEquals(
                "Matt Dowle [aut, cre],\\n Arun Srinivasan [aut],\\n Jan Gorecki [ctb],\\n Michael Chirico [ctb],\\n Pasha Stetsenko [ctb],\\n Tom Short [ctb],\\n Steve Lianoglou [ctb],\\n Eduard Antonyan [ctb],\\n Markus Bonsch [ctb],\\n Hugh Parsonage [ctb],\\n Scott Ritchie [ctb],\\n Kun Ren [ctb],\\n Xianying Tan [ctb],\\n Rick Saporta [ctb],\\n Otto Seiskari [ctb],\\n Xianghui Dong [ctb],\\n Michel Lang [ctb],\\n Watal Iwasaki [ctb],\\n Seth Wenchel [ctb],\\n Karl Broman [ctb],\\n Tobias Schmidt [ctb],\\n David Arenburg [ctb],\\n Ethan Smith [ctb],\\n Francois Cocquemas [ctb],\\n Matthieu Gomez [ctb],\\n Philippe Chataignon [ctb],\\n Nello Blaser [ctb],\\n Dmitry Selivanov [ctb],\\n Andrey Riabushenko [ctb],\\n Cheng Lee [ctb],\\n Declan Groves [ctb],\\n Daniel Possenriede [ctb],\\n Felipe Parages [ctb],\\n Denes Toth [ctb],\\n Mus Yaramaz-David [ctb],\\n Ayappan Perumal [ctb],\\n James Sams [ctb],\\n Martin Morgan [ctb],\\n Michael Quinn [ctb],\\n @javrucebo [ctb],\\n @marc-outins [ctb],\\n Roy Storey [ctb],\\n Manish Saraswat [ctb],\\n Morgan Jacob [ctb],\\n Michael Schubmehl [ctb],\\n Davis Vaughan [ctb],\\n Toby Hocking [ctb],\\n Leonardo Silvestri [ctb],\\n Tyson Barrett [ctb],\\n Jim Hester [ctb],\\n Anthony Damico [ctb],\\n Sebastian Freundt [ctb],\\n David Simons [ctb],\\n Elliott Sales de Andrade [ctb],\\n Cole Miller [ctb],\\n Jens Peder Meldgaard [ctb],\\n Vaclav Tlapak [ctb],\\n Kevin Ushey [ctb],\\n Dirk Eddelbuettel [ctb],\\n Ben Schwen [ctb]",
                properties.getProperty("Author"));
        assertEquals("data.table", properties.getProperty("Package"));
        assertEquals(
                "c(\\n person(\"Matt\",\"Dowle\", role=c(\"aut\",\"cre\"), email=\"mattjdowle@gmail.com\"),\\n person(\"Arun\",\"Srinivasan\", role=\"aut\", email=\"asrini@pm.me\"),\\n person(\"Jan\",\"Gorecki\", role=\"ctb\"),\\n person(\"Michael\",\"Chirico\", role=\"ctb\"),\\n person(\"Pasha\",\"Stetsenko\", role=\"ctb\"),\\n person(\"Tom\",\"Short\", role=\"ctb\"),\\n person(\"Steve\",\"Lianoglou\", role=\"ctb\"),\\n person(\"Eduard\",\"Antonyan\", role=\"ctb\"),\\n person(\"Markus\",\"Bonsch\", role=\"ctb\"),\\n person(\"Hugh\",\"Parsonage\", role=\"ctb\"),\\n person(\"Scott\",\"Ritchie\", role=\"ctb\"),\\n person(\"Kun\",\"Ren\", role=\"ctb\"),\\n person(\"Xianying\",\"Tan\", role=\"ctb\"),\\n person(\"Rick\",\"Saporta\", role=\"ctb\"),\\n person(\"Otto\",\"Seiskari\", role=\"ctb\"),\\n person(\"Xianghui\",\"Dong\", role=\"ctb\"),\\n person(\"Michel\",\"Lang\", role=\"ctb\"),\\n person(\"Watal\",\"Iwasaki\", role=\"ctb\"),\\n person(\"Seth\",\"Wenchel\", role=\"ctb\"),\\n person(\"Karl\",\"Broman\", role=\"ctb\"),\\n person(\"Tobias\",\"Schmidt\", role=\"ctb\"),\\n person(\"David\",\"Arenburg\", role=\"ctb\"),\\n person(\"Ethan\",\"Smith\", role=\"ctb\"),\\n person(\"Francois\",\"Cocquemas\", role=\"ctb\"),\\n person(\"Matthieu\",\"Gomez\", role=\"ctb\"),\\n person(\"Philippe\",\"Chataignon\", role=\"ctb\"),\\n person(\"Nello\",\"Blaser\", role=\"ctb\"),\\n person(\"Dmitry\",\"Selivanov\", role=\"ctb\"),\\n person(\"Andrey\",\"Riabushenko\", role=\"ctb\"),\\n person(\"Cheng\",\"Lee\", role=\"ctb\"),\\n person(\"Declan\",\"Groves\", role=\"ctb\"),\\n person(\"Daniel\",\"Possenriede\", role=\"ctb\"),\\n person(\"Felipe\",\"Parages\", role=\"ctb\"),\\n person(\"Denes\",\"Toth\", role=\"ctb\"),\\n person(\"Mus\",\"Yaramaz-David\", role=\"ctb\"),\\n person(\"Ayappan\",\"Perumal\", role=\"ctb\"),\\n person(\"James\",\"Sams\", role=\"ctb\"),\\n person(\"Martin\",\"Morgan\", role=\"ctb\"),\\n person(\"Michael\",\"Quinn\", role=\"ctb\"),\\n person(\"@javrucebo\",\"\", role=\"ctb\"),\\n person(\"@marc-outins\",\"\", role=\"ctb\"),\\n person(\"Roy\",\"Storey\", role=\"ctb\"),\\n person(\"Manish\",\"Saraswat\", role=\"ctb\"),\\n person(\"Morgan\",\"Jacob\", role=\"ctb\"),\\n person(\"Michael\",\"Schubmehl\", role=\"ctb\"),\\n person(\"Davis\",\"Vaughan\", role=\"ctb\"),\\n person(\"Toby\",\"Hocking\", role=\"ctb\"),\\n person(\"Leonardo\",\"Silvestri\", role=\"ctb\"),\\n person(\"Tyson\",\"Barrett\", role=\"ctb\"),\\n person(\"Jim\",\"Hester\", role=\"ctb\"),\\n person(\"Anthony\",\"Damico\", role=\"ctb\"),\\n person(\"Sebastian\",\"Freundt\", role=\"ctb\"),\\n person(\"David\",\"Simons\", role=\"ctb\"),\\n person(\"Elliott\",\"Sales de Andrade\", role=\"ctb\"),\\n person(\"Cole\",\"Miller\", role=\"ctb\"),\\n person(\"Jens Peder\",\"Meldgaard\", role=\"ctb\"),\\n person(\"Vaclav\",\"Tlapak\", role=\"ctb\"),\\n person(\"Kevin\",\"Ushey\", role=\"ctb\"),\\n person(\"Dirk\",\"Eddelbuettel\", role=\"ctb\"),\\n person(\"Ben\",\"Schwen\", role=\"ctb\"))",
                properties.getProperty("Authors@R"));
        assertEquals("R (>= 3.1.0)", properties.getProperty("Depends"));
        assertEquals("2023-02-17 12:20:12 UTC", properties.getProperty("Date/Publication"));
    }

    @Test
    public void readPropertiesFromMatrixFile() throws Exception {
        final Properties properties = new PropertiesParser(new File(MATRIX_FILE));
        assertEquals(26, properties.size());
        assertEquals(
                String.join(
                        "",
                        "A rich hierarchy of sparse and dense matrix classes,\\n ",
                        "including general, symmetric, triangular, and diagonal matrices\\n ",
                        "with numeric, logical, or pattern entries. Efficient methods for\\n ",
                        "operating on such matrices, often wrapping the 'BLAS', 'LAPACK',\\n ",
                        "and 'SuiteSparse' libraries."),
                properties.getProperty("Description"));
        assertEquals("2024-01-11 08:36:29 UTC; maechler", properties.getProperty("Packaged"));
        assertEquals("GPL (>= 2) | file LICENCE", properties.getProperty("License"));
        assertEquals("Sparse and Dense Matrix Classes and Methods", properties.getProperty("Title"));
        assertEquals("grDevices, graphics, grid, lattice, stats, utils", properties.getProperty("Imports"));
        assertEquals("https://Matrix.R-forge.R-project.org", properties.getProperty("URL"));
        assertEquals(
                "https://R-forge.R-project.org/tracker/?atid=294&group_id=61", properties.getProperty("BugReports"));
        assertEquals("Martin Maechler <mmaechler+Matrix@gmail.com>", properties.getProperty("Maintainer"));
        assertEquals("CRAN", properties.getProperty("Repository"));
        assertEquals("1.6-5", properties.getProperty("Version"));
        assertEquals("MASS, datasets, sfsmisc, tools", properties.getProperty("Suggests"));
        assertEquals("yes", properties.getProperty("NeedsCompilation"));
        assertEquals(
                String.join(
                        "",
                        "Douglas Bates [aut] (<https://orcid.org/0000-0001-8316-9503>),\\n ",
                        "Martin Maechler [aut, cre] (<https://orcid.org/0000-0002-8685-9910>),\\n ",
                        "Mikael Jagan [aut] (<https://orcid.org/0000-0002-3542-2938>),\\n ",
                        "Timothy A. Davis [ctb] (<https://orcid.org/0000-0001-7614-6899>,\\n ",
                        "SuiteSparse libraries, notably CHOLMOD and AMD, collaborators\\n ",
                        "listed in dir(pattern=\"^[A-Z]+[.]txt$\", full.names=TRUE,\\n ",
                        "system.file(\"doc\", \"SuiteSparse\", package=\"Matrix\"))),\\n ",
                        "Jens Oehlschlägel [ctb] (initial nearPD()),\\n ",
                        "Jason Riedy [ctb] (<https://orcid.org/0000-0002-4345-4200>, GNU\\n ",
                        "Octave's condest() and onenormest(), Copyright: Regents of the\\n ",
                        "University of California),\\n ",
                        "R Core Team [ctb] (base R's matrix implementation)"),
                properties.getProperty("Author"));
        assertEquals("Matrix", properties.getProperty("Package"));
        assertEquals(
                String.join(
                        "",
                        "c(person(\"Douglas\", \"Bates\", role = \"aut\",\\n ",
                        "comment = c(ORCID = \"0000-0001-8316-9503\")),\\n ",
                        "person(\"Martin\", \"Maechler\", role = c(\"aut\", \"cre\"),\\n ",
                        "email = \"mmaechler+Matrix@gmail.com\",\\n ",
                        "comment = c(ORCID = \"0000-0002-8685-9910\")),\\n ",
                        "person(\"Mikael\", \"Jagan\", role = \"aut\",\\n ",
                        "comment = c(ORCID = \"0000-0002-3542-2938\")),\\n ",
                        "person(\"Timothy A.\", \"Davis\", role = \"ctb\",\\n ",
                        "comment = c(ORCID = \"0000-0001-7614-6899\",\\n ",
                        "\"SuiteSparse libraries, notably CHOLMOD and AMD\",\\n ",
                        "\"collaborators listed in dir(pattern=\\\"^[A-Z]+[.]txt$\\\", full.names=TRUE, system.file(\\\"doc\\\", \\\"SuiteSparse\\\", package=\\\"Matrix\\\"))\")),\\n ",
                        "person(\"Jens\", \"Oehlschlägel\", role = \"ctb\",\\n ",
                        "comment = \"initial nearPD()\"),\\n ",
                        "person(\"Jason\", \"Riedy\", role = \"ctb\",\\n ",
                        "comment = c(ORCID = \"0000-0002-4345-4200\",\\n ",
                        "\"GNU Octave's condest() and onenormest()\",\\n ",
                        "\"Copyright: Regents of the University of California\")),\\n ",
                        "person(\"R Core Team\", role = \"ctb\",\\n ",
                        "comment = \"base R's matrix implementation\"))"),
                properties.getProperty("Authors@R"));
        assertEquals("R (>= 3.5.0), methods", properties.getProperty("Depends"));
        assertEquals("2024-01-11 17:50:15 UTC", properties.getProperty("Date/Publication"));
        assertEquals(
                "do also bump src/version.h, inst/include/Matrix/version.h", properties.getProperty("VersionNote"));
        assertEquals("2024-01-06", properties.getProperty("Date"));
        assertEquals("recommended", properties.getProperty("Priority"));
        assertEquals("Matrix-authors@R-project.org", properties.getProperty("Contact"));
        assertEquals("SparseM, graph", properties.getProperty("Enhances"));
        assertEquals("no", properties.getProperty("LazyData"));
        assertEquals("not possible, since we use data/*.R and our S4 classes", properties.getProperty("LazyDataNote"));
        assertEquals("no", properties.getProperty("BuildResaveData"));
        assertEquals("UTF-8", properties.getProperty("Encoding"));
    }

    @Test
    public void readPropertiesFromGgallyFile() throws Exception {
        final Properties properties = new PropertiesParser(new File(GGALLY_FILE));
        assertEquals(26, properties.size());
        assertEquals("GGally", properties.getProperty("Package"));
        assertEquals("2.2.1", properties.getProperty("Version"));
        assertEquals("GPL (>= 2.0)", properties.getProperty("License"));
        assertEquals("Extension to 'ggplot2'", properties.getProperty("Title"));
        assertEquals("Package", properties.getProperty("Type"));
        assertEquals("yes", properties.getProperty("LazyLoad"));
        assertEquals("true", properties.getProperty("LazyData"));
        assertEquals("https://ggobi.github.io/ggally/, https://github.com/ggobi/ggally", properties.getProperty("URL"));
        assertEquals("https://github.com/ggobi/ggally/issues", properties.getProperty("BugReports"));
        assertEquals(
                String.join(
                        "",
                        "c(\\n ",
                        "person(\"Barret\", \"Schloerke\", role = c(\"aut\", \"cre\"), email = \"schloerke@gmail.com\"),\\n ",
                        "person(\"Di\", \"Cook\", role = c(\"aut\", \"ths\"), email = \"dicook@monash.edu\"),\\n ",
                        "person(\"Joseph\", \"Larmarange\", role = \"aut\", email = \"joseph@larmarange.net\"),\\n ",
                        "person(\"Francois\", \"Briatte\", role = \"aut\", email = \"f.briatte@gmail.com\"),\\n ",
                        "person(\"Moritz\", \"Marbach\", role = \"aut\", email = \"mmarbach@mail.uni-mannheim.de\"),\\n ",
                        "person(\"Edwin\", \"Thoen\", role = \"aut\", email = \"edwinthoen@gmail.com\"),\\n ",
                        "person(\"Amos\", \"Elberg\", role = \"aut\", email = \"amos.elberg@gmail.com\"),\\n ",
                        "person(\"Ott\", \"Toomet\", role = \"ctb\", email = \"otoomet@gmail.com\"),\\n ",
                        "person(\"Jason\", \"Crowley\", role = \"aut\", email = \"crowley.jason.s@gmail.com\"),\\n ",
                        "person(\"Heike\", \"Hofmann\", role = \"ths\", email = \"hofmann@iastate.edu\"),\\n ",
                        "person(\"Hadley\", \"Wickham\", role = \"ths\", email = \"h.wickham@gmail.com\")\\n ",
                        ")"),
                properties.getProperty("Authors@R"));
        assertEquals(
                String.join(
                        "",
                        "The R package 'ggplot2' is a plotting system based on the grammar of graphics.\\n ",
                        "'GGally' extends 'ggplot2' by adding several functions\\n ",
                        "to reduce the complexity of combining geometric objects with transformed data.\\n ",
                        "Some of these functions include a pairwise plot matrix, a two group pairwise plot\\n ",
                        "matrix, a parallel coordinates plot, a survival plot, and several functions to\\n ",
                        "plot networks."),
                properties.getProperty("Description"));
        assertEquals("R (>= 3.1), ggplot2 (>= 3.4.4)", properties.getProperty("Depends"));
        assertEquals(
                String.join(
                        "",
                        "dplyr (>= 1.0.0), tidyr (>= 1.3.0), grDevices, grid, ggstats,\\n gtable (>= 0.2.0), lifecycle, plyr (>= 1.8.3), progress,\\n ",
                        "RColorBrewer, rlang, scales (>= 1.1.0), utils, magrittr"),
                properties.getProperty("Imports"));
        assertEquals(
                String.join(
                        "",
                        "broom (>= 0.7.0), broom.helpers (>= 1.3.0), chemometrics,\\n ",
                        "geosphere (>= 1.5-1), ggforce, Hmisc, igraph (>= 1.0.1),\\n ",
                        "intergraph (>= 2.0-2), labelled, maps (>= 3.1.0), mapproj,\\n ",
                        "nnet, network (>= 1.17.1), scagnostics, sna (>= 2.3-2),\\n ",
                        "survival, rmarkdown, roxygen2, testthat, crosstalk, knitr,\\n ",
                        "spelling, emmeans, vdiffr"),
                properties.getProperty("Suggests"));
        assertEquals("7.3.1", properties.getProperty("RoxygenNote"));
        assertEquals("openssl", properties.getProperty("SystemRequirements"));
        assertEquals("UTF-8", properties.getProperty("Encoding"));
        assertEquals("en-US", properties.getProperty("Language"));
        assertEquals("lifecycle", properties.getProperty("RdMacros"));
        assertEquals("3", properties.getProperty("Config/testthat/edition"));
        assertEquals("no", properties.getProperty("NeedsCompilation"));
        assertEquals("2024-02-13 19:02:36 UTC; barret", properties.getProperty("Packaged"));
        assertEquals(
                String.join(
                        "",
                        "Barret Schloerke [aut, cre],\\n ",
                        "Di Cook [aut, ths],\\n ",
                        "Joseph Larmarange [aut],\\n ",
                        "Francois Briatte [aut],\\n ",
                        "Moritz Marbach [aut],\\n ",
                        "Edwin Thoen [aut],\\n ",
                        "Amos Elberg [aut],\\n ",
                        "Ott Toomet [ctb],\\n ",
                        "Jason Crowley [aut],\\n ",
                        "Heike Hofmann [ths],\\n ",
                        "Hadley Wickham [ths]"),
                properties.getProperty("Author"));
        assertEquals("Barret Schloerke <schloerke@gmail.com>", properties.getProperty("Maintainer"));
        assertEquals("CRAN", properties.getProperty("Repository"));
        assertEquals("2024-02-14 00:53:32 UTC", properties.getProperty("Date/Publication"));
    }

    @Test
    public void readPropertiesFromActuarFile() throws Exception {
        final Properties properties = new PropertiesParser(new File(ACTUAR_FILE));
        assertEquals(23, properties.size());
        assertEquals("62P05, 91B30, 62G32", properties.getProperty("Classification/MSC-2010"));
        assertEquals(
                String.join(
                        "",
                        "Vincent Goulet [cre, aut],\\n ",
                        "Sébastien Auclair [ctb],\\n ",
                        "Christophe Dutang [aut],\\n ",
                        "Walter Garcia-Fontes [ctb],\\n ",
                        "Nicholas Langevin [ctb],\\n ",
                        "Xavier Milhaud [ctb],\\n ",
                        "Tommy Ouellet [ctb],\\n ",
                        "Alexandre Parent [ctb],\\n ",
                        "Mathieu Pigeon [aut],\\n ",
                        "Louis-Philippe Pouliot [ctb],\\n ",
                        "Jeffrey A. Ryan [aut] (Package API),\\n ",
                        "Robert Gentleman [aut] (Parts of the R to C interface),\\n ",
                        "Ross Ihaka [aut] (Parts of the R to C interface),\\n ",
                        "R Core Team [aut] (Parts of the R to C interface),\\n ",
                        "R Foundation [aut] (Parts of the R to C interface)"),
                properties.getProperty("Author"));
    }
}
