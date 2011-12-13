package pl.elka.gis.utils;

public class Log {

    private static final boolean LOGGING_ON = true;

    public static void d(String logtag, String message) {
        if (LOGGING_ON) {
            System.out.println("##" + logtag + "## " + message);
        }
    }

    public static void e(String logtag, String message) {
        if (LOGGING_ON) {
            System.err.println("##" + logtag + "## " + message);
        }
    }
}
