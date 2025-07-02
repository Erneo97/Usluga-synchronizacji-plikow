package universal.models;

/**
 * Enum {@code NextSyncTime} reprezentuje możliwe interwały czasowe
 * do synchronizacji w milisekundach.
 *
 * <p>Dostępne wartości:
 * <ul>
 *   <li>{@link #sec_5} — 5 sekund (5000 ms)</li>
 *   <li>{@link #sec_30} — 30 sekund (30000 ms)</li>
 *   <li>{@link #m_5} — 5 minut (300000 ms)</li>
 *   <li>{@link #h_1} — 1 godzina (3600000 ms)</li>
 * </ul>
 *
 * <p>Każda stała enum przechowuje wartość interwału w milisekundach,
 * którą można pobrać metodą {@link #getMilliseconds()}.
 */
public enum NextSyncTime {
    sec_5(5_000),
    sec_30(30_000),
    m_5(5 * 60_000),
    h_1(60 * 60_000);

    private final long milliseconds;

    /**
     * Konstruktor przypisujący wartość interwału w milisekundach.
     *
     * @param milliseconds interwał czasowy w milisekundach
     */
    NextSyncTime(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    /**
     * Zwraca wartość interwału czasowego w milisekundach.
     *
     * @return interwał w milisekundach
     */
    public long getMilliseconds() {
        return milliseconds;
    }
}
