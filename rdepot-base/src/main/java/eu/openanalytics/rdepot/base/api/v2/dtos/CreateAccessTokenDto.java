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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for
 * {@link eu.openanalytics.rdepot.base.entities.AccessToken Access Tokens}
 * creation.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccessTokenDto {

    @NotBlank(message = MessageCodes.TOKEN_NAME_MUST_BE_NOT_BLANK)
    private String name;

    @DecimalMin(value = "1", message = MessageCodes.TOKEN_NUMBER_OF_DAYS_MUST_BE_GREATER_THAN_0)
    @DecimalMax(value = "365", message = MessageCodes.TOKEN_NUMBER_OF_DAYS_MUST_BE_LESS_THAN_366)
    private String lifetime;
}
