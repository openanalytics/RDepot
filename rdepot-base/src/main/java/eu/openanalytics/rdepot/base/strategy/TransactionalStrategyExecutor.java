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
package eu.openanalytics.rdepot.base.strategy;

import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is a "wrapper" bean that executes {@link Strategy Strategies}
 * and makes sure that their {@link Transactional}
 * properties are preserved.
 */
@Component
public class TransactionalStrategyExecutor {

    @Transactional(rollbackFor = StrategyFailure.class)
    public <T extends Resource> T perform(Strategy<T> strategy) throws StrategyFailure {
        return strategy.perform();
    }

    @Transactional(rollbackFor = StrategyFailure.class)
    public <T extends Resource> void postStrategy(Strategy<T> strategy) throws StrategyFailure {
        strategy.postStrategy();
    }
}
