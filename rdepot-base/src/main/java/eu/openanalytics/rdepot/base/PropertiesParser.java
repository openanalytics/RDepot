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
package eu.openanalytics.rdepot.base;

import java.io.*;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

public class PropertiesParser extends Properties {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 356227594180997607L;

    private final Pattern isKeyValue = Pattern.compile("^[a-zA-Z][a-zA-Z-/@_]*:(?:.*|\\n)$");
    private final Pattern isPartOfValue = Pattern.compile("^(?:[\\t ]+.*|)$");

    public PropertiesParser(File descriptionFile) throws IOException {
        super();
        this.load(new FileInputStream(descriptionFile));
    }

    /**
     * Reads a property list (key and element pairs) from the input
     * byte stream. The input stream is in a simple line-oriented
     * format as specified in
     * {@link #load(java.io.Reader) load(Reader)} and is assumed to use
     * the ISO 8859-1 character encoding; that is each byte is one Latin1
     * character. Characters not in Latin1, and certain special characters,
     * are represented in keys and elements using
     * <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.3">Unicode escapes</a>.
     * <p>
     * The specified stream remains open after this method returns.
     *
     * @param      inStream   the input stream.
     * @exception  IOException  if an error occurred when reading from the
     *             input stream.
     * @throws     IllegalArgumentException if the input stream contains a
     *             malformed Unicode escape sequence.
     * @since 1.2
     */
    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        load0(new Scanner(inStream));
    }

    private void load0(Scanner scanner) throws IOException {
        scanner.useDelimiter("\\n");
        String currentKey = null;
        String currentValue = null;
        String line = null;
        boolean ifMetaData = true;
        while (scanner.hasNext() && ifMetaData) {
            line = scanner.next();

            if (isKeyValue.matcher(line).find()) {
                saveKeyValue(currentKey, currentValue);
                int index = line.indexOf(':');
                currentKey = line.substring(0, index);
                currentValue = line.substring(index + 1) + "\\n";
            } else if (isPartOfValue.matcher(line).find()) {
                currentValue += line + "\\n";
            } else {
                ifMetaData = false;
            }
        }
        saveKeyValue(currentKey, currentValue);
    }

    private void saveKeyValue(String key, String value) {
        if (Objects.nonNull(key) && Objects.nonNull(value)) {
            value = value.replaceAll("\\s{2,}", " ")
                    .replaceAll(" *(?:\\\\n)+ *$", "")
                    .replaceAll("^ *(?:\\\\n)+ *", "")
                    .replaceAll("\\t", " ")
                    .strip();
            if (containsKey(key)) {
                value = get(key).toString() + ", " + value;
            }
            put(key, value);
        }
    }
}
