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
package eu.openanalytics.rdepot.base.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Used to dynamically resolve messages without having to autowire message source.
 */
@Slf4j
@Component
public class StaticMessageResolver {

    private static MessageSource ms;

    public StaticMessageResolver(MessageSource messageSource) {
        ms = messageSource;
    }
    /**
     * Resolves localized message using the message source loaded from the context.
     */
    public static String getMessage(String messageCode) {
        try {
            return ms.getMessage(messageCode, null, messageCode, LocaleContextHolder.getLocale());
        } catch (ClassCastException | NullPointerException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("Could not properly resolve message source bean!");
        }
    }
}
