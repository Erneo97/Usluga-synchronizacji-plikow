package universal;

import java.io.Serializable;

public class FilePart implements Serializable {
    public static final int maxSizePart = 16 * 1024; // 16 KB

    public int partSize;
    public int fullSizeOfFile;
    public int partNumber;
    public String pathFile;
    public byte[] data;

    public FilePart(byte[] buffer, int read, int fullFileSize, int partNumber, String pathFile) {
        this.partSize = Math.min(read, maxSizePart);
        this.data = new byte[this.partSize];
        this.pathFile = pathFile;

        System.arraycopy(buffer, 0, this.data, 0, this.partSize);

        this.fullSizeOfFile = fullFileSize;
        this.partNumber = partNumber;
    }
}