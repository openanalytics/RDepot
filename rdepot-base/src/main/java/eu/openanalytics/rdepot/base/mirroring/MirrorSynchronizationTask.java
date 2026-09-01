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
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MirrorSynchronizationTask<P extends MirroredPackage, M extends Mirror<P>, R extends Repository> {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || !obj.getClass().equals(getClass())) return false;
        MirrorSynchronizationTask<?, ?, ?> that = (MirrorSynchronizationTask<?, ?, ?>) obj;
        if (this.repositoryToSynchronize.getId() != that.repositoryToSynchronize.getId()) return false;
        if (this.mirrorsToSynchronize.size() != that.mirrorsToSynchronize.size()) return false;
        for (int i = 0; i < mirrorsToSynchronize.size(); i++) {
            if (!mirrorsToSynchronize.get(i).equals(that.mirrorsToSynchronize.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryToSynchronize.getId(), mirrorsToSynchronize);
    }

    private final List<M> mirrorsToSynchronize;
    private final R repositoryToSynchronize;

    @Override
    public String toString() {
        final StringBuilder task = new StringBuilder();
        task.append("Mirroring task for repository: ");
        task.append(repositoryToSynchronize.getName());
        task.append("\nMirrors:");
        for (M mirror : mirrorsToSynchronize) {
            task.append("\n\t - ").append(mirror.getUri());
        }

        return task.toString();
    }
}
