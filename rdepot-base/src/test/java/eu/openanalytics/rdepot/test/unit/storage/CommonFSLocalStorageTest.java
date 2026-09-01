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
package eu.openanalytics.rdepot.test.unit.storage;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.storage.implementations.CommonFSLocalStorage;
import java.io.File;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommonFSLocalStorageTest {

    final CommonFSLocalStorage<Package> commonLocalStorage = new CommonFSLocalStorage<>() {

        @Override
        public void setCheckSum(Package packageBag, File packageFile) {}
    };

    @Test
    public void removeTrailingContentWithResult() {
        Assertions.assertEquals(
                Optional.of("<tbody><tr>package1</tr>"),
                commonLocalStorage.removeTrailingContent("</tbody>", "<tbody><tr>package1</tr></tbody>"));
    }

    @Test
    public void removeTrailingContentNoResult() {
        Assertions.assertEquals(
                Optional.empty(), commonLocalStorage.removeTrailingContent("</tbody>", "<tbody><tr>package1</tr>"));
    }

    @Test
    public void removeTrailingContentStripWithResult() {
        Assertions.assertEquals(
                Optional.of("<tbody><tr>package1</tr>"),
                commonLocalStorage.removeTrailingContent("\n  </tbody>\t  ", "<tbody><tr>package1</tr></tbody>"));
    }

    @Test
    public void removeTrailingContentStripTrailingWithResult() {
        Assertions.assertEquals(
                Optional.of("<tbody><tr>package1</tr>"),
                commonLocalStorage.removeTrailingContent("</tbody>", "<tbody><tr>package1</tr></tbody>\t  \n"));
    }

    @Test
    public void removeTrailingContentOnlyContentWithResult() {
        Assertions.assertEquals(Optional.of(""), commonLocalStorage.removeTrailingContent("</tbody>", "</tbody>"));
    }

    @Test
    public void removeTrailingContentNotTrailingNoResult() {
        Assertions.assertEquals(
                Optional.empty(),
                commonLocalStorage.removeTrailingContent(
                        "</tbody>", "<table><tbody><tr>package1</tr></tbody></table>"));
    }
}
