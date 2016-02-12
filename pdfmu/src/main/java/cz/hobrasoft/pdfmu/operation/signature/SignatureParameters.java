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

import com.itextpdf.text.pdf.security.MakeSignature;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.ArgsConfiguration;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
class SignatureParameters implements ArgsConfiguration {

    public SignatureAppearanceParameters appearance = new SignatureAppearanceParameters();
    public KeystoreParameters keystore = new KeystoreParameters("signing keystore");
    public KeyParameters key = new KeyParameters();
    public TimestampParameters timestamp = new TimestampParameters();

    private final ArgsConfiguration[] configurations = {appearance, keystore, key, timestamp};

    // digitalsignatures20130304.pdf : Code sample 2.19; Section 2.1.4; Code sample 2.2
    // Note: KDirSign uses SHA-512.
    public String digestAlgorithm = "SHA256";
    public MakeSignature.CryptoStandard sigtype = MakeSignature.CryptoStandard.CMS;

    private static final String[] digestAlgorithmChoices = {
        // Source: {@link DigestAlgorithms#digestNames}
        // Alternative source:
        // digitalsignatures20130304.pdf : Section 1.2.2; Code sample 1.5
        // TODO?: Add dashes in the algorithm names
        "MD2",
        "MD5",
        "SHA1",
        "SHA224",
        "SHA256",
        "SHA384",
        "SHA512",
        "RIPEMD128",
        "RIPEMD160",
        "RIPEMD256",
        "GOST3411"
    };

    @Override
    public void addArguments(ArgumentParser parser) {
        // CLI inspired by `keytool`
        keystore.fileArgument = parser.addArgument("-s", "--keystore")
                .help("keystore file that contains the signing private key");
        // Valid types:
        // https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyStore
        // Type "pkcs12" file extensions: P12, PFX
        // Source: https://en.wikipedia.org/wiki/PKCS_12
        // Another type: "Windows-MY" - Windows Certificate Store
        // TODO?: Guess type from file extension by default
        // TODO?: Default to "pkcs12"
        // TODO: Do not allow "Windows-MY" when running in a different OS than Windows
        keystore.typeArgument = parser.addArgument("-t", "--storetype")
                .help("type of the signing keystore")
                .choices(new String[]{"jceks", "jks", "pkcs12", "Windows-MY"});
        keystore.passwordArgs.passwordArgument = parser.addArgument("--storepass")
                .help("signing keystore password (default: <empty>)");
        keystore.passwordArgs.environmentVariableArgument = parser.addArgument("--storepass-envvar")
                .help("signing keystore password environment variable")
                .setDefault("PDFMU_STOREPASS");

        for (ArgsConfiguration configuration : configurations) {
            configuration.addArguments(parser);
        }

        // Possible names:
        // - digest algorithm
        // - Hash Algorithm
        // - hash algorithm for making the signature
        parser.addArgument("--digest-algorithm")
                .help("hash algorithm for making the signature")
                // Java 8 (using `String.join`):
                //.metavar(String.format("{%s}", String.join(",", digestAlgorithmChoices)))
                // Java 7 (using `org.apache.commons.lang3.StringUtils.join`):
                .metavar(String.format("{%s}", StringUtils.join(digestAlgorithmChoices, ",")))
                // TODO?: Limit the choices to `digesetAlgorithmChoices`
                .type(String.class)
                .setDefault(digestAlgorithm);

        parser.addArgument("--format")
                .help("signature format (CMS: adbe.pkcs7.detached, CADES: ETSI.CAdES.detached)")
                .type(MakeSignature.CryptoStandard.class)
                .choices(MakeSignature.CryptoStandard.values())
                .setDefault(sigtype);
    }

    @Override
    public void setFromNamespace(Namespace namespace) throws OperationException {
        for (ArgsConfiguration configuration : configurations) {
            configuration.setFromNamespace(namespace);
        }

        sigtype = namespace.get("sigtype");
        digestAlgorithm = namespace.getString("digest_algorithm");
    }

}
