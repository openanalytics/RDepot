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
package eu.openanalytics.rdepot.base.mirroring;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * Coordinates mirroring for given technology.
 * Since mirroring is an asynchronous operation,
 * this component makes sure that various mirroring tasks
 * do not collide and that unnecessary duplicates of them
 * are not created by multiple threads.
 */
@Slf4j
public abstract class MirrorSynchronizationCoordinator<
        P extends MirroredPackage, M extends Mirror<P>, R extends Repository> {

    private final BlockingQueue<MirrorSynchronizationTask<P, M, R>> tasks = new LinkedBlockingQueue<>();
    private final Set<MirrorSynchronizationTask<P, M, R>> taskSet = new HashSet<>();
    private final MirrorSynchronizer<P, M, ?, R> mirrorSynchronizer;
    private boolean running = false;

    protected MirrorSynchronizationCoordinator(MirrorSynchronizer<P, M, ?, R> mirrorSynchronizer) {
        this.mirrorSynchronizer = mirrorSynchronizer;
    }

    /**
     * Adds provided mirroring task to the queue.
     * If such a task already is in the queue, it will simply be ignored.
     */
    public synchronized void submitMirroringTask(MirrorSynchronizationTask<P, M, R> task) {
        if (taskSet.contains(task)) {
            log.debug("This mirroring task {} is already present in the queue. Skipping.", task.toString());
            return;
        }
        log.debug("Adding mirroring task {} to the queue.", task.toString());
        taskSet.add(task);

        try {
            tasks.put(task);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        if (!running) {
            log.debug("Starting synchronization task processing thread.");
            startTaskProcessingThread();
        }
    }

    /**
     * Creates consumer thread that processes
     * {@link MirrorSynchronizationTask mirroring synchronization tasks}.
     */
    private void startTaskProcessingThread() {
        Thread consumerThread = new Thread(this::processTask, "TaskProcessor");
        consumerThread.setDaemon(true);
        running = true;
        consumerThread.start();
    }

    private void processTask() {
        while (running && !tasks.isEmpty()) {
            try {
                final MirrorSynchronizationTask<P, M, R> task = tasks.take();
                taskSet.remove(task);
                mirrorSynchronizer.synchronizeWithMirrors(
                        task.getRepositoryToSynchronize(), task.getMirrorsToSynchronize());
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                break;
            }
        }
        running = false;
    }
}
