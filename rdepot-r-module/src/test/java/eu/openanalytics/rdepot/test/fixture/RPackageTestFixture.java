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
package eu.openanalytics.rdepot.test.fixture;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.time.DateProvider;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class RPackageTestFixture {
    public static final String NAME = "TestPackage";
    public static final String DESCRIPTION = "Simple test package";
    public static final String AUTHOR = "Albert Einstein";
    public static final String LICENSE = "Some license";
    public static final String TARGZ = ".tar.gz";
    public static final String TITLE = "Test Package";
    public static final String MD5SUM = "1234567";
    public static final String VERSION = "4.5.6";
    public static final Boolean ACTIVATED = true;
    public static final Boolean DELETED = false;
    public static final Boolean BINARY = false;
    public static final String MAINTAINER = "Test Maintainer From Description";

    public static List<RPackage> GET_FIXTURE_PACKAGES(
            RRepository repository, User user, int packageCount, int idShift) {
        List<RPackage> packages = new ArrayList<>();

        for (int i = idShift; i < packageCount + idShift; i++) {
            RPackage packageBag = new RPackage(
                    i,
                    repository,
                    user,
                    NAME + i,
                    DESCRIPTION + i,
                    AUTHOR + i,
                    LICENSE + i,
                    NAME + i + "_" + VERSION + TARGZ,
                    TITLE + i,
                    MD5SUM + i,
                    ACTIVATED,
                    DELETED,
                    BINARY);

            packageBag.setVersion(VERSION);
            packageBag.setGenerateManuals(false);
            Submission submission = RSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
            submission.setId(i);
            submission.setCreatedDate(DateProvider.now());
            packageBag.setSubmission(submission);
            packageBag.setMaintainer(MAINTAINER);
            packages.add(packageBag);
        }

        return packages;
    }

    public static RPackage GET_FIXTURE_PACKAGE(RRepository repository, User user, Integer id) {
        return GET_FIXTURE_PACKAGES(repository, user, 1, id).get(0);
    }

    public static RPackage GET_FIXTURE_PACKAGE(RRepository repository, User user) {
        return GET_FIXTURE_PACKAGE(repository, user, 1);
    }

    public static RPackage GET_FIXTURE_BINARY_PACKAGE(RRepository repository, User user) {
        RPackage binaryPackage = GET_FIXTURE_PACKAGE(repository, user);
        binaryPackage.setBinary(true);
        binaryPackage.setBuilt("R 4.2; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix");
        binaryPackage.setRVersion("4.2");
        binaryPackage.setArchitecture("x86_64");
        binaryPackage.setDistribution("centos7");
        binaryPackage.setMaintainer(MAINTAINER);

        Submission submission = RSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, binaryPackage);
        submission.setId(binaryPackage.getSubmission().getId());
        submission.setCreatedDate(DateProvider.now());
        binaryPackage.setSubmission(submission);
        return binaryPackage;
    }

    public static Page<RPackage> GET_EXAMPLE_PACKAGES_PAGED(RRepository repository, User user) {
        return new PageImpl<>(GET_FIXTURE_PACKAGES(repository, user, 3, 100));
    }

    public static Page<RPackage> GET_EXAMPLE_PACKAGES_PAGED_DELETED() {
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        List<RPackage> packages = GET_FIXTURE_PACKAGES(repository, user, 3, 100);
        packages.forEach(p -> p.setDeleted(true));

        return new PageImpl<>(packages);
    }

    public static RPackage GET_EXAMPLE_PACKAGE() {
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        return GET_FIXTURE_PACKAGES(repository, user, 3, 100).get(0);
    }

    public static PackageDto GET_EXAMPLE_PACKAGE_DTO(Package packageBag) {
        return new PackageDto(packageBag);
    }

    public static List<PackageDto> GET_EXAMPLE_PACKAGE_DTOS(List<Submission> submissions) {
        List<PackageDto> packageDtos = new ArrayList<>();
        submissions.forEach(submission -> packageDtos.add(new PackageDto(submission.getPackageBag())));
        return packageDtos;
    }

    public static class RPackagePopulationFixture {

        public static RRepository GET_EXAMPLE_R_REPOSITORY() {
            final RRepository arr = new RRepository();
            arr.setName("arr");
            arr.setPublicationUri("http://localhost/repo/arr");
            arr.setServerAddress("http://oa-rdepot-repo:8080/arr");
            arr.setPublished(true);
            arr.setDeleted(false);
            arr.setRedirectToSource(false);
            return arr;
        }

        public static User GET_EXAMPLE_USER() {
            final User usr = new User();
            usr.setId(1);
            usr.setName("Albert Einstein");
            usr.setLogin("einstein");
            usr.setEmail("einstein@ldap.forumsys.com");
            return usr;
        }

        public static List<RPackage> GET_LATEST_SOURCE_PACKAGES_REDIRECT_TO_SOURCE() {
            final List<RPackage> packages = new ArrayList<>();
            final List<RPackage> sourcePackages = GET_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
            packages.add(sourcePackages.get(5));
            packages.add(sourcePackages.get(4));
            return packages;
        }

        public static List<RPackage> GET_LATEST_SOURCE_PACKAGES() {
            final List<RPackage> packages = new ArrayList<>();
            final List<RPackage> sourcePackages = GET_SOURCE_PACKAGES();
            packages.add(sourcePackages.get(0));
            packages.add(sourcePackages.get(4));
            return packages;
        }

        public static List<RPackage> GET_ARCHIVE_SOURCE_PACKAGES() {
            final List<RPackage> packages = new ArrayList<>();
            final List<RPackage> sourcePackages = GET_SOURCE_PACKAGES();
            packages.add(sourcePackages.get(1));
            packages.add(sourcePackages.get(2));
            packages.add(sourcePackages.get(3));
            return packages;
        }

        public static List<RPackage> GET_ARCHIVE_SOURCE_PACKAGES_REDIRECT_TO_SOURCE() {
            final List<RPackage> packages = new ArrayList<>();
            final List<RPackage> sourcePackages = GET_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
            packages.add(sourcePackages.get(0));
            packages.add(sourcePackages.get(1));
            packages.add(sourcePackages.get(2));
            packages.add(sourcePackages.get(3));
            return packages;
        }

        public static List<RPackage> GET_LATEST_BINARY_PACKAGES() {
            final List<RPackage> packages = new ArrayList<>();
            final List<RPackage> binaryPackages = GET_BINARY_PACKAGES();
            packages.add(binaryPackages.get(2));
            return packages;
        }

        public static List<RPackage> GET_ARCHIVE_BINARY_PACKAGES() {
            final List<RPackage> packages = new ArrayList<>();
            final List<RPackage> binaryPackages = GET_BINARY_PACKAGES();
            packages.add(binaryPackages.get(0));
            packages.add(binaryPackages.get(1));
            return packages;
        }

        public static List<RPackage> GET_SOURCE_PACKAGES_REDIRECT_TO_SOURCE() {
            final List<RPackage> packages = GET_SOURCE_PACKAGES();
            final RPackage firstPkg = packages.get(0);
            final User user = firstPkg.getUser();
            final RRepository repository = firstPkg.getRepository();
            repository.setRedirectToSource(true);

            final RPackage plyr189 = new RPackage();
            plyr189.setRepository(repository);
            plyr189.setId(41);
            plyr189.setName("plyr");
            plyr189.setUser(user);
            plyr189.setSource("/tmp/rdepot-unit-tests/example-repository/plyr_1.8.9.tar.gz");
            plyr189.setAuthor("Hadley Wickham [aut, cre]");
            plyr189.setDeleted(false);
            plyr189.setVersion("1.8.9");
            plyr189.setDescription("A set of tools that solves a common set of problems: "
                    + "you need\\n to break a big problem down into manageable pieces, "
                    + "operate on each\\n piece and then put all the pieces back together. "
                    + "For example, you\\n might want to fit a model to each spatial "
                    + "location or time point in\\n your study, summarise data by "
                    + "panels or collapse high-dimensional\\n arrays to simpler "
                    + "summary statistics. The development of 'plyr' has\\n "
                    + "been generously supported by 'Becton Dickinson'.");
            plyr189.setTitle("Tools for Splitting, Applying and Combining Data");
            plyr189.setUrl("http://had.co.nz/plyr, https://github.com/hadley/plyr");
            plyr189.setDescriptionContentType("txt");
            plyr189.setBinary(false);
            plyr189.setActive(true);
            plyr189.setLicense("MIT + file LICENSE");
            plyr189.setMd5sum("5a8b129534abace172059ecc5c0b5072");
            packages.add(plyr189);
            return packages;
        }

        public static List<RPackage> GET_SOURCE_PACKAGES() {
            final List<RPackage> packages = new ArrayList<>();
            final RRepository repository = GET_EXAMPLE_R_REPOSITORY();
            final User user = GET_EXAMPLE_USER();

            final RPackage plyr188 = new RPackage();
            plyr188.setRepository(repository);
            plyr188.setId(36);
            plyr188.setName("plyr");
            plyr188.setUser(user);
            plyr188.setSource("/tmp/rdepot-unit-tests/example-repository/plyr_1.8.8.tar.gz");
            plyr188.setAuthor("Hadley Wickham [aut, cre]");
            plyr188.setDeleted(false);
            plyr188.setVersion("1.8.8");
            plyr188.setDescription("A set of tools that solves a common set of problems: "
                    + "you need\\n to break a big problem down into manageable pieces, "
                    + "operate on each\\n piece and then put all the pieces back together. "
                    + "For example, you\\n might want to fit a model to each spatial "
                    + "location or time point in\\n your study, summarise data by "
                    + "panels or collapse high-dimensional\\n arrays to simpler "
                    + "summary statistics. The development of 'plyr' has\\n "
                    + "been generously supported by 'Becton Dickinson'.");
            plyr188.setTitle("Tools for Splitting, Applying and Combining Data");
            plyr188.setUrl("http://had.co.nz/plyr, https://github.com/hadley/plyr");
            plyr188.setDescriptionContentType("txt");
            plyr188.setBinary(false);
            plyr188.setActive(true);
            plyr188.setLicense("MIT + file LICENSE");
            plyr188.setMd5sum("0a22da16605ee765e7d4f1efc9f7a61f");
            packages.add(plyr188);

            final RPackage plyr18 = new RPackage();
            plyr18.setRepository(repository);
            plyr18.setId(37);
            plyr18.setName("plyr");
            plyr18.setSource("/tmp/rdepot-unit-tests/example-repository/plyr_1.8.tar.gz");
            plyr18.setAuthor("Hadley Wickham <h.wickham@gmail.com>");
            plyr18.setDeleted(false);
            plyr18.setVersion("1.8");
            plyr18.setUser(user);
            plyr18.setDescription("plyr is a set of tools that solves a common set of\\n "
                    + "problems: you need to break a big problem down into manageable\\n "
                    + "pieces, operate on each pieces and then put all the pieces back\\n "
                    + "together. For example, you might want to fit a model to each\\n "
                    + "spatial location or time point in your study, summarise data by\\n "
                    + "panels or collapse high-dimensional arrays to simpler summary\\n "
                    + "statistics. The development of plyr has been generously\\n supported "
                    + "by BD (Becton Dickinson).");
            plyr18.setTitle("Tools for splitting, applying and combining data");
            plyr18.setUrl("http://had.co.nz/plyr");
            plyr18.setDescriptionContentType("txt");
            plyr18.setBinary(false);
            plyr18.setActive(true);
            plyr18.setMd5sum("e1c1d2f0c47fd16b2cef6ec9c2e5883c");
            plyr18.setLicense("MIT");
            packages.add(plyr18);

            final RPackage qsort021 = new RPackage();
            qsort021.setRepository(repository);
            qsort021.setId(35);
            qsort021.setName("qsort");
            qsort021.setVersion("0.2.1");
            qsort021.setDescriptionContentType("txt");
            qsort021.setBinary(false);
            qsort021.setActive(true);
            qsort021.setSource("/tmp/rdepot-unit-tests/example-repository/qsort_0.2.1.tar.gz");
            qsort021.setDescription("Computes scores from Q-sort data, using criteria "
                    + "sorts and\\n derived scales from subsets of items.\\n "
                    + "The 'qsort' package includes descriptions and scoring procedures\\n "
                    + "for four different Q-sets:\\n "
                    + "Attachment Q-set (version 3.0) (Waters, 1995, "
                    + "<doi:10.1111/j.1540-5834.1995.tb00214.x>);\\n California "
                    + "Child Q-set (Block and Block, 1969, "
                    + "<doi:10.1037/0012-1649.21.3.508>);\\n Maternal Behaviour "
                    + "Q-set (version 3.1)\\n (Pederson et al., 1999, "
                    + "<https://ir.lib.uwo.ca/cgi/viewcontent.cgi"
                    + "?article=1000&context=psychologypub>);\\n Preschool "
                    + "Q-set (Baumrind, 1968 revised by Wanda Bronson, "
                    + "<doi:10.1111/j.1540-5834.1995.tb00214.x>).");
            qsort021.setAuthor(
                    "João R Daniel [aut, cre] (<https://orcid.org/0000-0001-6609-2014>),\\n David N Sousa [aut]");
            qsort021.setTitle("Scoring Q-Sort Data");
            qsort021.setMd5sum("5dd316a3591a86ff3d6cda0526c67ba5");
            qsort021.setLicense("GPL-3");
            packages.add(qsort021);

            final RPackage qsort022 = new RPackage();
            qsort022.setRepository(repository);
            qsort022.setId(39);
            qsort022.setName("qsort");
            qsort022.setVersion("0.2.2");
            qsort022.setDescriptionContentType("txt");
            qsort022.setBinary(false);
            qsort022.setActive(true);
            qsort022.setTitle("Scoring Q-Sort Data");
            qsort022.setSource("/tmp/rdepot-unit-tests/example-repository/qsort_0.2.2.tar.gz");
            qsort022.setAuthor("João R Daniel [aut, cre] " + "(<https://orcid.org/0000-0001-6609-2014>),\\n "
                    + "David N Sousa [aut] (<https://orcid.org/0000-0001-7277-6447>)");
            qsort022.setDescription("Computes scores from Q-sort data, using criteria "
                    + "sorts and\\n derived scales from subsets of items.\\n "
                    + "The 'qsort' package includes descriptions and scoring "
                    + "procedures\\n for four different Q-sets:\\n Attachment "
                    + "Q-set (version 3.0) (Waters, 1995, <doi:10.1111/"
                    + "j.1540-5834.1995.tb00214.x>);\\n California "
                    + "Child Q-set (Block and Block, 1969, <doi:10.1037"
                    + "/0012-1649.21.3.508>);\\n Maternal Behaviour Q-set "
                    + "(version 3.1)\\n (Pederson et al., 1999, "
                    + "<https://ir.lib.uwo.ca/cgi/viewcontent.cgi"
                    + "?article=1000&context=psychologypub>);\\n "
                    + "Preschool Q-set (Baumrind, 1968 revised "
                    + "by Wanda Bronson, <doi:10.1111/j.1540-5834.1995.tb00214.x>).");
            qsort022.setMd5sum("76346f1a4ef62977b0acf794c6bb0aef");
            qsort022.setLicense("GPL-3");
            packages.add(qsort022);

            final RPackage qsort023 = new RPackage();
            qsort023.setRepository(repository);
            qsort023.setId(40);
            qsort023.setName("qsort");
            qsort023.setVersion("0.2.3");
            qsort023.setDescriptionContentType("txt");
            qsort023.setBinary(false);
            qsort023.setActive(true);
            qsort023.setTitle("Scoring Q-Sort Data");
            qsort023.setAuthor("David N Sousa [aut, cre] " + "(<https://orcid.org/0000-0001-7277-6447>),\\n "
                    + "João R Daniel [aut] (<https://orcid.org/0000-0001-6609-2014>)");
            qsort023.setDescription(
                    "Computes scores from Q-sort data, " + "using criteria sorts and\\n derived scales from "
                            + "subsets of items.\\n The 'qsort' package "
                            + "includes descriptions and scoring procedures\\n for "
                            + "four different Q-sets commonly used in "
                            + "developmental psychology research:\\n Attachment "
                            + "Q-set (version 3.0) (Waters, 1995, <doi:10.1111"
                            + "/j.1540-5834.1995.tb00214.x>);\\n California "
                            + "Child Q-set (Block and Block, 1969, "
                            + "<doi:10.1037/0012-1649.21.3.508>);\\n Maternal "
                            + "Behaviour Q-set (version 3.1)\\n (Pederson et al., 1999, "
                            + "<https://ir.lib.uwo.ca/cgi/viewcontent.cgi"
                            + "?article=1000&context=psychologypub>);\\n "
                            + "Preschool Q-set (Baumrind, 1968 revised by "
                            + "Wanda Bronson, <doi:10.1111/j.1540-5834.1995.tb00214.x>).");
            qsort023.setSource("/tmp/rdepot-unit-tests/example-repository/qsort_0.2.3.tar.gz");
            qsort023.setMd5sum("3204109d62ec7ff8e44bd15a989fc8b1");
            qsort023.setLicense("GPL-3");
            packages.add(qsort023);

            return packages;
        }

        public static List<RPackage> GET_BINARY_PACKAGES() {
            final List<RPackage> packages = new ArrayList<>();
            final RRepository repository = GET_EXAMPLE_R_REPOSITORY();

            final RPackage plyr186 = new RPackage();
            plyr186.setRepository(repository);
            plyr186.setId(31);
            plyr186.setName("plyr");
            plyr186.setVersion("1.8.6");
            plyr186.setDescriptionContentType("txt");
            plyr186.setBinary(true);
            plyr186.setActive(true);
            plyr186.setTitle("Tools for Splitting, Applying and Combining Data");
            plyr186.setDescription(
                    "A set of tools that solves a common set of\\n " + "problems: you need to break a big problem down "
                            + "into manageable pieces,\\n operate on each piece "
                            + "and then put all the pieces back together. For\\n example, "
                            + "you might want to fit a model to each spatial location or\\n "
                            + "time point in your study, summarise data by panels or collapse\\n "
                            + "high-dimensional arrays to simpler summary statistics. "
                            + "The development\\n of 'plyr' has been generously "
                            + "supported by 'Becton Dickinson'.");
            plyr186.setAuthor("Hadley Wickham [aut, cre]");
            plyr186.setUrl("http://had.co.nz/plyr, https://github.com/hadley/plyr");
            plyr186.setDistribution("centos8");
            plyr186.setArchitecture("x86_64");
            plyr186.setRVersion("4.5");
            plyr186.setSource("/tmp/rdepot-unit-tests/example-repository/plyr_1.8.6_R_x86_64-pc-linux-gnu.tar.gz");
            plyr186.setMd5sum("6a9c2acfd924f2fb626d54168120fa08");
            plyr186.setLicense("MIT + file LICENSE");
            plyr186.setBuilt("R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix");
            packages.add(plyr186);

            final RPackage plyr181 = new RPackage();
            plyr181.setRepository(repository);
            plyr181.setId(33);
            plyr181.setName("plyr");
            plyr181.setVersion("1.8.1");
            plyr181.setDescriptionContentType("txt");
            plyr181.setBinary(true);
            plyr181.setActive(true);
            plyr181.setTitle("Tools for splitting, applying and combining data");
            plyr181.setDescription("plyr is a set of tools that solves a common\\n "
                    + "set of problems: you need to break a big problem down\\n "
                    + "into manageable pieces, operate on each pieces and then\\n "
                    + "put all the pieces back together. For example, you\\n "
                    + "might want to fit a model to each spatial location or\\n "
                    + "time point in your study, summarise data by panels or\\n "
                    + "collapse high-dimensional arrays to simpler summary\\n "
                    + "statistics. The development of plyr has been generously\\n "
                    + "supported by BD (Becton Dickinson).");
            plyr181.setAuthor("Hadley Wickham <h.wickham@gmail.com>");
            plyr181.setUrl("http://had.co.nz/plyr");
            plyr181.setDistribution("centos8");
            plyr181.setArchitecture("x86_64");
            plyr181.setRVersion("4.5");
            plyr181.setSource("/tmp/rdepot-unit-tests/example-repository/plyr_1.8.1_R_x86_64-pc-linux-gnu.tar.gz");
            plyr181.setMd5sum("a8b2d2284d56ab1839728040d463a360");
            plyr181.setLicense("MIT + file LICENSE");
            plyr181.setBuilt("R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix");
            packages.add(plyr181);

            final RPackage plyr188 = new RPackage();
            plyr188.setRepository(repository);
            plyr188.setId(36);
            plyr188.setName("plyr");
            plyr188.setVersion("1.8.8");
            plyr188.setDescriptionContentType("txt");
            plyr188.setBinary(true);
            plyr188.setActive(true);
            plyr188.setTitle("Tools for Splitting, Applying and Combining Data");
            plyr188.setDescription(
                    "A set of tools that solves a common set of " + "problems: you need\\n to break a big problem down "
                            + "into manageable pieces, operate on each\\n piece and "
                            + "then put all the pieces back together. For example, "
                            + "you\\n might want to fit a model to each spatial "
                            + "location or time point in\\n your study, summarise "
                            + "data by panels or collapse high-dimensional\\n arrays "
                            + "to simpler summary statistics. The development "
                            + "of 'plyr' has\\n been generously "
                            + "supported by 'Becton Dickinson'.");
            plyr188.setAuthor("Hadley Wickham [aut, cre]");
            plyr188.setUrl("http://had.co.nz/plyr, https://github.com/hadley/plyr");
            plyr188.setDistribution("centos8");
            plyr188.setArchitecture("x86_64");
            plyr188.setRVersion("4.5");
            plyr188.setSource("/tmp/rdepot-unit-tests/example-repository/plyr_1.8.8_R_x86_64-pc-linux-gnu.tar.gz");
            plyr188.setMd5sum("08841cfd5edbd118a512198217cf5f2e");
            plyr188.setLicense("MIT + file LICENSE");
            plyr188.setBuilt("R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix");
            packages.add(plyr188);
            return packages;
        }
    }
}
