package announcements;

import java.util.List;

public class ListClientsFiles {
    public String IP;

    public List<FileInformation> filesInformation;

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("\t" + this.IP + " :\n");
        int index = 0;
        for (FileInformation fileInformation : this.filesInformation) {
            ret.append(index).append(") ").append(fileInformation.toString()).append("\n");
            index++;
        }
        return ret.toString();
    }
}
