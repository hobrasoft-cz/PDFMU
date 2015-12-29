package cz.hobrasoft.pdfmu;

import cz.hobrasoft.pdfmu.error.ErrorType;
import cz.hobrasoft.pdfmu.operation.OperationException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class ExceptionMessagePattern {

    private final ErrorType errorType;
    private final String regex;
    private final List<String> groupNames;

    public ExceptionMessagePattern(ErrorType errorType, String regex, List<String> groupNames) {
        this.errorType = errorType;
        this.regex = regex;
        this.groupNames = groupNames;
    }

    /**
     * Tries to convert an exception to an {@link OperationException}.
     *
     * @param e the exception to parse
     * @return an instance of {@link OperationException} if the pattern matches
     * the message of e, or null otherwise.
     */
    public OperationException getOperationException(Exception e) {
        String message = e.getMessage();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(message);
        if (m.matches()) {
            SortedMap<String, String> arguments = PdfmuUtils.getMatcherGroups(m, groupNames);
            SortedMap<String, Object> argumentsObjects = new TreeMap<>();
            argumentsObjects.putAll(arguments);
            return new OperationException(errorType, e, argumentsObjects);
        }
        return null;
    }
}
