package cz.hobrasoft.pdfmu.sign;

import cz.hobrasoft.pdfmu.OperationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
class KeystoreParameters {

    public File file = null;
    public String type = null;
    public char[] password = null;

    public void setFromNamespace(Namespace namespace) {
        file = namespace.get("keystore");
        type = namespace.getString("type");
        // Set password
        String passwordString = namespace.getString("keypass");
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
            System.err.println("Keystore type not specified. Using the default type.");
            type = KeyStore.getDefaultType();
        }
    }

    public void fixPassword() {
        if (password == null) {
            System.err.println("Keystore password not set. Using empty password.");
            password = "".toCharArray();
        }
    }

    public KeyStore loadKeystore() throws OperationException {
        if (file == null) {
            throw new OperationException("Keystore not set but is required. Use --keystore option.");
        }
        System.err.println(String.format("Keystore file: %s", file));
        fixType();
        System.err.println(String.format("Keystore type: %s", type));
        // digitalsignatures20130304.pdf : Code sample 2.2
        // Initialize keystore
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(type);
        } catch (KeyStoreException ex) {
            throw new OperationException(String.format("None of the registered security providers supports the keystore type %s.", type), ex);
        }
        System.err.println(String.format("Keystore security provider: %s", ks.getProvider().getName()));
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
