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
package eu.openanalytics.rdepot.r.storage.population.implementations;

import eu.openanalytics.rdepot.base.storage.PersistentStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.DownloadFileException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.entities.Vignette;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadPackageVignetteException;
import eu.openanalytics.rdepot.r.storage.population.VignetteReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@ConditionalOnProperty(name = "storage.implementation", havingValue = "local", matchIfMissing = true)
@Component
public class LocalVignetteReader implements VignetteReader {

    private static final String separator = FileSystems.getDefault().getSeparator();

    private final PersistentStorage<RPackage, RRepository> persistentStorage;

    public LocalVignetteReader(PersistentStorage<RPackage, RRepository> persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    @Override
    public List<Vignette> getAvailableVignettes(RPackage packageBag) {
        if (packageBag == null) {
            return List.of();
        }
        final List<Vignette> vignettes = new ArrayList<>();

        File vignettesFolder = new File(
                new File(packageBag.getSource()).getParent(),
                packageBag.getName() + separator + "inst" + separator + "doc" + separator);

        File[] vignetteFiles = new File[0];
        if (vignettesFolder.exists() && vignettesFolder.isDirectory()) {
            vignetteFiles = vignettesFolder.listFiles((File dir, String name) -> (name != null
                    && (name.toLowerCase().endsWith(".html")
                            || name.toLowerCase().endsWith(".pdf"))));
        }

        for (File vignetteFile : ArrayUtils.nullToEmpty(vignetteFiles, File[].class)) {
            if (FilenameUtils.getExtension(vignetteFile.getName()).equals("html")) {
                try {
                    Document htmlDoc = Jsoup.parse(vignetteFile, "UTF-8");

                    vignettes.add(new Vignette(htmlDoc.title(), vignetteFile.getName()));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                vignettes.add(new Vignette(FilenameUtils.getBaseName(vignetteFile.getName()), vignetteFile.getName()));
            }
        }
        return vignettes;
    }

    @Override
    public byte[] readVignette(RPackage packageBag, String filename) throws ReadPackageVignetteException {
        final String vignetteFilename = new File(packageBag.getSource()).getParent() + separator + packageBag.getName()
                + separator + "inst" + separator + "doc" + separator + filename;
        final File file = new File(vignetteFilename);

        try {
            return persistentStorage.readFile(file.toPath());
        } catch (DownloadFileException e) {
            log.error(e.getMessage(), e);
            throw new ReadPackageVignetteException(e);
        }
    }

    @Override
    public List<File> findVignettesInDir(File extractedDir) {
        final File vignettesFolder = new File(extractedDir + separator + "inst" + separator + "doc" + separator);
        if (vignettesFolder.exists() && vignettesFolder.isDirectory()) {
            return FileUtils.listFiles(vignettesFolder, new String[] {".html", ".pdf"}, false).stream()
                    .toList();
        } else {
            log.debug("Vignettes folder does not exist: {}", extractedDir.getAbsolutePath());
            return List.of();
        }
    }
}
