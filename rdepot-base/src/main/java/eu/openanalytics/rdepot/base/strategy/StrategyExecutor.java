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
package eu.openanalytics.rdepot.base.strategy;

import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * This is a "wrapper" bean that executes {@link Strategy Strategies}
 * and makes sure that their {@link org.springframework.transaction.annotation.Transactional}
 * properties are preserved.
 */
@Component
@AllArgsConstructor
public class StrategyExecutor {

    private final TransactionalStrategyExecutor transactionalStrategyExecutor;

    public <T extends Resource> T execute(Strategy<T> strategy) throws StrategyFailure {
        synchronized (this) {
            final T result = transactionalStrategyExecutor.perform(strategy);
            transactionalStrategyExecutor.postStrategy(strategy);
            return result;
        }
    }
}
