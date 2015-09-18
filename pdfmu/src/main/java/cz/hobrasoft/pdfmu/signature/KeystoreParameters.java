package cz.hobrasoft.pdfmu.signature;

import cz.hobrasoft.pdfmu.ArgsConfiguration;
import cz.hobrasoft.pdfmu.Console;
import cz.hobrasoft.pdfmu.OperationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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

    private static final Logger logger = Logger.getLogger(KeystoreParameters.class.getName());

    @Override
    public void addArguments(ArgumentParser parser) {
        // Keystore
        // CLI inspired by `keytool`
        parser.addArgument("-ks", "--keystore")
                .help("keystore file")
                .type(Arguments.fileType().verifyCanRead())
                .required(true);
        // Valid types:
        // https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore
        // Type "pkcs12" file extensions: P12, PFX
        // Source: https://en.wikipedia.org/wiki/PKCS_12
        parser.addArgument("-t", "--type")
                .help("keystore type")
                .type(String.class)
                .choices(new String[]{"jceks", "jks", "dks", "pkcs11", "pkcs12"});
        // TODO?: Guess type from file extension by default
        // TODO?: Hardcode to "pkcs12" since it seems to be required for our purpose
        parser.addArgument("-sp", "--storepass")
                .help("keystore password (default: <empty>)")
                .type(String.class);
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        file = namespace.get("keystore");
        type = namespace.getString("type");

        // Set password
        String passwordString = namespace.getString("storepass");
        if (passwordString == null) {
            // Load the password from an environment variable
            passwordString = System.getenv("PDFMU_STOREPASS");
            if (passwordString != null) {
                Console.println("Keystore password loaded from an environment variable.");
            }
        }
        if (passwordString != null) {
            password = passwordString.toCharArray();
        } else {
            password = null;
        }
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
        if (file == null) {
            throw new OperationException("Keystore not set but is required. Use --keystore option.");
        }
        logger.info(String.format("Keystore file: %s", file));
        fixType();
        logger.info(String.format("Keystore type: %s", type));
        // digitalsignatures20130304.pdf : Code sample 2.2
        // Initialize keystore
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(type);
        } catch (KeyStoreException ex) {
            throw new OperationException(String.format("None of the registered security providers supports the keystore type %s.", type), ex);
        }
        logger.info(String.format("Keystore security provider: %s", ks.getProvider().getName()));
        // Load keystore
        {
            // ksIs
            FileInputStream ksIs;
            try {
                ksIs = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                throw new OperationException("Could not open keystore file.", ex);
            }
            fixPassword();
            try {
                ks.load(ksIs, password);
            } catch (IOException ex) {
                throw new OperationException("Could not load keystore. Incorrect keystore password? Incorrect keystore type? Corrupted keystore file?", ex);
            } catch (NoSuchAlgorithmException | CertificateException ex) {
                throw new OperationException("Could not load keystore.", ex);
            }
            try {
                ksIs.close();
            } catch (IOException ex) {
                throw new OperationException("Could not close keystore file.", ex);
            }
        }
        return ks;
    }

}
