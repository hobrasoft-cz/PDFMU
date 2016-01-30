package cz.hobrasoft.pdfmu.operation.signature;

import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.ArgsConfiguration;
import cz.hobrasoft.pdfmu.operation.args.PasswordArgs;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Parameters of timestamping process.
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class TimestampParameters implements ArgsConfiguration {

    private static final Logger LOGGER = Logger.getLogger(TimestampParameters.class.getName());

    /**
     * Timestamp authority URL.
     */
    public String url;

    /**
     * Timestamp authority login username.
     */
    public String username;

    private PasswordArgs passwordArgs = new PasswordArgs("TSA password");

    private final KeystoreParameters sslKeystore = new KeystoreParameters(SslKeystore.PRIVATE.getName());
    private final KeystoreParameters sslTruststore = new KeystoreParameters(SslKeystore.TRUSTSTORE.getName());

    @Override
    public void addArguments(ArgumentParser parser) {
        ArgumentGroup group = parser.addArgumentGroup("timestamp");
        // TODO: Add description

        group.addArgument("--tsa-url")
                .help("timestamp authority URL (set to enable timestamp)")
                .type(String.class);

        group.addArgument("--tsa-username")
                .help("timestamp authority username (set to enable TSA authorization)")
                .type(String.class);

        passwordArgs.passwordArgument = group.addArgument("--tsa-password")
                .help("timestamp authority password (default: <none>)");
        passwordArgs.environmentVariableArgument = group.addArgument("--tsa-password-envvar")
                .help("timestamp authority password environment variable")
                .setDefault("PDFMU_TSA_PASSWORD");
        passwordArgs.finalizeArguments();

        sslKeystore.fileArgument = group.addArgument("--ssl-keystore")
                .help("The keystore file that contains the private keys used for SSL authorization");
        sslKeystore.typeArgument = group.addArgument("--ssl-keystore-type")
                .help("SSL KeyStore type")
                .choices(new String[]{"jks", "jceks", "pkcs12"});
        sslKeystore.passwordArgs.passwordArgument = group.addArgument("--ssl-keystore-password")
                .help("SSL KeyStore password (default: <none>)");
        sslKeystore.passwordArgs.environmentVariableArgument = group.addArgument("--ssl-keystore-password-envvar")
                .help("SSL KeyStore password environment variable")
                .setDefault("PDFMU_SSL_KEYSTORE_PASSWORD");
        sslKeystore.finalizeArguments();

        sslTruststore.fileArgument = group.addArgument("--ssl-truststore")
                .help("The keystore file that contains the certificates of the trusted certificate authorities");
        // TODO: Add support for type "pkcs12"
        sslTruststore.typeArgument = group.addArgument("--ssl-truststore-type")
                .help("SSL TrustStore type")
                .choices(new String[]{"jks", "jceks"});
        sslTruststore.passwordArgs.passwordArgument = group.addArgument("--ssl-truststore-password")
                .help("SSL TrustStore password (default: <none>)");
        sslTruststore.passwordArgs.environmentVariableArgument = group.addArgument("--ssl-truststore-password-envvar")
                .help("SSL TrustStore password environment variable")
                .setDefault("PDFMU_SSL_TRUSTSTORE_PASSWORD");
        sslTruststore.finalizeArguments();
    }

    @Override
    public void setFromNamespace(Namespace namespace) throws OperationException {
        url = namespace.get("tsa_url");
        username = namespace.getString("tsa_username");

        passwordArgs.setFromNamespace(namespace);

        sslKeystore.setFromNamespace(namespace);
        if (sslKeystore.file != null) {
            String password = sslKeystore.getPassword();
            if (password == null || password.isEmpty()) {
                LOGGER.warning("SSL KeyStore: Location has been set but password has not. Only KeyStores protected by a non-empty password are supported.");
            }
        }
        sslKeystore.setSystemProperties(SslKeystore.PRIVATE);

        sslTruststore.setFromNamespace(namespace);
        sslTruststore.setSystemProperties(SslKeystore.TRUSTSTORE);
    }

    /**
     * Returns the {@link TSAClient} that corresponds to these parameters.
     *
     * @return null if the timestamp authority has not been configured
     */
    public TSAClient getTSAClient() {
        if (url == null) {
            return null;
        }
        LOGGER.info("TSA URL has been set. Will attempt to attach a timestamp to the signature.");
        String password = getPassword();
        if (username != null && password == null) {
            LOGGER.warning("TSA username has been set but password has not.");
        }
        if (password != null && username == null) {
            LOGGER.warning("TSA password has been set but username has not.");
        }
        return new TSAClientBouncyCastle(url, username, password);
    }

    private String getPassword() {
        assert passwordArgs != null;
        return passwordArgs.getPassword();
    }

}
