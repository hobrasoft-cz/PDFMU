package cz.hobrasoft.pdfmu.operation;

import cz.hobrasoft.pdfmu.PdfmuError;
import java.util.SortedMap;
import org.apache.commons.lang3.text.StrSubstitutor;

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

    // TODO: Remove
    /**
     * Constructs an instance of <code>OperationException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public OperationException(String msg) {
        super(msg);
    }

    // TODO: Remove
    /**
     * Constructs a chained exception with the specified detail message.
     *
     * @param msg the detail message.
     * @param cause the original cause.
     */
    public OperationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    private PdfmuError e = null;
    private SortedMap<String, Object> messageArguments = null;

    public OperationException(PdfmuError e, Throwable cause) {
        this(e, cause, null);
    }

    /**
     * Creates a chained operation exception with error identifier and message
     * arguments.
     *
     * @param e the error identifier.
     * @param cause the original cause.
     * @param messageArguments the arguments of the message.
     */
    public OperationException(PdfmuError e, Throwable cause, SortedMap<String, Object> messageArguments) {
        super(e.toString(), cause);
        assert e != null;
        this.e = e;
        this.messageArguments = messageArguments;
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
        if (e != null) {
            return e.getCode();
        } else {
            return defaultErrorCode;
        }
    }

    @Override
    public String getLocalizedMessage() {
        if (e != null) {
            String pattern = e.getMessagePattern();
            if (pattern != null) {
                StrSubstitutor sub = new StrSubstitutor(messageArguments);
                return sub.replace(pattern);
            } else {
                return super.getLocalizedMessage();
            }
        } else {
            return super.getLocalizedMessage();
        }
    }
}
