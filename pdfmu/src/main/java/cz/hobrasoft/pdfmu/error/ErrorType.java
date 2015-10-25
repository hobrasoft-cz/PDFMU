package cz.hobrasoft.pdfmu.error;

import cz.hobrasoft.pdfmu.IntProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.functors.StringValueTransformer;

/**
 * Lists all the types of errors (failing conditions) that can happen in PDFMU.
 * Every error type has a code and a message associated. The codes are loaded
 * from the properties file {@link #errorCodesResourceName} and the messages are
 * loaded from the resource bundle {@link #errorMessagesResourceBundleBaseName}.
 * The messages are templated. The keys in the {@code .properties} files must
 * match the strings returned by {@link ErrorType#toString()} (which returns the
 * enum constant name by default).
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public enum ErrorType {
    PARSER_UNKNOWN,
    PARSER_UNRECOGNIZED_ARGUMENT,
    PARSER_INVALID_CHOICE,
    INPUT_NOT_VALID_PDF,
    INPUT_NOT_FOUND,
    INPUT_CLOSE,
    OUTPUT_NOT_SPECIFIED,
    OUTPUT_EXISTS_FORCE_NOT_SET,
    OUTPUT_OPEN,
    OUTPUT_STAMPER_OPEN,
    OUTPUT_STAMPER_CLOSE,
    OUTPUT_CLOSE,
    SIGNATURE_ADD_KEYSTORE_TYPE_UNSUPPORTED,
    SIGNATURE_ADD_KEYSTORE_FILE_NOT_SPECIFIED,
    SIGNATURE_ADD_KEYSTORE_FILE_OPEN,
    SIGNATURE_ADD_KEYSTORE_LOAD,
    SIGNATURE_ADD_KEYSTORE_FILE_CLOSE,
    SIGNATURE_ADD_KEYSTORE_ALIASES,
    SIGNATURE_ADD_KEYSTORE_EMPTY,
    SIGNATURE_ADD_KEYSTORE_ALIAS_MISSING,
    SIGNATURE_ADD_KEYSTORE_ALIAS_EXCEPTION,
    SIGNATURE_ADD_KEYSTORE_ALIAS_NOT_KEY,
    SIGNATURE_ADD_KEYSTORE_ALIAS_KEY_EXCEPTION,
    SIGNATURE_ADD_KEYSTORE_PRIVATE_KEY,
    SIGNATURE_ADD_KEYSTORE_CERTIFICATE_CHAIN,
    SIGNATURE_ADD_UNSUPPORTED_DIGEST_ALGORITHM,
    SIGNATURE_ADD_FAIL,
    ATTACH_FAIL,
    ATTACH_ATTACHMENT_EQUALS_OUTPUT;

    // Configuration
    /**
     * The default error code. It is used for error types that have no code
     * associated in {@link #errorCodesResourceName}.
     */
    public static final int defaultErrorCode = -1; // Used if no matching code is found in `errorCodes`
    public static final String errorCodesResourceName = "cz/hobrasoft/pdfmu/error/ErrorCodes.properties";
    public static final String errorMessagesResourceBundleBaseName = "cz.hobrasoft.pdfmu.error.ErrorMessages";

    private static final Logger logger = Logger.getLogger(ErrorType.class.getName());

    private static final IntProperties errorCodes = new IntProperties(defaultErrorCode);
    private static ResourceBundle errorMessages = null;

    /**
     * @return true iff each of the constants of this enum is a key in both
     * {@link #errorCodes} and {@link #errorMessages}.
     */
    private static boolean codesAndMessagesAvailable() {
        ErrorType[] enumKeyArray = ErrorType.values();
        List<ErrorType> enumKeyList = Arrays.asList(enumKeyArray);
        Collection<String> enumKeyStrings = CollectionUtils.collect(enumKeyList, StringValueTransformer.stringValueTransformer());

        Set<String> codeKeySet = errorCodes.stringPropertyNames();
        assert errorMessages != null;
        Set<String> messageKeySet = errorMessages.keySet();

        return codeKeySet.containsAll(enumKeyStrings) && messageKeySet.containsAll(enumKeyStrings);
    }

    /**
     * @return true iff the codes stored in {@link #errorCodes} are pairwise
     * different.
     */
    private static boolean codesUnique() {
        Collection<Integer> codes = errorCodes.intPropertyValues();
        Set<Integer> codesUnique = new HashSet<>(codes);
        assert codes.size() >= codesUnique.size();
        return codes.size() == codesUnique.size();
    }

    /**
     * Loads error codes from the properties resource
     * {@link #errorCodesResourceName} and stores them in {@link #errorCodes}.
     */
    private static void loadErrorCodes() {
        ClassLoader classLoader = ErrorType.class.getClassLoader();
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

    /**
     * Loads error messages from the resource bundle
     * {@link #errorMessagesResourceBundleBaseName} and stores them in
     * {@link #errorMessages}.
     */
    private static void loadErrorMessages() {
        try {
            errorMessages = ResourceBundle.getBundle(errorMessagesResourceBundleBaseName);
        } catch (MissingResourceException ex) {
            logger.severe(String.format("Could not load the error messages resource bundle: %s", ex));
        }
    }

    static {
        // Load error codes before OperationException is instantiated
        loadErrorCodes();

        loadErrorMessages();
        assert errorMessages != null;

        // Make sure all a code and a message is available for every enum value
        assert codesAndMessagesAvailable();

        assert codesUnique();
    }

    /**
     * Returns the error code associated with this error type. The code should
     * uniquely identify the error. The default value {@link #defaultErrorCode}
     * is returned in case no code is associated with this error type. The error
     * codes are loaded from {@code ErrorCodes.properties} when the first
     * {@link ErrorType} is instantiated.
     *
     * @return the error code associated with this error type, or -1 if none is
     * associated
     */
    public int getCode() {
        return errorCodes.getIntProperty(toString());
    }

    /**
     * Returns the message pattern associated with this error type. The message
     * patterns are loaded from {@code ErrorMessages.properties} when the first
     * {@link ErrorType} is instantiated.
     *
     * @return the message pattern associated with this error type, or null if
     * none is associated
     */
    public String getMessagePattern() {
        String key = toString();
        assert errorMessages != null;
        if (errorMessages != null && errorMessages.containsKey(key)) {
            return errorMessages.getString(key);
        } else {
            return null;
        }
    }
}
