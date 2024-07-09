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
package eu.openanalytics.rdepot.python.validation.exceptions;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.BindingResult;

@AllArgsConstructor
@Getter
@Setter
public class PythonRepositoryValidationError extends Exception {
    private static final long serialVersionUID = 1L;
    private transient BindingResult bindingResult;

    private void writeObject(ObjectOutputStream out)
            throws IOException, ClassNotFoundException, NotSerializableException {
        out.defaultWriteObject();
        out.writeObject(getBindingResult());
    }

    private void readObject(ObjectInputStream in) throws IOException, NotSerializableException, ClassNotFoundException {
        in.defaultReadObject();
        bindingResult = (BindingResult) in.readObject();
    }
}
