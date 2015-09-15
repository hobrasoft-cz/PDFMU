package cz.hobrasoft.pdfmu;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * Prints possibly indented messages to {@link System#err}
 *
 * <p>
 * Maintains indentation level.
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class Console {

    private static int indent = 0;
    private static String prefix = "";
    public static Logger logger = Logger.getLogger(Console.class.getName());

    static {
        assert prefix.length() == indent;

        // Initialize the default logger
        Handler handler = new ConsoleHandler(); // Prints to `System.err`
        handler.setFormatter(new VerbatimFormatter());
        logger.addHandler(handler);
        logger.setUseParentHandlers(false); // Only print using the ConsoleHandler
    }

    public static void println(String message) {
        assert prefix.length() == indent * "  ".length();
        logger.log(Level.INFO, "{0}{1}", new Object[]{prefix, message});
    }

    public static void indentMore() {
        ++indent;
        updatePrefix();
    }

    public static void indentLess() {
        if (indent > 0) {
            --indent;
            updatePrefix();
        }
    }

    private static void updatePrefix() {
        prefix = StringUtils.repeat("  ", indent);
    }
}
