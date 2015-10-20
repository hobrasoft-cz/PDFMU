package cz.hobrasoft.pdfmu.operation;

import cz.hobrasoft.pdfmu.IntProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * This exception is thrown by {@link Operation#execute(Namespace)} to notify
 * {@link cz.hobrasoft.pdfmu.Main#main(String[])} that the operation has
 * encountered a condition it cannot recover from.
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationException extends Exception {

    // Configuration
    private static final int defaultErrorCode = -1;
    private static final String errorCodesResourceName = "cz/hobrasoft/pdfmu/operation/errorCodes.properties";

    private static final Logger logger = Logger.getLogger(OperationException.class.getName());

    private static final IntProperties errorCodes = new IntProperties(defaultErrorCode);

    // Load error codes from a properties resource
    private static void loadErrorCodes() {
        ClassLoader classLoader = OperationException.class.getClassLoader();
        InputStream in = classLoader.getResourceAsStream(errorCodesResourceName);
        if (in != null) {
            try {
                errorCodes.load(in);
            } catch (IOException ex) {
                logger.severe(String.format("Could not load the error codes properties file: %s", ex));
            }
            try {
                in.close();
            } catch (IOException ex) {
                logger.severe(String.format("Could not close the error codes properties file: %s", ex));
            }
        } else {
            logger.severe("Could not open the error codes properties file.");
        }
    }

    static {
        // Load error codes before OperationException is instantiated
        loadErrorCodes();
    }

    /**
     * Constructs an instance of <code>OperationException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public OperationException(String msg) {
        super(msg);
    }

    /**
     * Constructs a chained exception with the specified detail message.
     *
     * @param msg the detail message.
     * @param cause the original cause.
     */
    public OperationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Returns the error code associated with this exception
     *
     * <p>
     * The code should uniquely identify the error that caused the exception.
     *
     * @return the error code associated with this exception
     */
    public int getCode() {
        return errorCodes.getIntProperty(getMessage());
    }
}
