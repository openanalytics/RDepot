/*
 * RDepot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.test.unit;

import eu.openanalytics.rdepot.python.utils.PublicationURIUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PublicationUriUtilsTest {
    @Test
    public void shouldReturnSingleSlash() throws Exception {
        Assertions.assertEquals("/", PublicationURIUtils.resolveToRelativeURL("https://localhost:8080/"));
    }

    @Test
    public void shouldReturnSingleSlash_whenAuthorityDoesNotEndWithSlash() throws Exception {
        Assertions.assertEquals("/", PublicationURIUtils.resolveToRelativeURL("https://localhost:8080"));
    }

    @Test
    public void shouldReturnSingleSlash_whenAuthorityIsAFullDomain() throws Exception {
        Assertions.assertEquals("/", PublicationURIUtils.resolveToRelativeURL("https://www.example.org:8080/"));
    }

    @Test
    public void shouldReturnSingleSlash_whenAuthorityIsAFullDomainWithoutPort() throws Exception {
        Assertions.assertEquals("/", PublicationURIUtils.resolveToRelativeURL("https://www.example.org/"));
    }

    @Test
    public void shouldReturnRelativeUrl_whenAuthorityIsAFullDomainWithoutPort() throws Exception {
        Assertions.assertEquals(
                "/dsadsadsa", PublicationURIUtils.resolveToRelativeURL("https://www.example.org/dsadsadsa"));
        Assertions.assertEquals(
                "/dsads/23/adsa", PublicationURIUtils.resolveToRelativeURL("https://www.example.org/dsads/23/adsa"));
        Assertions.assertEquals(
                "/dsadsadsa/", PublicationURIUtils.resolveToRelativeURL("https://www.example.org/dsadsadsa/"));
        Assertions.assertEquals(
                "/dsads/23/adsa/", PublicationURIUtils.resolveToRelativeURL("https://www.example.org/dsads/23/adsa/"));
    }

    @Test
    public void shouldReturnRelativeUrl_whenAuthorityIsAFullDomain() throws Exception {
        Assertions.assertEquals(
                "/dsadsadsa", PublicationURIUtils.resolveToRelativeURL("https://www.example.org:8080/dsadsadsa"));
        Assertions.assertEquals(
                "/dsads/23/adsa",
                PublicationURIUtils.resolveToRelativeURL("https://www.example.org:8080/dsads/23/adsa"));
        Assertions.assertEquals(
                "/dsadsadsa/", PublicationURIUtils.resolveToRelativeURL("https://www.example.org:8080/dsadsadsa/"));
        Assertions.assertEquals(
                "/dsads/23/adsa/",
                PublicationURIUtils.resolveToRelativeURL("https://www.example.org:8080/dsads/23/adsa/"));
    }

    @Test
    public void shouldReturnRelativeUrl() throws Exception {
        Assertions.assertEquals(
                "/dsadsadsa", PublicationURIUtils.resolveToRelativeURL("https://localhost:8080/dsadsadsa"));
        Assertions.assertEquals(
                "/dsads/23/adsa", PublicationURIUtils.resolveToRelativeURL("https://localhost:8080/dsads/23/adsa"));
        Assertions.assertEquals(
                "/dsadsadsa/", PublicationURIUtils.resolveToRelativeURL("https://localhost:8080/dsadsadsa/"));
        Assertions.assertEquals(
                "/dsads/23/adsa/", PublicationURIUtils.resolveToRelativeURL("https://localhost:8080/dsads/23/adsa/"));
    }

    @Test
    public void shouldReturnRelativeUrl_whenAuthorityIsAnIPAddress() throws Exception {
        Assertions.assertEquals(
                "/dsadsadsa", PublicationURIUtils.resolveToRelativeURL("https://127.0.0.1:8080/dsadsadsa"));
        Assertions.assertEquals(
                "/dsads/23/adsa", PublicationURIUtils.resolveToRelativeURL("https://127.0.0.1:8080/dsads/23/adsa"));
        Assertions.assertEquals(
                "/dsadsadsa/", PublicationURIUtils.resolveToRelativeURL("https://127.0.0.1:8080/dsadsadsa/"));
        Assertions.assertEquals(
                "/dsads/23/adsa/", PublicationURIUtils.resolveToRelativeURL("https://127.0.0.1:8080/dsads/23/adsa/"));
    }
}
