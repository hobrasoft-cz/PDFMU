package cz.hobrasoft.pdfmu;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Extracts the log record message followed by line break
 *
 * <p>
 * Log using this formatter to output the messages verbatim.
 *
 * <p>
 * Example of usage:
 * <pre>
 * {@code
 * Handler handler = new ConsoleHandler(); // Prints to `System.err`
 * handler.setFormatter(new VerbatimFormatter());
 * String loggerName = ""; // Set `loggerName` to a sensible value. "" refers to the root logger.
 * Logger logger = Logger.getLogger(name);
 * logger.addHandler(handler);
 * }
 * </pre>
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
// http://stackoverflow.com/a/5937929
public class VerbatimFormatter extends SimpleFormatter {

    @Override
    public String format(LogRecord record) {
        return formatMessage(record) + "\n";
    }
}
