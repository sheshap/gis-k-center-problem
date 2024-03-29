package pl.elka.gis.utils;

/**
 * @author Andrzej Makarewicz
 */
public class Log {

    private static final boolean LOGGABLE = true;

    public static boolean isLoggable() {
        return LOGGABLE;
    }

    public static void d(String logtag, String message) {
        if (LOGGABLE) {
            System.out.println(String.format("%-30s %s", logtag, message));
        }
    }

    public static void d(String message) {
        d(getCallerClassName(3), message);
    }

    public static void e(String logtag, String message) {
        if (LOGGABLE) {
            System.err.println(String.format("%-30s %s", logtag, message));
        }
    }

    public static void e(String message) {
        e(getCallerClassName(3), message);
    }

    public static String getCurrentMethodName() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        return String.format("%s() ", stackTraceElements[2].getMethodName());
    }

    private static String getCallerClassName(int level) {
        return sun.reflect.Reflection.getCallerClass(level).getSimpleName();
    }
}
