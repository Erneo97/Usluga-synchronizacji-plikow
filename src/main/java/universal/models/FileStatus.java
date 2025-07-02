package universal.models;

/**
 * Enum {@code FileStatus} reprezentuje różne statusy, które może mieć plik
 * w systemie zarządzania plikami.
 *
 * <ul>
 *   <li>{@link #SEND} — plik został wysłany.</li>
 *   <li>{@link #WITHOUT_CHANGES} — plik nie został zmieniony.</li>
 *   <li>{@link #DELETE} — plik został oznaczony do usunięcia.</li>
 *   <li>{@link #MODIFED} — plik został zmodyfikowany.</li>
 *   <li>{@link #NO_INFORMATION} — brak informacji o statusie pliku.</li>
 *   <li>{@link #MOVE} — plik został przeniesiony.</li>
 * </ul>
 */
public enum FileStatus {
    SEND,
    WITHOUT_CHANGES,
    DELETE,
    MODIFED,
    NO_INFORMATION,
    MOVE,
}
