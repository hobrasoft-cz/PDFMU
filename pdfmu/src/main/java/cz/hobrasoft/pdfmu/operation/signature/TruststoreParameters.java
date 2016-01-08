package cz.hobrasoft.pdfmu.operation.signature;

import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.ArgsConfiguration;
import java.io.File;
import java.util.AbstractMap;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import static cz.hobrasoft.pdfmu.error.ErrorType.SSL_TRUSTSTORE_NOT_FOUND;

/**
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
public class TruststoreParameters implements ArgsConfiguration {

    private static final Logger LOGGER = Logger.getLogger(TruststoreParameters.class.getName());

    /**
     * TrustStore path
     */
    public String location;

    /**
     * TrustStore type
     */
    //public String type;
    /**
     * TrustStore password
     */
    //public String password;

    /*private final PasswordArgs passwordArgs = new PasswordArgs("truststore password",
            null,
            "truststore-password",
            "truststore password (default: <none>)",
            null,
            "truststore-password-envvar",
            "truststore password environment variable",
            "PDFMU_TRUSTSTORE_PASSWORD");*/
    @Override
    public void addArguments(ArgumentParser parser) {
        ArgumentGroup group = parser.addArgumentGroup("truststore");
        // TODO: Add description

        group.addArgument("--truststore")
                .help("Location of the keystore file containing the trusted certificates. Always uses forward slashes.")
                .type(String.class);
        /*group.addArgument("--truststore-type")
                .help("truststore type")
                .type(String.class)
                .choices(new String[]{"jceks", "jks", "pkcs12"});*/

        // TODO: Include the password options in `group`
        //passwordArgs.addArguments(parser);
    }

    @Override
    public void setFromNamespace(Namespace namespace) throws OperationException {
        location = namespace.getString("truststore");
        //type = namespace.getString("truststore_type");

        // Set password
        //passwordArgs.setFromNamespace(namespace);
        //password = passwordArgs.getPassword();
        setProperties();
    }

    private void setProperties() throws OperationException {
        if (location != null) {
            File trustStoreFile = new File(location);
            if (!trustStoreFile.exists()) {
                throw new OperationException(SSL_TRUSTSTORE_NOT_FOUND,
                        new AbstractMap.SimpleEntry<String, Object>("trustStore", location));
            }
            // TODO: Check that `location` is forward-slash-separated
            LOGGER.info(String.format("Using TrustStore: %s", location));
            System.setProperty("javax.net.ssl.trustStore", location);
        }

        /*if (type != null) {
            if (location == null) {
                LOGGER.warning("TrustStore: Type has been specified but location has not.");
            }
            // TODO: Warn if `location` extension is inconsistent with `type`
            System.setProperty("javax.net.ssl.trustStoreType", type);
        }

        if (password != null) {
            if (location == null) {
                LOGGER.warning("TrustStore: Password has been specified but location has not.");
            }
            System.setProperty("javax.net.ssl.trustStorePassword", password);
        }*/
    }
}
