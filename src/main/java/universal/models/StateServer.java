package universal.models;

/**
 * Enum {@code StateServer} reprezentuje możliwe stany serwera
 * w trakcie obsługi połączeń i operacji.
 *
 * <ul>
 *   <li>{@link #BUSY} — serwer jest zajęty należy poczekać na syknał {@link #READY}.</li>
 *   <li>{@link #READY} — serwer jest gotowy do obsługi nowych żądań.</li>
 *   <li>{@link #PERMISION_DENIED} — dostęp został odmówiony (brak uprawnień).</li>
 *   <li>{@link #CONNECTED} — nawiązano połączenie z klientem.</li>
 *   <li>{@link #DONE} — przesył plików została zakończona pomyślnie</li>
 *   <li>{@link #CONTINUE} — potrzymanie komunikacji z klientem
 *   <li>{@link #FORCED_END} — .klient daje znać serwerowi że zostanie zamknięty a transmisja zakończona
 * </ul>
 */
public enum StateServer {
    BUSY,
    READY,
    PERMISION_DENIED,
    CONNECTED,
    DONE,
    CONTINUE,
    FORCED_END,
}
