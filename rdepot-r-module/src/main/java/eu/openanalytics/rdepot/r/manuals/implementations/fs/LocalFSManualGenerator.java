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
package eu.openanalytics.rdepot.r.manuals.implementations.fs;

import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.manuals.ManualGenerator;
import eu.openanalytics.rdepot.r.storage.exceptions.GenerateManualException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalFSManualGenerator implements ManualGenerator {

    private final String separator = FileSystems.getDefault().getSeparator();

    @Override
    public void generateManual(RPackage packageBag) throws GenerateManualException {
        final File targzFile = new File(packageBag.getSource());

        if (!targzFile.exists()
                || targzFile.getParentFile() == null
                || !targzFile.getParentFile().exists()) {
            log.error("Invalid package source!");
            throw new GenerateManualException(packageBag);
        }
        final String packageName = packageBag.getName().replaceAll("[^a-zA-Z0-9-_]", "");
        final File manualPdf = new File(targzFile.getParent(), packageName + separator + packageName + ".pdf");

        if (manualPdf.getParentFile() != null && manualPdf.getParentFile().exists()) {
            if (manualPdf.exists()) {
                log.warn("Manual already exists!");
                return;
            }

            ProcessBuilder pb = new ProcessBuilder("R");

            pb.command().add("CMD");
            pb.command().add("Rd2pdf");
            pb.command().add("--no-preview");
            if (packageBag.getEncoding() != null) {
                pb.command().add("--encoding=" + packageBag.getEncoding());
            }
            pb.command().add("--title=" + packageName);
            pb.command().add("--output=" + packageName + ".pdf");
            pb.command().add(".");
            pb.directory(manualPdf.getParentFile()).redirectErrorStream(true);

            Process process = null;
            try {
                process = pb.start();

                String outputLine;
                log.debug("Rd2pdf output: ");
                try (BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    while ((outputLine = out.readLine()) != null) log.debug(outputLine.replaceAll("[\r\n]", ""));
                }

                int exitValue = process.waitFor();
                if (exitValue != 0) {
                    log.error("Rd2pdf failed with exit code: {}", exitValue);
                    throw new GenerateManualException(packageBag);
                }
            } catch (IOException | InterruptedException e) {
                log.error(e.getMessage(), e);
                throw new GenerateManualException(packageBag);
            } finally {
                if (process != null && process.isAlive()) process.destroyForcibly();
            }
        }
    }
}
