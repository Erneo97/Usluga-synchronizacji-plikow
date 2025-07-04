package universal;

/**
 * Klasa pomocnicza do formatowanego wyświetlania tekstu w terminalu.
 * Umożliwia wypisywanie tekstu z kolorami i wyróżnieniami takimi jak pogrubienie.
 * Wykorzystuje kody ANSI do formatowania tekstu.
 */
public class FormaterTerminalText {
    /**
     * Resetowanie formatu tekstu (przywrócenie domyślnego stylu).
     */
    public static final String ANSI_RESET = "\u001B[0m";

    /**
     * Kolor czerwony (używany np. do błędów).
     */
    public static final String ANSI_RED = "\u001B[31m";

    /**
     * Kolor zielony (np. do komunikatów o powodzeniu).
     */
    public static final String ANSI_GREEN = "\u001B[32m";

    /**
     * Kolor żółty (np. do informacji i komunikatów serwera).
     */
    public static final String ANSI_YELLOW = "\u001B[33m";

    /**
     * Pogrubienie tekstu.
     */
    public static final String ANSI_BOLD = "\u001B[1m";

    /**
     * Cofnięcie pogrubienia tekstu.
     */
    public static final String ANSI_UNBOLD = "\u001B[21m";

    /**
     * Kolor cyjan (niebiesko-zielony, do wyróżnień).
     */
    public static final String ANSI_CYAN = "\u001B[36m";

    /**
     * Wypisuje komunikat serwera w kolorze żółtym i pogrubioną czcionką, poprzedzony tabulatorem.
     *
     * @param text tekst do wyświetlenia
     */
    public static void printServerComunicate(String text) {
        System.out.println("\t" + ANSI_YELLOW + ANSI_BOLD + text + ANSI_UNBOLD + ANSI_RESET);
    }

    /**
     * Wypisuje komunikat sukcesu w zielonym i pogrubionym stylu.
     *
     * @param text tekst do wyświetlenia
     */
    public static void printSucess(String text) {
        System.out.println(ANSI_GREEN + ANSI_BOLD + text + ANSI_UNBOLD + ANSI_RESET);
    }

    /**
     * Wypisuje komunikat błędu w czerwonym i pogrubionym stylu.
     *
     * @param text tekst do wyświetlenia
     */
    public static void printFailure(String text) {
        System.out.println(ANSI_RED + ANSI_BOLD + text + ANSI_UNBOLD + ANSI_RESET);
    }

    /**
     * Wypisuje zwykły, nieformatowany tekst.
     *
     * @param text tekst do wyświetlenia
     */
    public static void printNormal(String text) {
        System.out.println(text);
    }

    /**
     * Wypisuje tekst zachęty do wpisania danych (np. zapytanie), w kolorze cyjan i pogrubione.
     * Nie dodaje nowej linii na końcu, aby umożliwić wpis użytkownika w tej samej linii.
     *
     * @param text tekst zachęty
     */
    public static void printTextInputs(String text) {
        System.out.print(ANSI_CYAN + ANSI_BOLD + text + ANSI_UNBOLD + ANSI_RESET);
    }

    public static void printprogressBar(String text, int progress, int max)  {
        if( progress < max ) {
            double procent = progress * 100.0 /max;
            System.out.print("\r" + " ".repeat(80) + "\r");
            System.out.print(text +  progress + "/" + max + " " + loadBar(procent, 20) + "  " + String.format("%.2f", procent) + "%" );

        }
    }
    private  static String loadBar(double procent, int width) {
        int countDot = (int)(procent * width / 100);
        return "[" + ".".repeat(countDot) + " ".repeat(width-countDot) + "]";
    }
}
