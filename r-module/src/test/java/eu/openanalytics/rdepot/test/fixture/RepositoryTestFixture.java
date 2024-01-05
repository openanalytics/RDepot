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
package eu.openanalytics.rdepot.test.fixture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.r.entities.RPackage;

public class RepositoryTestFixture {
//	public static final String PUBLICATION_URL = "http://localhost/test_repo";
//	public static final String NAME = "TestRepo";
//	public static final String SERVER_ADDRESS = "http://oa-rdepot-repo:8080/Repo";
//	public static final Boolean PUBLISHED = false;
//	public static final Boolean DELETED = false;
//	
//	public static List<Repository> GET_FIXTURE_REPOSITORIES(int repositoryCount, int idShift) {
//		List<Repository> repositories = new ArrayList<>();
//		
//		for(int i = idShift; i < repositoryCount + idShift; i++) {
//			Repository repository = new Repository(
//					i,
//					PUBLICATION_URL + Integer.toString(i),
//					NAME + Integer.toString(i),
//					SERVER_ADDRESS + Integer.toString(i),
//					PUBLISHED,
//					DELETED
//					);
//			repository.setVersion(1);
//			repositories.add(repository);
//		}
//		
//		return repositories;
//	}
//	
//	public static List<Repository> GET_FIXTURE_REPOSITORIES(int repositoryCount) {
//		return GET_FIXTURE_REPOSITORIES(repositoryCount, 0);
//	}
//	
//	public static Repository GET_FIXTURE_REPOSITORY() {
//		return GET_FIXTURE_REPOSITORIES(1).get(0);
//	}
//	
//	/**
//	 * Creates example Repository with package maintainers and repository maintainers.
//	 * 
//	 * @param packageMaintainerCount
//	 * @param repositoryMaintainerCount
//	 * @return 
//	 */
//	public static Repository GET_FIXTURE_REPOSITORY_WITH_PACKAGES_AND_REPOSITORY_AND_PACKAGE_MAINTAINERS(int packageMaintainerCount, int repositoryMaintainerCount) {
//		Repository repository = GET_FIXTURE_REPOSITORY();
//		List<User> repositoryMaintainerUsers = UserTestFixture.GET_FIXTURE_USERS(repositoryMaintainerCount, 0, 0);
//		User packageMaintainerUser = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(repositoryMaintainerCount);
//		List<RepositoryMaintainer> repositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(repositoryMaintainerUsers, repository);
//		repository.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(repositoryMaintainers));
//		List<PackageMaintainer> packageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(packageMaintainerUser, repository, packageMaintainerCount);
//		
//		Set<RPackage> packages = new HashSet<>();
//		for(PackageMaintainer packageMaintainer : packageMaintainers) {
//			RPackage packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, packageMaintainer.getUser());
//			packageBag.setName(packageMaintainer.getPackage());
//			packages.add(packageBag);
//		}
//		repository.setPackages(packages);
//		repository.setPackageMaintainers(new HashSet<PackageMaintainer>(packageMaintainers));
//		return repository;
//	}
//	
//	public static List<Repository> GET_DECLARED_REPOSITORIES_WITH_MIRRORS() {
//		List<Repository> repositories = GET_FIXTURE_REPOSITORIES(3);
//		
//		Mirror mirrorA = new Mirror();
//		mirrorA.setName("CRAN");
//		mirrorA.setUri("https://cran.r-project.org");
//		mirrorA.setSyncInterval("* */60 * * * *");
//		mirrorA.setType("tarball");
//		
//		RPackage ggplot2 = new RPackage();
//		ggplot2.setName("ggplot2");
//		ggplot2.setVersion("3.3.2");
//		
//		RPackage plotly = new RPackage();
//		plotly.setName("plotly");
//		
//		List<RPackage> packagesA = new ArrayList<>();
//		packagesA.add(ggplot2);
//		packagesA.add(plotly);
//		
//		mirrorA.setPackages(packagesA);
//		
//		Mirror mirrorB = new Mirror();
//		mirrorB.setName("Bioconductor-3.12");
//		mirrorB.setUri("https://bioconductor.org/packages/3.12/bioc");
//		mirrorB.setSyncInterval("* */60 * * * *");
//		mirrorB.setType("tarball");
//		
//		RPackage annotate = new RPackage();
//		annotate.setName("annotate");
//		annotate.setVersion("1.68.0");
//		
//		List<RPackage> packagesB = new ArrayList<>();
//		packagesB.add(annotate);
//		
//		mirrorB.setPackages(packagesB);
//		
//		Mirror mirrorC = new Mirror();
//		mirrorC.setName("Bioconductor-release");
//		mirrorC.setUri("https://bioconductor.org/packages/release/bioc");
//		mirrorC.setSyncInterval("* */60 * * * *");
//		mirrorC.setType("tarball");
//		
//		RPackage genefilter = new RPackage();
//		genefilter.setName("genefilter");
//		
//		List<RPackage> packagesC = new ArrayList<>();
//		packagesC.add(genefilter);
//		
//		mirrorC.setPackages(packagesC);
//		Set<Mirror> mirrors1 = new HashSet<Mirror>();
//		mirrors1.add(mirrorA);
//		
//		repositories.get(0).setMirrors(mirrors1);
//		
//		Set<Mirror> mirrors2 = new HashSet<Mirror>();
//		mirrors2.add(mirrorB);
//		mirrors2.add(mirrorC);
//		
//		repositories.get(1).setMirrors(mirrors2);
//		
//		return repositories;
//	}
//	
//	public static Map<String, Map<String, RPackage>> GET_REPOSITORIES_WITH_THE_LATEST_PACKAGES() {
//		Map<String, Map<String, RPackage>> repositories = new HashMap<>();
//		
//		//TestRepo0
//		RPackage ggplot2 = new RPackage();
//		ggplot2.setName("ggplot2");
//		ggplot2.setVersion("3.3.2");
//		ggplot2.setMd5sum("123412341323241");
//		
//		RPackage plotly = new RPackage();
//		plotly.setName("plotly");
//		plotly.setVersion("4.9.3");
//		plotly.setMd5sum("5b1cae380156c2d5c9052111e0431f85");
//		
//		Map<String, RPackage> testRepo0 = new HashMap<>();
//		testRepo0.put("ggplot2_3.3.2", ggplot2);
//		testRepo0.put("plotly_4.9.3", plotly);
//		
//		repositories.put("TestRepo0", testRepo0);
//		
//		//TestRepo1
//		RPackage annotate = new RPackage();
//		annotate.setName("annotate");
//		annotate.setVersion("1.68.0");
//		annotate.setMd5sum("c06830f9bcfc0ae87023bfe03acb319e");
//		
//		RPackage genefilter = new RPackage();
//		genefilter.setName("genefilter");
//		genefilter.setVersion("1.72.1");
//		genefilter.setMd5sum("ebf02b933c3f4e09ed52cdf46f65cb1e");
//		
//		Map<String, RPackage> testRepo1 = new HashMap<>();
//		testRepo1.put("annotate_1.68.0", annotate);
//		testRepo1.put("genefilter_1.72.1", genefilter);
//		
//		repositories.put("TestRepo1", testRepo1);
//		
//		//TestRepo2
//		repositories.put("TestRepo2", new HashMap<>());
//		
//		return repositories;
//	}
//	
//	public static Map<String, Map<String, RPackage>> GET_REPOSITORIES_WITH_NO_PACKAGES() {
//		Map<String, Map<String, RPackage>> repositories = new HashMap<>();
//		
//		repositories.put("TestRepo0", new HashMap<>());
//		repositories.put("TestRepo1", new HashMap<>());
//		repositories.put("TestRepo2", new HashMap<>());
//		
//		return repositories;
//	}
//	
//	public static Map<String, Map<String, RPackage>> GET_REPOSITORIES_WITH_ONE_PACKAGE_OUT_OF_DATE() {
//		Map<String, Map<String, RPackage>> repositories = new HashMap<>();
//		
//		//TestRepo0
//		RPackage ggplot2 = new RPackage();
//		ggplot2.setName("ggplot2");
//		ggplot2.setVersion("3.3.2");
//		ggplot2.setMd5sum("123412341323241");
//		
//		RPackage plotly = new RPackage();
//		plotly.setName("plotly");
//		plotly.setVersion("4.9.1"); //out of date
//		plotly.setMd5sum("alteredmd5sum9430493");
//		
//		Map<String, RPackage> testRepo0 = new HashMap<>();
//		testRepo0.put("ggplot2_3.3.2", ggplot2);
//		testRepo0.put("plotly_4.9.3", plotly);
//		
//		repositories.put("TestRepo0", testRepo0);
//		
//		//TestRepo1
//		RPackage annotate = new RPackage();
//		annotate.setName("annotate");
//		annotate.setVersion("1.68.0");
//		annotate.setMd5sum("c06830f9bcfc0ae87023bfe03acb319e");
//		
//		RPackage genefilter = new RPackage();
//		genefilter.setName("genefilter");
//		genefilter.setVersion("1.72.1");
//		genefilter.setMd5sum("ebf02b933c3f4e09ed52cdf46f65cb1e");
//		
//		Map<String, RPackage> testRepo1 = new HashMap<>();
//		testRepo1.put("annotate_1.68.0", annotate);
//		testRepo1.put("genefilter_1.72.1", genefilter);
//		
//		repositories.put("TestRepo1", testRepo1);
//		
//		//TestRepo2
//		repositories.put("TestRepo2", new HashMap<>());
//		
//		return repositories;		
//	}
//	
//	public static List<Repository> GET_DECLARED_REPOSITORIES_WITH_MIRRORS_ONE_PACKAGE_OUT_OF_DATE() {
//		List<Repository> repositories = GET_FIXTURE_REPOSITORIES(3);
//		
//		Mirror mirrorA = new Mirror();
//		mirrorA.setName("CRAN");
//		mirrorA.setUri("https://cran.r-project.org");
//		mirrorA.setSyncInterval("* */60 * * * *");
//		mirrorA.setType("tarball");
//		
//		RPackage ggplot2 = new RPackage();
//		ggplot2.setName("ggplot2");
//		ggplot2.setVersion("3.3.1");
//		
//		RPackage plotly = new RPackage();
//		plotly.setName("plotly");
//		
//		List<RPackage> packagesA = new ArrayList<>();
//		packagesA.add(ggplot2);
//		packagesA.add(plotly);
//		
//		mirrorA.setPackages(packagesA);
//		
//		Mirror mirrorB = new Mirror();
//		mirrorB.setName("Bioconductor-3.12");
//		mirrorB.setUri("https://bioconductor.org/packages/3.12/bioc");
//		mirrorB.setSyncInterval("* */60 * * * *");
//		mirrorB.setType("tarball");
//		
//		RPackage annotate = new RPackage();
//		annotate.setName("annotate");
//		annotate.setVersion("1.68.0");
//		
//		List<RPackage> packagesB = new ArrayList<>();
//		packagesB.add(annotate);
//		
//		mirrorB.setPackages(packagesB);
//		
//		Mirror mirrorC = new Mirror();
//		mirrorC.setName("Bioconductor-release");
//		mirrorC.setUri("https://bioconductor.org/packages/release/bioc");
//		mirrorC.setSyncInterval("* */60 * * * *");
//		mirrorC.setType("tarball");
//		
//		RPackage genefilter = new RPackage();
//		genefilter.setName("genefilter");
//		
//		List<RPackage> packagesC = new ArrayList<>();
//		packagesC.add(genefilter);
//		
//		mirrorC.setPackages(packagesC);
//		Set<Mirror> mirrors1 = new HashSet<Mirror>();
//		mirrors1.add(mirrorA);
//		
//		repositories.get(0).setMirrors(mirrors1);
//		
//		Set<Mirror> mirrors2 = new HashSet<Mirror>();
//		mirrors2.add(mirrorB);
//		mirrors2.add(mirrorC);
//		
//		repositories.get(1).setMirrors(mirrors2);
//		
//		return repositories;
//	}
}
