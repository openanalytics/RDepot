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
package eu.openanalytics.rdepot.r.storage.indexes;

import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.indexes.PackageIndexGenerator;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.storage.indexes.resolvers.RPackagePublicationURIResolver;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RPackageIndexGenerator extends PackageIndexGenerator<RPackage> {
    public RPackageIndexGenerator(
            @Value("classpath:templates/r/package_template.html") Resource packageTemplate,
            @Value("classpath:templates/r/package_anchor_template.html") Resource packageAnchorTemplate,
            Storage<RPackage> storage)
            throws IOException {
        super(
                packageTemplate.getContentAsString(Charset.defaultCharset()),
                packageAnchorTemplate.getContentAsString(Charset.defaultCharset()),
                storage,
                new RPackagePublicationURIResolver());
    }

    @Override
    protected String generatePackageAnchor(RPackage packageBag) {
        final String anchor = super.generatePackageAnchor(packageBag);
        return RPackageAnchorPropertiesAdder.addPackageAnchorProperties(anchor, packageBag);
    }
}
