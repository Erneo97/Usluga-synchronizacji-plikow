package server;

import java.io.Serializable;
import java.util.Arrays;

public class FilePart implements Serializable {
    public final int maxSizePart = 16 * 1024; // bytes

    public int partSize;
    public int fullSizeOfFile;
    public int partNumber;
    public byte[] data;

    public FilePart(byte[] buffer, int read, int fullFileSize, int partNumber) {
        this.partSize = Math.min(read, maxSizePart);
        this.data = new byte[this.partSize];

        System.arraycopy(buffer, 0, this.data, 0, this.partSize);

        this.fullSizeOfFile = fullFileSize;
        this.partNumber = partNumber;
    }
}
