package universal;

import universal.announcements.InitClientToServer;
import universal.announcements.ListClientsFiles;
import com.google.gson.Gson;
import universal.models.NextSyncTime;
import universal.models.StateServer;


public class ConverterClassToJson {

    public static String convert(Object filesInformation) {
        Gson gson = new Gson();
        String json = gson.toJson(filesInformation);

        return json;
    }

    public static ListClientsFiles restoreFileInformation(String communicate) {
        return (ListClientsFiles)restoreDefault(communicate, ListClientsFiles.class);
    }

    public static InitClientToServer restoreInitClientToServer(String communicate) {
        return (InitClientToServer)restoreDefault(communicate, InitClientToServer.class);
    }

    public static StateServer restoreStateServer(String communicate) {
        return (StateServer)restoreDefault(communicate, StateServer.class);
    }

    public static NextSyncTime restoreNextSyncTime(String communicate) {
        return (NextSyncTime)restoreDefault(communicate, NextSyncTime.class);
    }

    private static Object restoreDefault(String communicate, Class<?> klasa) {
        Gson gson = new Gson();
        return gson.fromJson(communicate, klasa);
    }
}
