package cz.hobrasoft.pdfmu;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.functors.StringValueTransformer;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public enum PdfmuError {
    PARSER_UNKNOWN,
    PARSER_UNRECOGNIZED_ARGUMENTS,
    PARSER_INVALID_CHOICE,
    INPUT_NOT_VALID_PDF,
    INPUT_NOT_FOUND,
    INPUT_CLOSE,
    ATTACH_FAIL;

    // Configuration
    private static final int defaultErrorCode = -1;
    private static final String errorCodesResourceName = "cz/hobrasoft/pdfmu/errorCodes.properties";
    private static final String errorMessagesResourceBundleBaseName = "cz.hobrasoft.pdfmu.ErrorMessages";

    private static final Logger logger = Logger.getLogger(PdfmuError.class.getName());

    private static final IntProperties errorCodes = new IntProperties(defaultErrorCode);
    private static final ResourceBundle errorMessages = ResourceBundle.getBundle(errorMessagesResourceBundleBaseName);

    /**
     * Returns true iff each of the values of this enum is a key in both
     * {@link #errorCodes} and {@link #errorMessages}.
     */
    private static boolean codesAndMessagesAvailable() {
        PdfmuError[] enumKeyArray = PdfmuError.values();
        List<PdfmuError> enumKeyList = Arrays.asList(enumKeyArray);
        Collection<String> enumKeyStrings = CollectionUtils.collect(enumKeyList, StringValueTransformer.stringValueTransformer());

        Set<String> codeKeySet = errorCodes.stringPropertyNames();
        Set<String> messageKeySet = errorMessages.keySet();

        return codeKeySet.containsAll(enumKeyStrings) && messageKeySet.containsAll(enumKeyStrings);
    }

    /**
     * @return true iff the codes stored in `errorCodes` are pairwise different.
     */
    private static boolean codesUnique() {
        Collection<Integer> codes = errorCodes.intPropertyValues();
        Set<Integer> codesUnique = new HashSet<>(codes);
        assert codes.size() >= codesUnique.size();
        return codes.size() == codesUnique.size();
    }

    // Load error codes from a properties resource
    private static void loadErrorCodes() {
        ClassLoader classLoader = PdfmuError.class.getClassLoader();
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

        // Make sure all a code and a message is available for every enum value
        assert codesAndMessagesAvailable();

        assert codesUnique();
    }

    /**
     * Returns the error code associated with this error. The code should
     * uniquely identify the error.
     *
     * @return the error code associated with this error.
     */
    public int getCode() {
        return errorCodes.getIntProperty(toString());
    }

    /**
     * Returns the message pattern associated with this error.
     *
     * @return the message pattern associated with this error.
     */
    public String getMessagePattern() {
        String key = toString();
        if (errorMessages.containsKey(key)) {
            return errorMessages.getString(key);
        } else {
            return null;
        }
    }
}
