/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.strategy.exceptions;

import eu.openanalytics.rdepot.base.messaging.RefactoredMessageCodes;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.strategy.Strategy;

/**
 * It is thrown in case of {@link Strategy} failure.
 */
public class StrategyFailure extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8980879718772746241L;
	private boolean requiresRollback_ = true;
	private final Exception reason;
	
	protected StrategyFailure(Exception reason, String messageCode, boolean requiresRollback) {
		super(resolveFullMessage(reason, messageCode));
		this.requiresRollback_ = requiresRollback;
		this.reason = reason;
	}
	
	public StrategyFailure(Exception reason) {
		this(reason, false);
	}
	
	public StrategyFailure(Exception reason, boolean requiresRollback) {
		super(resolveFullMessage(reason, RefactoredMessageCodes.STRATEGY_FAILURE));
		this.requiresRollback_ = requiresRollback;
		this.reason = reason;
	}
	
	protected static String resolveFullMessage(Exception reason, String messageCode) {
		String strategyFailureMessage = 
				StaticMessageResolver.getMessage(messageCode);
		String reasonMessage = reason.getMessage();
		
		return strategyFailureMessage + ": " + reasonMessage;
	}
	
	/**
	 * Determines if failure requires all introduced changes to be reverted.
	 * @return true or false
	 */
	public boolean requiresRollback() {
		return requiresRollback_;
	}
	
	public Exception getReason() {
		return reason;
	}
}
