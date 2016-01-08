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

    private PasswordArgs passwordArgs = new PasswordArgs("TSA password");

    private final TruststoreParameters truststore = new TruststoreParameters();


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

        truststore.addArguments(parser);
    }

    @Override
    public void setFromNamespace(Namespace namespace) throws OperationException {
        url = namespace.get("tsa_url");
        username = namespace.getString("tsa_username");

        passwordArgs.setFromNamespace(namespace);

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
        return new TSAClientBouncyCastle(url, username, getPassword());
    }

    private String getPassword() {
        assert passwordArgs != null;
        return passwordArgs.getPassword();
    }

}
