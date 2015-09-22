package cz.hobrasoft.pdfmu.signature;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.CertificateInfo.X500Name;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import cz.hobrasoft.pdfmu.Console;
import cz.hobrasoft.pdfmu.Operation;
import cz.hobrasoft.pdfmu.OperationException;
import cz.hobrasoft.pdfmu.PreferenceListComparator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.security.auth.x500.X500Principal;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.apache.commons.lang3.StringUtils;

/**
 * Displays signatures of a PDF document
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationSignatureDisplay implements Operation {

    @Override
    public String getCommandName() {
        return "display";
    }

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Display signatures of a PDF document";

        String metavarIn = "IN.pdf";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", this.getClass());

        // Add arguments to the subparser
        // Positional arguments are required by default
        subparser.addArgument("in")
                .help("input PDF document")
                .metavar(metavarIn)
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead())
                .required(true);

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        // Input file
        File inFile = namespace.get("in");
        assert inFile != null; // Required argument

        display(inFile);
    }

    private static void display(File inFile) throws OperationException {
        assert inFile != null;

        Console.println(String.format("Input PDF document: %s", inFile));

        // Open the input stream
        FileInputStream inStream;
        try {
            inStream = new FileInputStream(inFile);
        } catch (FileNotFoundException ex) {
            throw new OperationException("Input file not found.", ex);
        }

        display(inStream);

        // Close the input stream
        try {
            inStream.close();
        } catch (IOException ex) {
            throw new OperationException("Could not close the input file.", ex);
        }
    }

    private static void display(InputStream inStream) throws OperationException {
        // Open the PDF reader
        // PdfReader parses a PDF document.
        PdfReader pdfReader;
        try {
            pdfReader = new PdfReader(inStream);
        } catch (IOException ex) {
            throw new OperationException("Could not open the input PDF document.", ex);
        }

        display(pdfReader);

        // Close the PDF reader
        pdfReader.close();
    }

    private static void display(PdfReader pdfReader) {
        // digitalsignatures20130304.pdf : Code sample 5.1
        AcroFields fields = pdfReader.getAcroFields();
        display(fields);
    }

    private static void display(AcroFields fields) {
        // digitalsignatures20130304.pdf : Code sample 5.1
        ArrayList<String> names = fields.getSignatureNames();

        // Print number of signatures
        Console.println(String.format("Number of signatures: %d", names.size()));
        Console.println(String.format("Number of document revisions: %d", fields.getTotalRevisions()));

        if (names.size() > 0) {
            Console.println(""); // Precede the first signature with an empty line
        }

        for (String name : names) {
            Console.println(String.format("Signature field name: %s", name));

            Console.indentMore();
            try {
                display(fields, name); // May throw OperationException
            } finally {
                Console.indentLess();
                Console.println(""); // Follow each signature with an empty line
            }
        }
    }

    private static void display(AcroFields fields, String name) {
        // digitalsignatures20130304.pdf : Code sample 5.2
        Console.println(String.format("Signature covers the whole document: %s", (fields.signatureCoversWholeDocument(name) ? "Yes" : "No")));
        Console.println(String.format("Document revision: %d of %d", fields.getRevision(name), fields.getTotalRevisions()));

        PdfPKCS7 pkcs7 = fields.verifySignature(name);
        display(pkcs7);
    }

    private static void display(PdfPKCS7 pkcs7) {
        // digitalsignatures20130304.pdf : Code sample 5.3
        Console.println("Signature metadata:");
        {
            Console.indentMore();

            // Only name may be null.
            // The values are set in {@link PdfPKCS7#verifySignature}.
            { // name
                String name = pkcs7.getSignName(); // May be null
                if (name == null) {
                    name = "N/A";
                }
                Console.println(String.format("Name: %s", name));
            }

            // TODO?: Print "N/A" if the value is an empty string
            // TODO?: Determine whether the value is set in the signature
            Console.println(String.format("Reason: %s", pkcs7.getReason()));
            Console.println(String.format("Location: %s", pkcs7.getLocation()));

            { // Date
                Date date = pkcs7.getSignDate().getTime();
                Console.println(String.format("Date and time: %s", date));
            }

            Console.indentLess();
        }
        { // Certificate chain
            Console.indentMore("Certificate chain:");
            Certificate[] certificates = pkcs7.getSignCertificateChain();
            Console.println(String.format("Number of certificates: %d", certificates.length));
            int i = 0;
            for (Certificate certificate : certificates) {
                Console.indentMore(String.format("Certificate %d%s:", i, (i == 0 ? " (signing certificate)" : "")));
                String type = certificate.getType();
                Console.println(String.format("Type: %s", type));
                // http://docs.oracle.com/javase/1.5.0/docs/guide/security/CryptoSpec.html#AppA
                if ("X.509".equals(type)) {
                    X509Certificate certificateX509 = (X509Certificate) certificate;
                    showCertInfo(certificateX509);
                }
                Console.indentLess();
                ++i;
            }
            Console.indentLess();
        }
    }

    private static void showCertInfo(X509Certificate cert) {
        { // Self-signed?
            X500Principal principalSubject = cert.getSubjectX500Principal();
            X500Principal principalIssuer = cert.getIssuerX500Principal();
            boolean selfSigned = principalSubject.equals(principalIssuer);
            Console.println(String.format("Self-signed: %s", (selfSigned ? "Yes" : "No")));
        }

        // Note: More attributes may be available by more direct processing of `cert`
        // than by using `CertificateInfo.get*Fields`.
        { // Subject
            Console.indentMore("Subject:");
            showX500Name(CertificateInfo.getSubjectFields(cert));
            Console.indentLess();
        }
        { // Issuer
            Console.indentMore("Issuer:");
            showX500Name(CertificateInfo.getIssuerFields(cert));
            Console.indentLess();
        }
    }

    // The desired order of DN attributes by their type
    private static final List<String> types = new ArrayList<>();

    static {
        types.add("CN");
        types.add("E");
        types.add("OU");
        types.add("O");
        types.add("STREET");
        types.add("L");
        types.add("ST");
        types.add("C");
    }

    private static void showX500Name(X500Name name) {
        Map<String, ArrayList<String>> fields = name.getFields();

        SortedMap<String, ArrayList<String>> fieldsSorted = new TreeMap<>(new PreferenceListComparator(types));
        fieldsSorted.putAll(fields);

        for (Entry<String, ArrayList<String>> field : fieldsSorted.entrySet()) {
            String type = field.getKey();
            type = niceX500AttributeType(type);
            ArrayList<String> values = field.getValue();
            String valuesString = StringUtils.join(values, ", ");
            Console.println(String.format("%s: %s", type, valuesString));
        }
    }

    private static final Map<String, String> attributeTypeAliases = new HashMap<>();

    static {
        // Alias sources:
        // http://www.ietf.org/rfc/rfc2253.txt : Section 2.3
        // http://api.itextpdf.com/itext/com/itextpdf/text/pdf/security/CertificateInfo.X500Name.html
        attributeTypeAliases.put("CN", "Common name");
        attributeTypeAliases.put("L", "Locality");
        attributeTypeAliases.put("ST", "State or province");
        attributeTypeAliases.put("O", "Organization");
        attributeTypeAliases.put("OU", "Organizational unit");
        attributeTypeAliases.put("C", "Country code");
        attributeTypeAliases.put("STREET", "Street address");
        attributeTypeAliases.put("DC", "Domain component");
        attributeTypeAliases.put("UID", "User ID");
        attributeTypeAliases.put("E", "Email address");
        attributeTypeAliases.put("SN", "Serial number");
        attributeTypeAliases.put("T", "Title");
    }

    private static String niceX500AttributeType(String type) {
        String nice = attributeTypeAliases.get(type);
        if (nice != null) {
            type = nice;
        } else {
            return String.format("<%s>", type);
        }

        return type;
    }

}
