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
import java.util.List;
import java.util.concurrent.*;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.tomcat.util.buf.StringUtils;

@Slf4j
public class BashTestEnvironmentConfigurator implements TestEnvironmentConfigurator {

	private final static int TIMEOUT = 10;
	private final static int MAX_RETRIES = 3;
	private static BashTestEnvironmentConfigurator instance;
	private BashTestEnvironmentConfigurator() {}
	
	public static BashTestEnvironmentConfigurator getInstance() {
		if(instance == null) {
			instance = new BashTestEnvironmentConfigurator();
		}
		
		return instance;
	}
	
	@Override
	public void restoreEnvironment() throws Exception {
		executeWithRetries(() -> {
            try {
                executeBashScript("src/test/resources/scripts/restore.sh");
            } catch (Exception e) {
				throw new RuntimeException(e);
			}
        });
	}
	
	@Override
	public void backupEnvironment() throws Exception {
		executeWithRetries(() -> {
            try {
                executeBashScript("src/test/resources/scripts/backupDeclarative.sh");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
	}
	
	protected void executeBashScript(String ...args) {
		String[] cmd = ArrayUtils.addAll(new String[]{"/bin/bash"}, args);
		execute(cmd);
	}

	protected void executeBashCommand(@NonNull final String bashCmd) {
		String[] cmd = ArrayUtils.addAll(new String[]{"/bin/bash", "-c"},"\"" + bashCmd + "\"");
		execute(cmd);
	}

	protected void execute(String... args) {
		try {
			Process process = new ProcessBuilder(args).redirectErrorStream(true).start();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String msg;
			while((msg = reader.readLine()) != null) {}
			process.waitFor();
			process.destroy();
		} catch(InterruptedException | IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void restoreDeclarative() throws Exception {
		executeWithRetries(() -> {
            try {
                executeBashScript("src/test/resources/scripts/restoreDeclarative.sh");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
	}

	@Override
	public void blockRepoContainer(Runnable testMethod) throws Exception {
		executeWithRetries(() -> {
			executeBashScript("src/test/resources/scripts/blockRepo.sh");
		});
		testMethod.run();
		executeWithRetries(() -> {
			executeBashScript("src/test/resources/scripts/unblockRepo.sh");
		});
	}

	private void executeWithRetries(Runnable method) throws Exception {
		int remainingAttempts = MAX_RETRIES;

		while(remainingAttempts > 0) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<?> future = executor.submit(method);
			try {
				future.get(TIMEOUT, TimeUnit.SECONDS);
				executor.shutdownNow();
				break;
			} catch(TimeoutException e) {
				future.cancel(true);
				executeBashCommand("pkill -9 -f 'docker exec'");

				log.warn("Restore timeout! [ATTEMPT "
						+ (MAX_RETRIES - remainingAttempts + 1) + "/" + MAX_RETRIES + "]");
				remainingAttempts--;
				executor.shutdownNow();
			}
		}
	}
}
