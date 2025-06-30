package announcements;

import java.util.Date;

public class FileInformation {
    public String fileName;
    public String fileType;
    public String modfiferTime;
    public String fileSize;
    public String filePath;

    @Override
    public String toString() {
        return fileType + "  " + fileName + "  " + filePath + "  " + modfiferTime + "  " + fileSize;
    }
}
