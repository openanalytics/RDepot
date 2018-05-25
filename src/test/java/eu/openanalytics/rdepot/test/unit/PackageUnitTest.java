/**
 * R Depot
 *
 * Copyright (C) 2012-2018 Open Analytics NV
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
	public void testCompareTo()
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
		
		assertEquals(100, testv1_2_3.compareTo(testv0_2_3));
		assertEquals(-100, testv0_2_3.compareTo(testv1_2_3));
		
		assertEquals(-10, testv1_2_3.compareTo(testv1_3_3));
		assertEquals(10, testv1_3_3.compareTo(testv1_2_3));
		
		assertEquals(-1, testv1_2_3.compareTo(testv1_2_4));
		assertEquals(1, testv1_2_4.compareTo(testv1_2_3));
		
		assertEquals(0, testv1_2_4.compareTo(testv1_2_4));
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
}
