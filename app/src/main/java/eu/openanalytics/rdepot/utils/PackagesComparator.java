package eu.openanalytics.rdepot.utils;

import java.util.Comparator;

import eu.openanalytics.rdepot.model.Package;

public class PackagesComparator implements Comparator<Package> {

	@Override
	public int compare(Package packageA, Package packageB) {
		
		if(packageA.getRepository().getId() != packageB.getRepository().getId())
			return packageA.getRepository().getName().compareTo(packageB.getRepository().getName());
		
		if(!packageA.getName().equals(packageB.getName()))
			return packageA.getName().compareTo(packageB.getName());
		
		return -1 * packageA.compareTo(packageB);		
	}
}
