package cz.hobrasoft.pdfmu.operation.signature;

import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.ArgsConfiguration;
import cz.hobrasoft.pdfmu.operation.args.PasswordArgs;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Parameters of timestamping process.
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class TimestampParameters implements ArgsConfiguration {

    /**
     * Timestamp authority URL.
     */
    public String url;

    /**
     * Timestamp authority login username.
     */
    public String username;

    /**
     * Timestamp authority login password.
     */
    public String password;

    private final TruststoreParameters truststore = new TruststoreParameters();

    private final PasswordArgs passwordArgs = new PasswordArgs("timestamp authority password",
            null,
            "tsa-password",
            "timestamp authority password (default: <none>)",
            null,
            "tsa-password-envvar",
            "timestamp authority password environment variable",
            "PDFMU_TSA_PASSWORD");

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

        // TODO: Include the password options in `group`
        passwordArgs.addArguments(parser);

        truststore.addArguments(parser);
    }

    @Override
    public void setFromNamespace(Namespace namespace) throws OperationException {
        url = namespace.get("tsa_url");
        username = namespace.getString("tsa_username");

        // Set password
        passwordArgs.setFromNamespace(namespace);
        password = passwordArgs.getPassword();

        truststore.setFromNamespace(namespace);
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
        return new TSAClientBouncyCastle(url, username, password);
    }

}
