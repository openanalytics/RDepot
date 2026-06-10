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
package eu.openanalytics.rdepot.integrationtest.environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
public class BashScriptExecutor {
    public ExecutionResult executeBashScript(String... args) {
        String[] cmd = ArrayUtils.addAll(new String[] {"/bin/bash"}, args);
        return execute(cmd);
    }

    public ExecutionResult executeBashCommand(@NonNull final String bashCmd) {
        System.out.println(bashCmd);
        String[] cmd = ArrayUtils.addAll(new String[] {"/bin/bash", "-c", bashCmd});
        return execute(cmd);
    }

    public ExecutionResult execute(String... args) {
        try {
            final Process process =
                    new ProcessBuilder(args).redirectErrorStream(true).start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final StringBuilder output = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
                if (line != null) {
                    output.append("\n");
                }
            }
            process.waitFor();
            final int exitCode = process.exitValue();
            process.destroy();
            return new ExecutionResult(exitCode, output.toString());
        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage(), e);
            return new ExecutionResult(-1, "");
        }
    }
}
