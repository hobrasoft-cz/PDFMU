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
package cz.hobrasoft.pdfmu.operation.signature;

import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_ALIASES;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_ALIAS_EXCEPTION;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_ALIAS_KEY_EXCEPTION;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_ALIAS_MISSING;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_ALIAS_NOT_KEY;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_CERTIFICATE_CHAIN;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_EMPTY;
import static cz.hobrasoft.pdfmu.error.ErrorType.SIGNATURE_ADD_KEYSTORE_PRIVATE_KEY;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.ArgsConfiguration;
import cz.hobrasoft.pdfmu.operation.args.PasswordArgs;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.AbstractMap.SimpleEntry;
import java.util.Enumeration;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.inf.Argument;
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

    public PasswordArgs passwordArgs = new PasswordArgs("key password");

    private Argument keyAliasArgument;

    @Override
    public void addArguments(ArgumentParser parser) {
        keyAliasArgument = parser.addArgument("--key-alias")
                .help("key keystore entry alias (default: <first entry in the keystore>)")
                .type(String.class);

        passwordArgs.passwordArgument = parser.addArgument("--key-password")
                .help("key password (default: <empty>)");
        passwordArgs.environmentVariableArgument = parser.addArgument("--key-password-envvar")
                .help("key password environment variable")
                .setDefault("PDFMU_KEYPASS");
        passwordArgs.finalizeArguments();
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        assert keyAliasArgument != null;
        alias = namespace.getString(keyAliasArgument.getDest());

        // Set password
        assert passwordArgs != null;
        passwordArgs.setFromNamespace(namespace);
        password = passwordArgs.getPasswordCharArray();
        // TODO?: Use keystore password by default
    }

    /**
     * Encodes the alias using ISO-8859-1 and replaces it with the longest
     * prefix of itself that is an actual alias in the keystore. The shortening
     * is a necessary magic necessary for strings with some non-ASCII
     * characters.
     */
    private void fixAliasWcs(KeyStore ks) throws OperationException {
        assert "Windows-MY".equals(ks.getType());
        if (alias != null) {
            logger.info(String.format("WCS alias correction will be applied. Original alias: %s", alias));
            alias = new String(alias.getBytes(), StandardCharsets.ISO_8859_1);

            Enumeration<String> aliases = null;
            try {
                aliases = ks.aliases();
            } catch (KeyStoreException ex) {
                throw new OperationException(SIGNATURE_ADD_KEYSTORE_ALIASES, ex);
            }
            String aliasBest = null;
            while (aliases.hasMoreElements()) {
                String aliasCandidate = aliases.nextElement();
                if (alias.startsWith(aliasCandidate)
                        && (aliasBest == null
                        || aliasCandidate.length() > aliasBest.length())) {
                    aliasBest = aliasCandidate;
                }
            }
            if (aliasBest != null) {
                alias = aliasBest;
            }
        }
    }

    public void fixAlias(KeyStore ks) throws OperationException {
        if (alias == null) {
            // Get the first alias in the keystore
            logger.info("Keystore entry alias not set. Using the first entry in the keystore.");
            Enumeration<String> aliases;
            try {
                aliases = ks.aliases();
            } catch (KeyStoreException ex) {
                throw new OperationException(SIGNATURE_ADD_KEYSTORE_ALIASES, ex);
            }
            if (!aliases.hasMoreElements()) {
                throw new OperationException(SIGNATURE_ADD_KEYSTORE_EMPTY);
            }
            alias = aliases.nextElement();
            assert alias != null;
        } else if ("Windows-MY".equals(ks.getType())) {
            fixAliasWcs(ks);
        }
        logger.info(String.format("Keystore entry alias: %s", alias));

        // Make sure the entry `alias` is present in the keystore
        try {
            if (!ks.containsAlias(alias)) {
                throw new OperationException(SIGNATURE_ADD_KEYSTORE_ALIAS_MISSING,
                        new SimpleEntry<String, Object>("alias", alias));
            }
        } catch (KeyStoreException ex) {
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_ALIAS_EXCEPTION, ex,
                    new SimpleEntry<String, Object>("alias", alias));
        }

        // Make sure `alias` is a key entry
        try {
            if (!ks.isKeyEntry(alias)) {
                throw new OperationException(SIGNATURE_ADD_KEYSTORE_ALIAS_NOT_KEY,
                        new SimpleEntry<String, Object>("alias", alias));
            }
        } catch (KeyStoreException ex) {
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_ALIAS_KEY_EXCEPTION, ex,
                    new SimpleEntry<String, Object>("alias", alias));
        }
    }

    public void fixPassword(KeyStore ks) {
        switch (ks.getType()) {
            case "Windows-MY":
                if (password != null) {
                    logger.info("The keystore Windows-MY does not accept key password.");
                    password = null;
                }
                break;
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
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_PRIVATE_KEY, ex,
                    new SimpleEntry<String, Object>("alias", alias));
        } catch (NoSuchAlgorithmException ex) {
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_PRIVATE_KEY, ex,
                    new SimpleEntry<String, Object>("alias", alias));
        } catch (UnrecoverableKeyException ex) {
            // Incorrect key password? Incorrect keystore type?
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_PRIVATE_KEY, ex,
                    new SimpleEntry<String, Object>("alias", alias));
        }
        if (pk == null) {
            // Incorrect alias?
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_PRIVATE_KEY,
                    new SimpleEntry<String, Object>("alias", alias));
        }
        return pk;
    }

    public Certificate[] getCertificateChain(KeyStore ks) throws OperationException {
        Certificate[] chain;
        try {
            chain = ks.getCertificateChain(alias);
        } catch (KeyStoreException ex) {
            throw new OperationException(SIGNATURE_ADD_KEYSTORE_CERTIFICATE_CHAIN, ex,
                    new SimpleEntry<String, Object>("alias", alias));
        }
        return chain;
    }

}
