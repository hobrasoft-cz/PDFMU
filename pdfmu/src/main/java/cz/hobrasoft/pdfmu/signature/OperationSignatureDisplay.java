package cz.hobrasoft.pdfmu.signature;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import cz.hobrasoft.pdfmu.Console;
import cz.hobrasoft.pdfmu.Operation;
import cz.hobrasoft.pdfmu.OperationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.security.auth.x500.X500Principal;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

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

    private static void display(PdfReader pdfReader) throws OperationException {
        // digitalsignatures20130304.pdf : Code sample 5.1
        AcroFields fields = pdfReader.getAcroFields();
        display(fields);
    }

    private static void display(AcroFields fields) throws OperationException {
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

    private static PdfPKCS7 display(AcroFields fields, String name) throws OperationException {
        // digitalsignatures20130304.pdf : Code sample 5.2
        Console.println(String.format("Signature covers the whole document: %b", fields.signatureCoversWholeDocument(name)));
        Console.println(String.format("Document revision: %d of %d", fields.getRevision(name), fields.getTotalRevisions()));

        PdfPKCS7 pkcs7 = fields.verifySignature(name);
        display(pkcs7);

        return pkcs7;
    }

    private static PdfPKCS7 display(PdfPKCS7 pkcs7) throws OperationException {
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

            Console.indentLess();
        }

        // TODO: Format date
        //Console.println(String.format("  date: %s", pkcs7.getSignDate()));
        X509Certificate cert = pkcs7.getSigningCertificate();

        { // Signing certificate
            Console.indentMore("Signing certificate:");
            showCertInfo(cert);
            Console.indentLess();
        }

        // Various signature properties can be extracted by calling `pkcs7` getters.
        return pkcs7;
    }

    private static void showCertInfo(X509Certificate cert) {
        X500Principal issuer = cert.getIssuerX500Principal();
        X500Principal subject = cert.getSubjectX500Principal();

        showPrincipalInfo(issuer);
        showPrincipalInfo(subject);
    }

    private static void showPrincipalInfo(X500Principal principal) {
        // TODO: Parse using javax.naming.ldap.LdapName
        Console.println(principal.getName());
    }

}
