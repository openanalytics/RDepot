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
package eu.openanalytics.rdepot.integrationtest.environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
public class BashScriptExecutor {
    public void executeBashScript(String... args) {
        String[] cmd = ArrayUtils.addAll(new String[] {"/bin/bash"}, args);
        execute(cmd);
    }

    public void executeBashCommand(@NonNull final String bashCmd) {
        String[] cmd = ArrayUtils.addAll(new String[] {"/bin/bash", "-c"}, "\"" + bashCmd + "\"");
        execute(cmd);
    }

    public void execute(String... args) {
        try {
            final Process process =
                    new ProcessBuilder(args).redirectErrorStream(true).start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String msg;
            while ((msg = reader.readLine()) != null) {}
            process.waitFor();
            process.destroy();
        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
