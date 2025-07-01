package universal;

import universal.announcements.FileInformation;
import universal.models.FileStatus;

import java.util.*;

public class FileStatusComparator {

    public static List<FileInformation> compare(List<FileInformation> clientList, List<FileInformation>  serverList) {
        Map<String, FileInformation> serverMap = new HashMap<>();

        for (FileInformation file : serverList) {
            serverMap.put(file.filePath, file);
        }

        List<FileInformation> result = new ArrayList<>();

        for (FileInformation clientFile : clientList) {
            FileInformation serverFile = serverMap.remove(clientFile.filePath);

            if (serverFile == null) {
                clientFile.fileStatus = FileStatus.SEND;
            } else if (clientFile.modfiferTime.equals(serverFile.modfiferTime)) {
                clientFile.fileStatus = FileStatus.WITHOUT_CHANGES;
            } else {
                clientFile.fileStatus = FileStatus.MODIFED;
            }

            result.add(clientFile);
        }

        for (FileInformation serverLeft : serverMap.values()) {
            serverLeft.fileStatus = FileStatus.DELETE;
            result.add(serverLeft);
        }

        return result;
    }

}
