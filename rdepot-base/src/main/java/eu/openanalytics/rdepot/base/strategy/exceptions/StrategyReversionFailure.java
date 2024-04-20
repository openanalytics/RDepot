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
package eu.openanalytics.rdepot.base.strategy.exceptions;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;

import java.io.Serial;

/**
 * Thrown when there was a {@link StrategyFailure} that requires rollback
 * but it could not be performed due to an error.
 */
public class StrategyReversionFailure extends StrategyFailure {

	@Serial
	private static final long serialVersionUID = -6275355677809984480L;

	public StrategyReversionFailure(Exception reason) {
		super(reason, MessageCodes.REVERSE_STRATEGY_FAILURE, false);
	}

}
