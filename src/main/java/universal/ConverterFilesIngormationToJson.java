package universal;

import announcements.ListClientsFiles;
import com.google.gson.Gson;


public class ConverterFilesIngormationToJson {

    public static String convertInitClientToServer(ListClientsFiles filesInformation) {
        Gson gson = new Gson();
        String json = gson.toJson(filesInformation);

        return json;
    }

    public static ListClientsFiles restoreInitClientToServer(String filesInformation) {
        Gson gson = new Gson();
        ListClientsFiles information = gson.fromJson(filesInformation, ListClientsFiles.class);

        return information;
    }

}
