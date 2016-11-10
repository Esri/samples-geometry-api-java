package com.esri.core.geometry.examples;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Augments DataInputStream with methods to read little-endian primitives, as the ESRI
 * shapefile format is mixed.
 */
class MixedEndianDataInputStream extends DataInputStream {
    private byte readBuffer[] = new byte[8];

    MixedEndianDataInputStream(InputStream inputStream) {
        super(inputStream);
    }

    /**
     * copied from {@link DataInputStream#readInt()} just reversing the variable names
     */
    final int readLittleEndianInt() throws IOException {
        int ch4 = in.read();
        int ch3 = in.read();
        int ch2 = in.read();
        int ch1 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }

    /**
     * copied from {@link DataInputStream#readLong()} ()} just reversing the array indices
     */
    private long readLittleEndianLong() throws IOException {
        readFully(readBuffer, 0, 8);
        return (((long)readBuffer[7] << 56) +
                ((long)(readBuffer[6] & 255) << 48) +
                ((long)(readBuffer[5] & 255) << 40) +
                ((long)(readBuffer[4] & 255) << 32) +
                ((long)(readBuffer[3] & 255) << 24) +
                ((readBuffer[2] & 255) << 16) +
                ((readBuffer[1] & 255) <<  8) +
                ((readBuffer[0] & 255)));
    }

    double readLittleEndianDouble() throws IOException {
        return Double.longBitsToDouble(readLittleEndianLong());
    }

}
