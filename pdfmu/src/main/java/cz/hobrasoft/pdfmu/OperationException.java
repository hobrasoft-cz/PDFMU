package cz.hobrasoft.pdfmu;

/**
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

    public OperationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
