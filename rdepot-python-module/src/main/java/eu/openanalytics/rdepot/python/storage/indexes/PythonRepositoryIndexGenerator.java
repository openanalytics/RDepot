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
package eu.openanalytics.rdepot.python.storage.indexes;

import eu.openanalytics.rdepot.base.entities.Hashable;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.indexes.RepositoryIndexGenerator;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.storage.implementations.fs.PythonLocalStorage;
import eu.openanalytics.rdepot.python.storage.indexes.resolvers.PythonRepositoryPublicationURIResolver;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PythonRepositoryIndexGenerator extends RepositoryIndexGenerator<PythonRepository, PythonPackage> {

    private final PythonLocalStorage storage;

    public PythonRepositoryIndexGenerator(
            @Value("classpath:templates/python/index_template.html") Resource indexTemplate,
            @Value("classpath:templates/python/index_anchor_template.html") Resource indexAnchorTemplate,
            PythonLocalStorage storage)
            throws IOException {
        super(
                indexTemplate.getContentAsString(Charset.defaultCharset()),
                indexAnchorTemplate.getContentAsString(Charset.defaultCharset()),
                storage,
                new PythonRepositoryPublicationURIResolver());
        this.storage = storage;
    }

    @Override
    protected String getPackageListEnding() {
        return "\n</body>\n</html>\n";
    }

    @Override
    protected String calculateChecksum(Hashable item, String path) throws IOException {
        try {
            return storage.calculateChecksum(item.getHashMethod(), path);
        } catch (CheckSumCalculationException e) {
            log.error(e.getMessage(), e);
            throw new IOException(e);
        }
    }
}
