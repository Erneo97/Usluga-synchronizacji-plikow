package universal;

import announcements.InitClientToServer;
import com.google.gson.Gson;


public class ConverterFilesIngormationToJson {

    public static String convertInitClientToServer(InitClientToServer filesInformation) {
        Gson gson = new Gson();
        String json = gson.toJson(filesInformation);

        return json;
    }

    public static InitClientToServer restoreInitClientToServer(String filesInformation) {
        Gson gson = new Gson();
        InitClientToServer information = gson.fromJson(filesInformation, InitClientToServer.class);

        return information;
    }

}
