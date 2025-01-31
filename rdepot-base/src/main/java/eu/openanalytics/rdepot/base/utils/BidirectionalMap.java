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
package eu.openanalytics.rdepot.base.utils;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

/**
 * Elements put into this {@link Map}
 * are also put to an "inverted" map where key is a value and vice-versa.
 * @param <V> type of both keys and values - must be the same since the same element is used both as key and value
 */
@Getter
public class BidirectionalMap<V> extends HashMap<V, V> {
    @Serial
    private static final long serialVersionUID = -3262286821725572695L;

    /**
     * -- GETTER --
     *  Returns inverted version of this map.
     *  Since the inverted map is maintained alongside this one,
     *  it is not built while calling this method,
     *  therefore not having any performance implications.
     */
    private final HashMap<V, V> inverted = new HashMap<>();

    @Override
    public V put(V key, V value) {
        if (this.containsKey(key) || inverted.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Bidirectional map cannot be used " + "with duplicate value on any end: " + value);
        }
        inverted.put(value, key);
        return super.put(key, value);
    }
}
