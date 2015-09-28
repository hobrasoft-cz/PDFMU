package cz.hobrasoft.pdfmu.operation.signature;

import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.ArgsConfiguration;
import cz.hobrasoft.pdfmu.operation.args.PasswordArgs;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
class KeyParameters implements ArgsConfiguration {

    public String alias = null;
    public char[] password = null;

    // TODO: Replace with Console
    private static final Logger logger = Logger.getLogger(KeyParameters.class.getName());

    private final PasswordArgs passwordArgs = new PasswordArgs("key password",
            "kp",
            "keypass",
            "key password (default: <empty>)",
            "kpev",
            "keypassenvvar",
            "key password environment variable",
            "PDFMU_KEYPASS");

    @Override
    public void addArguments(ArgumentParser parser) {
        parser.addArgument("-a", "--alias")
                .help("key keystore entry alias (default: <first entry in the keystore>)")
                .type(String.class);
        // TODO?: Hardcode to first entry since most P12 keystores contain only one entry

        passwordArgs.addArguments(parser);
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        alias = namespace.getString("alias");

        // Set password
        passwordArgs.setFromNamespace(namespace);
        password = passwordArgs.getPassword();
        // TODO?: Use keystore password by default
    }

    public void fixAlias(KeyStore ks) throws OperationException {
        if (alias == null) {
            // Get the first alias in the keystore
            logger.info("Keystore entry alias not set. Using the first entry in the keystore.");
            Enumeration<String> aliases;
            try {
                aliases = ks.aliases();
            } catch (KeyStoreException ex) {
                throw new OperationException("Could not get aliases from keystore.", ex);
            }
            if (!aliases.hasMoreElements()) {
                throw new OperationException("Keystore is empty (no aliases).");
            }
            alias = aliases.nextElement();
            assert alias != null;
        }
        logger.info(String.format("Keystore entry alias: %s", alias));
        // Make sure the entry `alias` is present in the keystore
        try {
            if (!ks.containsAlias(alias)) {
                throw new OperationException(String.format("Keystore does not contain the alias %s.", alias));
            }
        } catch (KeyStoreException ex) {
            throw new OperationException(String.format("Could not determine whether the keystore contains the alias %s.", alias), ex);
        }
        // Make sure `alias` is a key entry
        try {
            if (!ks.isKeyEntry(alias)) {
                throw new OperationException(String.format("The keystore entry associated with the alias %s is not a key entry.", alias));
            }
        } catch (KeyStoreException ex) {
            throw new OperationException(String.format("Could not determine whether the keystore entry %s is a key.", alias), ex);
        }
    }

    public void fixPassword(KeyStore ks) {
        switch (ks.getType()) {
            case "Windows-MY":
                if (password != null) {
                    logger.info("The keystore Windows-MY does not accept key password.");
                    password = null;
                }
            default:
                // Set key password to empty string if not set from command line
                if (password == null) {
                    logger.info("Key password not set. Using an empty password.");
                    password = "".toCharArray();
                }
        }
    }

    public void fix(KeyStore ks) throws OperationException {
        fixAlias(ks);
        fixPassword(ks);
    }

    public PrivateKey getPrivateKey(KeyStore ks) throws OperationException {
        // Get private key from keystore
        PrivateKey pk;
        try {
            pk = (PrivateKey) ks.getKey(alias, password);
        } catch (KeyStoreException ex) {
            throw new OperationException("Could not get key from keystore.", ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new OperationException("Could not get key from keystore.", ex);
        } catch (UnrecoverableKeyException ex) {
            throw new OperationException("Could not get key from keystore. Incorrect key password? Incorrect keystore type?", ex);
        }
        if (pk == null) {
            throw new OperationException("Could not get key from keystore. Incorrect alias?");
        }
        return pk;
    }

    public Certificate[] getCertificateChain(KeyStore ks) throws OperationException {
        Certificate[] chain;
        try {
            chain = ks.getCertificateChain(alias);
        } catch (KeyStoreException ex) {
            throw new OperationException("Could not get certificate chain from keystore.", ex);
        }
        return chain;
    }

}
