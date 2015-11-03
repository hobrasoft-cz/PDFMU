package cz.hobrasoft.pdfmu.operation.signature;

import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_FILE_CLOSE;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_FILE_NOT_SPECIFIED;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_FILE_OPEN;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_LOAD;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_TYPE_UNSUPPORTED;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.ArgsConfiguration;
import cz.hobrasoft.pdfmu.operation.args.PasswordArgs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
class KeystoreParameters implements ArgsConfiguration {

    public File file = null;
    public String type = null;
    public char[] password = null;

    // TODO: Replace with Console
    private static final Logger logger = Logger.getLogger(KeystoreParameters.class.getName());

    private final PasswordArgs passwordArgs = new PasswordArgs("keystore password",
            null,
            "storepass",
            "keystore password (default: <empty>)",
            null,
            "storepass-envvar",
            "keystore password environment variable",
            "PDFMU_STOREPASS");

    @Override
    public void addArguments(ArgumentParser parser) {
        // Valid types:
        // https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore
        // Type "pkcs12" file extensions: P12, PFX
        // Source: https://en.wikipedia.org/wiki/PKCS_12
        // Another type: "Windows-MY" - Windows Certificate Store
        parser.addArgument("-t", "--storetype")
                .help("keystore type")
                .type(String.class)
                .choices(new String[]{"jceks", "jks", "dks", "pkcs11", "pkcs12", "Windows-MY"});
        // TODO?: Guess type from file extension by default
        // TODO?: Default to "pkcs12"
        // TODO: Do not allow "Windows-MY" when running in a different OS than Windows

        // Keystore
        // CLI inspired by `keytool`
        parser.addArgument("-ks", "--keystore")
                .help("keystore file")
                .type(Arguments.fileType());

        passwordArgs.addArguments(parser);
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        file = namespace.get("keystore");
        type = namespace.getString("storetype");

        // Set password
        passwordArgs.setFromNamespace(namespace);
        password = passwordArgs.getPassword();
    }

    public void fixType() {
        // Set keystore type if not set from command line
        if (type == null) {
            // TODO: Guess type from `ksFile` file extension
            logger.info("Keystore type not specified. Using the default type.");
            type = KeyStore.getDefaultType();
        }
    }

    public void fixPassword() {
        if (password == null) {
            logger.info("Keystore password not set. Using empty password.");
            password = "".toCharArray();
        }
    }

    public KeyStore loadKeystore() throws OperationException {
        fixType();
        logger.info(String.format("Keystore type: %s", type));
        // digitalsignatures20130304.pdf : Code sample 2.2
        // Initialize keystore
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(type);
        } catch (KeyStoreException ex) {
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_TYPE_UNSUPPORTED, ex,
                    new SimpleEntry<String, Object>("type", type));
        }
        logger.info(String.format("Keystore security provider: %s", ks.getProvider().getName()));
        switch (type) {
            case "Windows-MY":
                loadWindowsKeystore(ks);
                break;
            default:
                loadFileKeystore(ks);
        }
        return ks;
    }

    private void loadFileKeystore(KeyStore ks) throws OperationException {
        if (file == null) {
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_FILE_NOT_SPECIFIED,
                    new SimpleEntry<String, Object>("type", type));
        }
        logger.info(String.format("Keystore file: %s", file));
        // ksIs
        FileInputStream ksIs;
        try {
            ksIs = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_FILE_OPEN, ex,
                    new SimpleEntry<String, Object>("file", file));
        }
        fixPassword();
        try {
            ks.load(ksIs, password);
        } catch (IOException ex) {
            // Incorrect keystore password? Incorrect keystore type? Corrupted keystore file?
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_LOAD, ex,
                    new SimpleEntry<String, Object>("type", type),
                    new SimpleEntry<String, Object>("file", file));
        } catch (NoSuchAlgorithmException | CertificateException ex) {
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_LOAD, ex,
                    new SimpleEntry<String, Object>("type", type),
                    new SimpleEntry<String, Object>("file", file));
        }
        try {
            ksIs.close();
        } catch (IOException ex) {
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_FILE_CLOSE, ex,
                    new SimpleEntry<String, Object>("file", file));
        }
    }

    private void loadWindowsKeystore(KeyStore ks) throws OperationException {
        assert "Windows-MY".equals(type);
        try {
            ks.load(null, null);
        } catch (IOException | NoSuchAlgorithmException | CertificateException ex) {
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_LOAD, ex,
                    new SimpleEntry<String, Object>("type", type));
        }
    }

}
