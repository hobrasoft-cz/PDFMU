package cz.hobrasoft.pdfmu.sign;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;
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
import java.util.logging.Logger;
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

    private static final Logger logger = Logger.getLogger(OperationSignatureDisplay.class.getName());

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

        logger.info(String.format("Input PDF document: %s", inFile));

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
        logger.info(String.format("Number of signatures: %d", names.size()));
        logger.info(String.format("Number of document revisions: %d", fields.getTotalRevisions()));

        for (String name : names) {
            logger.info(""); // Separate singatures by an empty line
            logger.info(String.format("Signature field name: %s", name));
            verifySignature(fields, name);
        }
    }

    private static PdfPKCS7 verifySignature(AcroFields fields, String name) throws OperationException {
        // digitalsignatures20130304.pdf : Code sample 5.2
        logger.info(String.format("  Signature covers the whole document: %b", fields.signatureCoversWholeDocument(name)));
        logger.info(String.format("  Document revision: %d of %d", fields.getRevision(name), fields.getTotalRevisions()));

        PdfPKCS7 pkcs7 = fields.verifySignature(name);
        verifySignature(pkcs7);

        return pkcs7;
    }

    private static PdfPKCS7 verifySignature(PdfPKCS7 pkcs7) throws OperationException {
        // digitalsignatures20130304.pdf : Code sample 5.2
        try {
            logger.info(String.format("  Integrity check OK: %b", pkcs7.verify()));
        } catch (GeneralSecurityException ex) {
            throw new OperationException("Could not verify a signature.", ex);
        }

        logger.info(String.format("  name: %s", pkcs7.getSignName()));
        logger.info(String.format("  reason: %s", pkcs7.getReason()));
        logger.info(String.format("  location: %s", pkcs7.getLocation()));

        // TODO: Format date
        //logger.info(String.format("  date: %s", pkcs7.getSignDate()));
        X509Certificate cert = pkcs7.getSigningCertificate();
        showCertInfo(cert);

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
        logger.info(principal.getName());
    }

}
