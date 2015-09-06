package cz.hobrasoft.pdfmu;

/**
 * This exception is thrown by {@link Operation#execute(Namespace)} to notify
 * {@link Main#main(String[])} that the operation has encountered a condition it
 * cannot recover from.
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationException extends Exception {

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
}
