package cz.hobrasoft.pdfmu.operation.signature;

import cz.hobrasoft.pdfmu.error.ErrorType;
import cz.hobrasoft.pdfmu.operation.OperationException;
import java.io.File;
import java.util.AbstractMap;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 * SSL keystore (that is TrustStore or private KeyStore) configurator.
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 * @see
 * <a href="https://access.redhat.com/documentation/en-US/Fuse_MQ_Enterprise/7.1/html/Security_Guide/files/SSL-SysProps.html">Configuring
 * JSSE System Properties</a>
 * @see
 * <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyStore">KeyStore
 * Types</a>
 * @see
 * <a href="http://docs.oracle.com/cd/E19509-01/820-3503/6nf1il6er/index.html">Generating
 * a KeyStore and TrustStore</a>
 */
public enum SslKeystore {
    /**
     * The keystore that contains the private keys used for authorization. The
     * respective system property keys start with the prefix
     * {@code javax.net.ssl.keyStore}.
     */
    PRIVATE("javax.net.ssl.keyStore",
            "javax.net.ssl.keyStorePassword",
            "javax.net.ssl.keyStoreType",
            "SSL KeyStore",
            ErrorType.SSL_KEYSTORE_NOT_FOUND),
    /**
     * The keystore that contains the certificates of the trusted certificate
     * authorities. The respective system property keys start with the prefix
     * {@code javax.net.ssl.trustStore}.
     */
    TRUSTSTORE("javax.net.ssl.trustStore",
            "javax.net.ssl.trustStorePassword",
            "javax.net.ssl.trustStoreType",
            "SSL TrustStore",
            ErrorType.SSL_TRUSTSTORE_NOT_FOUND);

    private static final Logger LOGGER = Logger.getLogger(SslKeystore.class.getName());

    private final String keyLocation;
    private final String keyPassword;
    private final String keyType;
    private final String name;
    private final ErrorType errorTypeNotFound;

    private SslKeystore(String location,
            String password,
            String type,
            String name,
            ErrorType errorTypeNotFound) {
        this.keyLocation = location;
        this.keyPassword = password;
        this.keyType = type;
        this.name = name;
        this.errorTypeNotFound = errorTypeNotFound;
    }

    /**
     * @return a short description of this keystore
     */
    public String getName() {
        return name;
    }

    /**
     * Set the system properties that configure this SSL keystore.
     *
     * @param file the keystore file
     * @param type the type of the keystore
     * @param password the password of the keystore
     * @throws OperationException if the keystore file does not exist
     */
    public void setSystemProperties(File file, String type, String password) throws OperationException {
        if (file != null) {
            // https://access.redhat.com/documentation/en-US/Fuse_MQ_Enterprise/7.1/html/Security_Guide/files/SSL-SysProps.html
            // > On Windows, the specified pathname must use forward slashes, /, in place of backslashes, \.
            String location = FilenameUtils.separatorsToUnix(file.getPath());
            if (!file.exists()) {
                throw new OperationException(errorTypeNotFound,
                        new AbstractMap.SimpleEntry<String, Object>("location", location));
            }
            LOGGER.info(String.format("%s: Configuring to use the keystore file %s.", name, location));
            System.setProperty(keyLocation, location);
        }

        if (type != null) {
            if (file == null) {
                LOGGER.warning(String.format("%s: Type has been specified but location has not.", name));
            }
            // TODO: Warn if `file` extension is inconsistent with `type`
            System.setProperty(keyType, type);
        }

        if (password != null) {
            if (file == null) {
                LOGGER.warning(String.format("%s: Password has been specified but location has not.", name));
            }
            System.setProperty(keyPassword, password);
        }
    }
}
