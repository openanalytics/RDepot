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
import eu.openanalytics.rdepot.base.storage.indexes.RepositoryIndexGenerator;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.storage.indexes.resolvers.RRepositoryPackagePublicationURIResolver;
import java.io.IOException;
import java.nio.charset.Charset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class RRepositoryIndexGenerator extends RepositoryIndexGenerator<RRepository, RPackage> {
    public RRepositoryIndexGenerator(
            @Value("classpath:templates/r/index_template.html") Resource indexTemplate,
            @Value("classpath:templates/r/index_anchor_template.html") Resource indexAnchorTemplate,
            Storage<RPackage> storage)
            throws IOException {
        super(
                indexTemplate.getContentAsString(Charset.defaultCharset()),
                indexAnchorTemplate.getContentAsString(Charset.defaultCharset()),
                storage,
                new RRepositoryPackagePublicationURIResolver());
    }

    @Override
    protected String generatePackageAnchor(RPackage packageBag) {
        final String anchor = super.generatePackageAnchor(packageBag);
        return RPackageAnchorPropertiesAdder.addPackageAnchorProperties(anchor, packageBag);
    }
}
