package cz.hobrasoft.pdfmu;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * Prints possibly indented messages to a logger
 *
 * <p>
 * Indents the messages by an indentation level that is maintained. The
 * indentation level can be changed by calling {@link Console#indentMore} and
 * {@link Console#indentLess}.
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class Console {

    private static int indentLevel = 0;
    private static String prefix = "";
    private static final String indentString = "  ";

    /**
     * This logger is used for printing the messages
     *
     * <p>
     * By default, a logger that <em>only</em> outputs the messages to
     * {@link System#err} is used. The default logger's name is the fully
     * qualified name of this class (that is {@link Console}).
     */
    public static Logger logger = Logger.getLogger(Console.class.getName());

    static {
        assert prefix.length() == indentLevel * indentString.length();

        // Initialize the default logger
        Handler handler = new ConsoleHandler(); // Prints to `System.err`
        handler.setFormatter(new VerbatimFormatter());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false); // Only print using the ConsoleHandler
    }

    /**
     * Prints a message on a separate line
     *
     * <p>
     * The message is indented by {@link Console#indentLevel} level and logged
     * as an info message ({@link Level#INFO}) using {@link Console#logger}.
     *
     * @param message message to print
     */
    public static void println(String message) {
        assert prefix.length() == indentLevel * indentString.length();
        logger.log(Level.INFO, "{0}{1}", new Object[]{prefix, message});
    }

    /**
     * Increments the indentation level
     *
     * <p>
     * If the code between {@link Console#indentMore} and
     * {@link Console#indentLess} may throw a (checked) exception, surround the
     * code with a {@code try}-{@code finally} block like this to maintain
     * consistency:
     * <pre>
     * {@code
     * Console.indentMore();
     * try {
     *  // Do stuff, possibly throwing an exception
     * // Possibly catch the exception
     * } finally {
     *   Console.indentLess();
     * }
     * }
     * </pre>
     */
    public static void indentMore() {
        ++indentLevel;
        updatePrefix();
    }

    /**
     * Print message and increment indentation
     *
     * @param message message to print
     */
    public static void indentMore(String message) {
        println(message);
        indentMore();
    }

    /**
     * Decrements the indentation level
     */
    public static void indentLess() {
        if (indentLevel > 0) {
            --indentLevel;
            updatePrefix();
        }
    }

    private static void updatePrefix() {
        prefix = StringUtils.repeat(indentString, indentLevel);
    }

    // Instancing is discouraged
    private Console() {
    }
}
