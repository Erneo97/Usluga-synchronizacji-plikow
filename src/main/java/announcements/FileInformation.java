package announcements;

public class FileInformation {
    public String fileName;
    public String fileType;
    public String modfiferTime;
    public String fileSize;
    public String filePath;
    public FileStatus fileStatus;

    public FileInformation() {this.fileStatus = FileStatus.NO_INFORMATION;}

    @Override
    public String toString() {
        return String.format("%-4s", fileType) + "  " + String.format("%-25s", fileName) + "  "
                + String.format("%-35s", filePath) + "  " + modfiferTime  + "  "
                + String.format("%10s", fileSize) + "  (" + fileStatus + ")";
    }
}
