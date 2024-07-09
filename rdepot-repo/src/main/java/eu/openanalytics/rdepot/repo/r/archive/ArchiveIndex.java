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
package eu.openanalytics.rdepot.repo.r.archive;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ArchiveIndex {

    // https://tools.ietf.org/html/rfc1832
    // https://cran.r-project.org/doc/manuals/r-release/R-ints.html#Serialization-Formats
    // https://svn.r-project.org/R/trunk/src/include/Rinternals.h

    private static final byte[] HEADER = new byte[] {
        // Offset 0x00000000 to 0x00000022
        0x58,
        0x0A,
        0x00,
        0x00,
        0x00,
        0x03,
        0x00,
        0x03,
        0x06,
        0x01,
        0x00,
        0x03,
        0x05,
        0x00,
        0x00,
        0x00,
        0x00,
        0x05,
        0x55,
        0x54,
        0x46,
        0x2D,
        0x38
    };

    private static final int LGLSXP = 10; // logical
    private static final int INTSXP = 13; // integer
    private static final int REALSXP = 14; // numeric
    private static final int STRSXP = 16; // character
    private static final int VECSXP = 19; // generic vector

    private static final byte[] ATTR = new byte[] {0x00, 0x00, 0x04, 0x02};
    private static final byte[] CLOSE = new byte[] {0x00, 0x00, 0x00, (byte) 0xFE};
    private static final byte[] CHARPREFIX = new byte[] {0x00, 0x04, 0x00, 0x09};
    private static final byte[] PAD2 = new byte[] {0x00, 0x00};

    private static final int NCOLS = 10;

    private final Map<String, List<ArchiveInfo>> archives;

    public ArchiveIndex(Map<String, List<ArchiveInfo>> archives) {
        this.archives = Collections.unmodifiableMap(archives);
    }

    public void serialize(OutputStream output) throws IOException {

        try (DataOutputStream dos = new DataOutputStream(output)) {

            dos.write(HEADER);

            writeVecHeader(dos, VECSXP + (2 << 8), archives.size()); // named list

            ArrayList<String> names = new ArrayList<>();

            final String classAttr = "class";
            final String posixct = "POSIXct";
            final String posixt = "POSIXt";

            for (Map.Entry<String, List<ArchiveInfo>> entry : archives.entrySet()) {

                names.add(entry.getKey());

                writeVecHeader(dos, VECSXP + (3 << 8), NCOLS); // data.frame

                writeVecHeader(dos, REALSXP, entry.getValue().size());
                for (ArchiveInfo archive : entry.getValue()) {
                    dos.writeDouble(archive.size);
                }

                writeVecHeader(dos, LGLSXP, entry.getValue().size());
                for (ArchiveInfo archive : entry.getValue()) {
                    dos.writeInt(archive.isdir);
                }

                writeVecHeader(dos, (INTSXP + (3 << 8)), entry.getValue().size());
                for (ArchiveInfo archive : entry.getValue()) {
                    dos.writeInt(archive.mode);
                }
                writeAttrHeader(dos, classAttr);
                writeVecHeader(dos, STRSXP, 1);
                writeStr(dos, "octmode");
                dos.write(CLOSE);

                writeVecHeader(dos, (REALSXP + (3 << 8)), entry.getValue().size());
                for (ArchiveInfo archive : entry.getValue()) {
                    dos.writeDouble(archive.mtime);
                }
                writeAttrHeader(dos, classAttr);
                writeVecHeader(dos, STRSXP, 2);
                writeStr(dos, posixct);
                writeStr(dos, posixt);
                dos.write(CLOSE);

                writeVecHeader(dos, (REALSXP + (3 << 8)), entry.getValue().size());
                for (ArchiveInfo archive : entry.getValue()) {
                    dos.writeDouble(archive.ctime);
                }
                writeAttrHeader(dos, classAttr);
                writeVecHeader(dos, STRSXP, 2);
                writeStr(dos, posixct);
                writeStr(dos, posixt);
                dos.write(CLOSE);

                writeVecHeader(dos, (REALSXP + (3 << 8)), entry.getValue().size());
                for (ArchiveInfo archive : entry.getValue()) {
                    dos.writeDouble(archive.atime);
                }
                writeAttrHeader(dos, classAttr);
                writeVecHeader(dos, STRSXP, 2);
                writeStr(dos, posixct);
                writeStr(dos, posixt);
                dos.write(CLOSE);

                writeVecHeader(dos, INTSXP, entry.getValue().size());
                for (ArchiveInfo archive : entry.getValue()) {
                    dos.writeInt(archive.uid);
                }

                writeVecHeader(dos, INTSXP, entry.getValue().size());
                for (ArchiveInfo archive : entry.getValue()) {
                    dos.writeInt(archive.gid);
                }

                writeVecHeader(dos, STRSXP, entry.getValue().size());
                for (ArchiveInfo archive : entry.getValue()) {
                    writeStr(dos, archive.uname);
                }

                writeVecHeader(dos, STRSXP, entry.getValue().size());
                for (ArchiveInfo archive : entry.getValue()) {
                    writeStr(dos, archive.grname);
                }

                writeAttrHeader(dos, "names");
                writeVecHeader(dos, STRSXP, NCOLS);
                writeStr(dos, "size");
                writeStr(dos, "isdir");
                writeStr(dos, "mode");
                writeStr(dos, "mtime");
                writeStr(dos, "ctime");
                writeStr(dos, "atime");
                writeStr(dos, "uid");
                writeStr(dos, "gid");
                writeStr(dos, "uname");
                writeStr(dos, "grname");

                writeAttrHeader(dos, "class");
                writeVecHeader(dos, STRSXP, 1);
                writeStr(dos, "data.frame");

                writeAttrHeader(dos, "row.names");
                writeVecHeader(dos, STRSXP, entry.getValue().size());
                for (ArchiveInfo archive : entry.getValue()) {
                    writeStr(dos, archive.path);
                }

                dos.write(CLOSE);
            }

            writeAttrHeader(dos, "names");
            writeStrVec(dos, names);

            dos.write(CLOSE);
        }
    }

    private void writeVecHeader(DataOutputStream dos, int sxp, int size) throws IOException {
        dos.writeInt(sxp);
        dos.writeInt(size);
    }

    private void writeAttrHeader(DataOutputStream dos, String attr) throws IOException {
        dos.write(ATTR);
        dos.writeInt(1);
        writeStr(dos, attr);
    }

    private void writeStrVec(DataOutputStream dos, ArrayList<String> vec) throws IOException {
        dos.writeInt(STRSXP);
        dos.writeInt(vec.size());
        for (String str : vec) {
            writeStr(dos, str);
        }
    }

    private void writeStr(DataOutputStream dos, String string) throws IOException {
        dos.write(CHARPREFIX);
        dos.write(PAD2);
        dos.writeUTF(string);
    }
}
