package universal;

public class FormaterTerminalText {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_UNBOLD = "\u001B[21m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static void printServerComunicate(String text ) {
        System.out.println("\t" + ANSI_YELLOW + ANSI_BOLD + text + ANSI_UNBOLD + ANSI_RESET);
    }

    public static void printSucess(String text ) {
        System.out.println(ANSI_GREEN + ANSI_BOLD + text + ANSI_UNBOLD + ANSI_RESET);
    }

    public static void printFailure(String text ) {
        System.out.println(ANSI_RED + ANSI_BOLD + text + ANSI_UNBOLD + ANSI_RESET);
    }

    public static void printNormal(String text ) {
        System.out.println(text);
    }

    public static void printTextInputs(String text ) {
        System.out.print(ANSI_CYAN + ANSI_BOLD + text + ANSI_UNBOLD + ANSI_RESET);
    }
}
