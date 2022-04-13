/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
package eu.openanalytics.rdepot.test.unit;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import eu.openanalytics.rdepot.model.Package;

public class PackageUnitTest 
{
	@Test
	public void testCompareToDots()
	{
		Package testv1_2_3 = new Package();
		testv1_2_3.setName("test");
		testv1_2_3.setVersion("1.2.3");
		
		Package testv0_2_3 = new Package();
		testv0_2_3.setName("test");
		testv0_2_3.setVersion("0.2.3");
		
		Package testv1_3_3 = new Package();
		testv1_3_3.setName("test");
		testv1_3_3.setVersion("1.3.3");
		
		Package testv1_2_4 = new Package();
		testv1_2_4.setName("test");
		testv1_2_4.setVersion("1.2.4");
		
		assertEquals(1, testv1_2_3.compareTo(testv0_2_3));
		assertEquals(-1, testv0_2_3.compareTo(testv1_2_3));
		
		assertEquals(-1, testv1_2_3.compareTo(testv1_3_3));
		assertEquals(1, testv1_3_3.compareTo(testv1_2_3));
		
		assertEquals(-1, testv1_2_3.compareTo(testv1_2_4));
		assertEquals(1, testv1_2_4.compareTo(testv1_2_3));
		
		assertEquals(0, testv1_2_4.compareTo(testv1_2_4));
	}
	
	@Test
	public void testCompareToDotsAndHyphens() {
		String name = "name";
		
		Package package1 = new Package();
		package1.setName(name);
		package1.setVersion("2.0.5-1.2");
		
		Package package2 = new Package();
		package2.setName(name);
		package2.setVersion("2.0.5-1.1.1");
		
		Package package3 = new Package();
		package3.setName(name);
		package3.setVersion("1.0");
		
		Package package4 = new Package();
		package4.setName(name);
		package4.setVersion("3.1");
		
		Package package5 = new Package();
		package5.setName(name);
		package5.setVersion("3.1.2");
		
		assertEquals(1, package1.compareTo(package2));
		assertEquals(-1, package3.compareTo(package2));
		assertEquals(1, package4.compareTo(package1));
		assertEquals(-1, package4.compareTo(package5));		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCompareToException()
	{
		Package testv1_2_3 = new Package();
		testv1_2_3.setName("test");
		testv1_2_3.setVersion("1.2.3");
		
		Package otherv1_2_3 = new Package();
		otherv1_2_3.setName("other");
		otherv1_2_3.setVersion("1.2.3");
		
		testv1_2_3.compareTo(otherv1_2_3);
	}
	
//	@Test
//	public void testGetVignettes()
//	{
//		Package testPackage = new Package();
//		File packageFile = new File(classLoader.getResource("testPackage.tar.gz").getFile());
//		testPackage.setSource(packageFile.getAbsolutePath());
//		testPackage.setName("testPackage");
//		List<Vignette> vignettes = testPackage.getVignettes();
//		assertNotNull(vignettes);
//		assertEquals(6, vignettes.size());
//		for(Vignette vignette : vignettes)
//		{
//			switch(vignette.getFileName())
//			{
//				case "iHaveNoTitle.html":
//					assertEquals("iHaveNoTitle", vignette.getTitle());
//					break;
//				case "lookAtMyTitle.html":
//					assertEquals("My Title Is Amazing", vignette.getTitle());
//					break;
//				case "myRmdTitleIsEmpty.html":
//					assertEquals("myRmdTitleIsEmpty", vignette.getTitle());
//					break;
//				case "myRmdTitleIsMissing.html":
//					assertEquals("myRmdTitleIsMissing", vignette.getTitle());
//					break;
//				case "myTitleIsEmpty.html":
//					assertEquals("myTitleIsEmpty", vignette.getTitle());
//					break;
//				case "useRmd.html":
//					assertEquals("Some basic info about testPackage", vignette.getTitle());
//					break;
//				default:
//					fail("Unknown vignette found: " + vignette.getFileName());
//			}
//		}
//	}
}
