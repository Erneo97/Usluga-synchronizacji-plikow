package universal;

import announcements.InitClientToServer;
import announcements.ListClientsFiles;
import com.google.gson.Gson;


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

    private static Object restoreDefault(String communicate, Class<?> klasa) {
        Gson gson = new Gson();
        return gson.fromJson(communicate, klasa);
    }
}
