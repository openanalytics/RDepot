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
package eu.openanalytics.rdepot.base.utils;

public class PackageVersionComparator {

    /**
     * @param a
     * @param b
     * @return >0 if a is newer than b, =0 if a is the same version as b, <0 if a is older than b
     */
    public int compare(String a, String b) {
        String[] theseSplitDots = a.split("[-.]");
        String[] thoseSplitDots = b.split("[-.]");
        int length;
        if (theseSplitDots.length - thoseSplitDots.length > 0) {
            length = thoseSplitDots.length;
        } else {
            length = theseSplitDots.length;
        }

        int thisNumber, thatNumber;
        for (int i = 0; i < length; i++) {
            thisNumber = Integer.parseInt(theseSplitDots[i]);
            thatNumber = Integer.parseInt(thoseSplitDots[i]);
            if (thisNumber > thatNumber) {
                return 1;
            }
            if (thatNumber > thisNumber) {
                return -1;
            }
        }

        if (theseSplitDots.length == thoseSplitDots.length) {
            return 0;
        }

        if (theseSplitDots.length - thoseSplitDots.length > 0) {
            return 1;
        } else {
            return -1;
        }
    }
}
