/* 
 * Copyright (C) 2016 Hobrasoft s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
 * Contains all the types of errors (failing conditions) that can happen in
 * PDFMU. Every error type has a code and a message template associated. The
 * codes are loaded from the resource {@value #CODES_RESOURCE_NAME} and the
 * messages are loaded from the resource bundle
 * {@value #MESSAGES_RESOURCE_BUNDLE_BASE_NAME}. The keys in both of the
 * resource files must match the strings returned by
 * {@link ErrorType#toString()} (which returns the enum constant name by
 * default).
 *
 * <p>
 * Assertions are in place that ensure that every error type has a code and a
 * message associated, and that the codes are unique. If assertions are disabled
 * and a resource is missing or incompatible, default values are provided.
 *
 * <p>
 * How to add a new error type:
 * <ul>
 * <li>Add an enum constant in {@link ErrorType}
 * <li>Add a corresponding code in <tt>ErrorCodes.properties</tt>
 * <li>Add a corresponding message in <tt>ErrorMessages.properties</tt>
 * </ul>
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public enum ErrorType {
    PARSER_UNKNOWN,
    PARSER_UNRECOGNIZED_COMMAND,
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
    OUTPUT_WRITE,
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
    SIGNATURE_ADD_TSA_UNTRUSTED,
    SIGNATURE_ADD_TSA_UNAUTHORIZED,
    SIGNATURE_ADD_TSA_LOGIN_FAIL,
    SIGNATURE_ADD_TSA_UNREACHABLE,
    SIGNATURE_ADD_TSA_BAD_CERTIFICATE,
    SIGNATURE_ADD_TSA_HANDSHAKE_FAILURE,
    SIGNATURE_ADD_TSA_SSL_HANDSHAKE_EXCEPTION,
    SIGNATURE_ADD_TSA_SSL_FATAL_ALERT,
    SIGNATURE_ADD_TSA_INVALID_URL,
    ATTACH_FAIL,
    ATTACH_ATTACHMENT_EQUALS_OUTPUT,
    SSL_TRUSTSTORE_NOT_FOUND,
    SSL_TRUSTSTORE_INCORRECT_TYPE,
    SSL_TRUSTSTORE_EMPTY,
    SSL_KEYSTORE_NOT_FOUND;

    /**
     * The default error code. It is used for error types that have no code
     * associated.
     *
     * <p>
     * Value: {@value #DEFAULT_ERROR_CODE}
     */
    public static final int DEFAULT_ERROR_CODE = -1;

    /**
     * The name of the resource that contains the error codes. The resource must
     * be a properties file. It is located using
     * {@link ClassLoader#getResourceAsStream(String)} and loaded using
     * {@link IntProperties#load(InputStream)}.
     *
     * <p>
     * Value: {@value #CODES_RESOURCE_NAME}
     */
    public static final String CODES_RESOURCE_NAME = "cz/hobrasoft/pdfmu/error/ErrorCodes.properties";

    /**
     * The base name of the resource bundle that contains the error messages.
     * The resource bundle is loaded using
     * {@link ResourceBundle#getBundle(String)}.
     *
     * <p>
     * Value: {@value #MESSAGES_RESOURCE_BUNDLE_BASE_NAME}
     */
    public static final String MESSAGES_RESOURCE_BUNDLE_BASE_NAME = "cz.hobrasoft.pdfmu.error.ErrorMessages";

    private static final Logger LOGGER = Logger.getLogger(ErrorType.class.getName());

    private static final IntProperties CODES = new IntProperties(DEFAULT_ERROR_CODE);
    private static ResourceBundle messages = null;

    /**
     * @return true iff each of the constants of this enum is a key in both
     * {@link #CODES} and {@link #messages}.
     */
    private static boolean codesAndMessagesAvailable() {
        ErrorType[] enumKeyArray = ErrorType.values();
        List<ErrorType> enumKeyList = Arrays.asList(enumKeyArray);
        Collection<String> enumKeyStrings = CollectionUtils.collect(enumKeyList, StringValueTransformer.stringValueTransformer());

        Set<String> codeKeySet = CODES.stringPropertyNames();
        assert messages != null;
        Set<String> messageKeySet = messages.keySet();

        return codeKeySet.containsAll(enumKeyStrings) && messageKeySet.containsAll(enumKeyStrings);
    }

    /**
     * @return true iff the codes stored in {@link #CODES} are pairwise
     * different.
     */
    private static boolean codesUnique() {
        Collection<Integer> codes = CODES.intPropertyValues();
        Set<Integer> codesUnique = new HashSet<>(codes);
        assert codes.size() >= codesUnique.size();
        return codes.size() == codesUnique.size();
    }

    /**
     * Loads error codes from the properties resource
     * {@link #CODES_RESOURCE_NAME} and stores them in {@link #CODES}.
     */
    private static void loadErrorCodes() {
        ClassLoader classLoader = ErrorType.class.getClassLoader();
        InputStream in = classLoader.getResourceAsStream(CODES_RESOURCE_NAME);
        if (in != null) {
            try {
                CODES.load(in);
            } catch (IOException ex) {
                LOGGER.severe(String.format("Could not load the error codes properties file: %s", ex));
            }
            try {
                in.close();
            } catch (IOException ex) {
                LOGGER.severe(String.format("Could not close the error codes properties file: %s", ex));
            }
        } else {
            LOGGER.severe("Could not open the error codes properties file.");
        }
    }

    /**
     * Loads error messages from the resource bundle
     * {@link #MESSAGES_RESOURCE_BUNDLE_BASE_NAME} and stores them in
     * {@link #messages}.
     */
    private static void loadErrorMessages() {
        try {
            messages = ResourceBundle.getBundle(MESSAGES_RESOURCE_BUNDLE_BASE_NAME);
        } catch (MissingResourceException ex) {
            LOGGER.severe(String.format("Could not load the error messages resource bundle: %s", ex));
        }
    }

    static {
        // Load error codes and messages before OperationException is instantiated
        loadErrorCodes();
        loadErrorMessages();
        assert messages != null;

        // Assert that a code and a message is available for every enum constant
        assert codesAndMessagesAvailable();

        // Assert that the codes are pairwise different
        assert codesUnique();
    }

    /**
     * Returns the error code associated with this error type. The code should
     * uniquely identify the error. The value of {@link #DEFAULT_ERROR_CODE} is
     * returned if no code is associated with this error type. The error codes
     * are loaded from the resource {@value #CODES_RESOURCE_NAME} when the first
     * {@link ErrorType} is instantiated.
     *
     * @return the error code associated with this error type, or
     * {@value #DEFAULT_ERROR_CODE} if none is associated
     */
    public int getCode() {
        return CODES.getIntProperty(toString());
    }

    /**
     * Returns the message pattern associated with this error type. The message
     * patterns are loaded from the resource bundle
     * {@value #MESSAGES_RESOURCE_BUNDLE_BASE_NAME} when the first
     * {@link ErrorType} is instantiated.
     *
     * @return the message pattern associated with this error type, or null if
     * none is associated
     */
    public String getMessagePattern() {
        String key = toString();
        assert messages != null;
        if (messages.containsKey(key)) {
            return messages.getString(key);
        } else {
            return null;
        }
    }
}
