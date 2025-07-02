package universal;

import universal.announcements.InitClientToServer;
import universal.announcements.ListClientsFiles;
import com.google.gson.Gson;
import universal.models.NextSyncTime;
import universal.models.StateServer;

/**
 * Klasa pomocnicza do konwersji obiektów Java na JSON oraz odwrotnie.
 * Wykorzystuje bibliotekę Gson do serializacji i deserializacji.
 */
public class ConverterClassToJson {

    /**
     * Konwertuje dowolny obiekt na reprezentację JSON w postaci Stringa.
     *
     * @param filesInformation obiekt do konwersji na JSON
     * @return łańcuch znaków w formacie JSON reprezentujący obiekt
     */
    public static String convert(Object filesInformation) {
        Gson gson = new Gson();
        String json = gson.toJson(filesInformation);
        return json;
    }

    /**
     * Odtwarza obiekt typu {@link ListClientsFiles} z podanego JSON-a.
     *
     * @param communicate JSON do deserializacji
     * @return odtworzony obiekt ListClientsFiles
     */
    public static ListClientsFiles restoreFileInformation(String communicate) {
        return (ListClientsFiles) restoreDefault(communicate, ListClientsFiles.class);
    }

    /**
     * Odtwarza obiekt typu {@link InitClientToServer} z podanego JSON-a.
     *
     * @param communicate JSON do deserializacji
     * @return odtworzony obiekt InitClientToServer
     */
    public static InitClientToServer restoreInitClientToServer(String communicate) {
        return (InitClientToServer) restoreDefault(communicate, InitClientToServer.class);
    }

    /**
     * Odtwarza wartość enum {@link StateServer} z podanego JSON-a.
     *
     * @param communicate JSON do deserializacji
     * @return odtworzony enum StateServer
     */
    public static StateServer restoreStateServer(String communicate) {
        return (StateServer) restoreDefault(communicate, StateServer.class);
    }

    /**
     * Odtwarza wartość enum {@link NextSyncTime} z podanego JSON-a.
     *
     * @param communicate JSON do deserializacji
     * @return odtworzony enum NextSyncTime
     */
    public static NextSyncTime restoreNextSyncTime(String communicate) {
        return (NextSyncTime) restoreDefault(communicate, NextSyncTime.class);
    }

    /**
     * Metoda pomocnicza deserializująca JSON do obiektu podanego typu.
     *
     * @param communicate JSON do deserializacji
     * @param klasa klasa docelowego typu
     * @return odtworzony obiekt typu podanego jako parametr
     */
    private static Object restoreDefault(String communicate, Class<?> klasa) {
        Gson gson = new Gson();
        return gson.fromJson(communicate, klasa);
    }
}
