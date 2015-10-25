package cz.hobrasoft.pdfmu;

import cz.hobrasoft.pdfmu.error.PdfmuError;
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

    private final PdfmuError pe;
    private final String regex;
    private final List<String> groupNames;

    public ExceptionMessagePattern(PdfmuError pe, String regex, List<String> groupNames) {
        this.pe = pe;
        this.regex = regex;
        this.groupNames = groupNames;
    }

    public OperationException getOperationException(Exception e) {
        String message = e.getMessage();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(message);
        if (m.matches()) {
            SortedMap<String, String> arguments = PdfmuUtils.getMatcherGroups(m, groupNames);
            SortedMap<String, Object> argumentsObjects = new TreeMap<>();
            argumentsObjects.putAll(arguments);
            return new OperationException(pe, e, argumentsObjects);
        }
        return null;
    }
}
