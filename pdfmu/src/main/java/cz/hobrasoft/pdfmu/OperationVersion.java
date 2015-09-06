package cz.hobrasoft.pdfmu;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Gets or sets the version of a PDF file
 *
 * <p>
 * Usage:
 * <ul>
 * <li>{@code pdfmu version --in in.pdf --out out.pdf --set 1.6}</li>
 * <li>{@code pdfmu version --in in.pdf --get}</li>
 * <li>{@code pdfmu version inout.pdf --force}</li>
 * </ul>
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationVersion implements Operation {

    /**
     * Copies a PDF document, changing its version
     */
    private void setPdfVersion(File outFile, PdfReader inPdfReader, char outPdfVersion) throws OperationException {
        // Open output stream
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(outFile);
        } catch (FileNotFoundException ex) {
            throw new OperationException("Could not open the output file.", ex);
        }
        assert os != null;

        // Open PDF stamper
        PdfStamper pdfStamper = null;
        try {
            // Set version immediately when opening the stamper
            pdfStamper = new PdfStamper(inPdfReader, os, outPdfVersion);
        } catch (DocumentException | IOException ex) {
            throw new OperationException("Could not open PDF stamper.", ex);
        }
        assert pdfStamper != null;

        System.out.println("The PDF version has been successfully set.");

        // Close PDF stamper
        try {
            pdfStamper.close();
        } catch (DocumentException | IOException ex) {
            throw new OperationException("Could not close PDF stamper.", ex);
        }

        // Close output stream
        try {
            os.close();
        } catch (IOException ex) {
            throw new OperationException("Could not close the output file.", ex);
        }
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        File inFile = namespace.get("in");
        if (inFile == null) {
            throw new OperationException("Input PDF document has not been specified. Use the --in option to set the input PDF document.");
        }

        System.out.println(String.format("Input PDF document: %s", inFile));

        // Open the input stream
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(inFile);
        } catch (FileNotFoundException ex) {
            throw new OperationException("Input file not found.", ex);
        }
        assert inStream != null;

        // Open the PDF reader
        // PdfReader parses a PDF document.
        PdfReader pdfReader = null;
        try {
            pdfReader = new PdfReader(inStream);
        } catch (IOException ex) {
            throw new OperationException("Could not open the input PDF document.", ex);
        }
        assert pdfReader != null;

        // Fetch the PDF version of the input PDF document
        PdfVersion inVersion = new PdfVersion(pdfReader.getPdfVersion());
        System.out.println(String.format("Input PDF document version: %s", inVersion));

        if (!namespace.getBoolean("get")) {
            // Commence to set the PDF version of the output PDF document

            // Determine the desired PDF version
            PdfVersion outVersion = namespace.get("set");
            assert outVersion != null; // The argument "set" has a default value
            System.out.println(String.format("Desired output PDF version: %s", outVersion));

            if (outVersion.compareTo(inVersion) < 0) {
                // The desired version is lower than the current version.
                throw new OperationException("Cannot lower the PDF version.");
                // TODO: Add --force-lower-version flag that enables lowering the version
            } else {
                File outFile = namespace.get("out");
                if (outFile == null) {
                    System.out.println("--out option not specified; assuming in-place version change");
                    outFile = inFile;
                }

                System.out.println(String.format("Output PDF document: %s", outFile));

                if (outFile.exists()) {
                    System.out.println("Output file already exists.");
                }

                if (!outFile.exists() || namespace.getBoolean("force")) {
                    // Creating a new file or allowed to overwrite the old one
                    setPdfVersion(outFile, pdfReader, outVersion.toChar());
                } else {
                    throw new OperationException("Set --force flag to overwrite.");
                }
            }
        } else {
            System.out.println("--get flag is set; no modifications will be made.");
        }

        // Close the PDF reader
        pdfReader.close();

        // Close the input stream
        try {
            inStream.close();
        } catch (IOException ex) {
            throw new OperationException("Could not close the input file.", ex);
        }
    }

    @Override
    public Subparser addParser(Subparsers subparsers) {
        String help = "Set or display version of a PDF document";
        String metavarIn = "IN.pdf";
        String metavarOut = "OUT.pdf";
        String metavarSet = "VERSION";

        // Add the subparser
        Subparser subparser = subparsers.addParser("version")
                .help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationVersion.class);

        // Add arguments to the subparser
        subparser.addArgument("-i", "--in")
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead())
                .help("input PDF document")
                .metavar(metavarIn);
        subparser.addArgument("in") // positional alternative to "--in"
                .help("input PDF document")
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead())
                .nargs("?")
                .metavar(metavarIn);
        subparser.addArgument("-o", "--out")
                .type(Arguments.fileType().verifyCanCreate())
                .help("output PDF document")
                .nargs("?")
                .metavar(metavarOut);
        subparser.addArgument("-s", "--set")
                .type(PdfVersion.class)
                .help(String.format("set PDF version to %s", metavarSet))
                .nargs("?")
                .setDefault(new PdfVersion("1.6"))
                .metavar(metavarSet);
        subparser.addArgument("-g", "--get")
                .type(boolean.class)
                .help(String.format("display version of %s without setting the version", metavarIn))
                .action(Arguments.storeTrue());
        subparser.addArgument("--force")
                .help(String.format("overwrite %s if already present", metavarOut))
                .type(boolean.class)
                .action(Arguments.storeTrue());

        return subparser;
    }

    /**
     * Represents a PDF version
     *
     * <p>
     * Versions 1.2 to 1.7 are supported. This range corresponds to the versions
     * supported by iText, as seen in {@link com.itextpdf.text.pdf.PdfWriter}
     * static fields {@link com.itextpdf.text.pdf.PdfWriter#VERSION_1_2} etc.
     */
    static public class PdfVersion implements Comparable<PdfVersion> {

        static private Pattern p = Pattern.compile("1\\.(?<charValue>[2-7])");

        private char charValue;

        /**
         * Creates a PDF version from its string representation
         *
         * <p>
         * Versions 1.2 to 1.7 are supported. Valid string representations
         * include:
         * <ul>
         * <li>{@code 1.3}</li>
         * <li>{@code 1.6}</li>
         * </ul>
         *
         * @param stringValue string representation of the version
         * @throws IllegalArgumentException if stringValue does not represent a
         * valid version
         */
        public PdfVersion(String stringValue) throws IllegalArgumentException {
            Matcher m = p.matcher(stringValue);
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid or unsupported PDF version; use 1.2 to 1.7");
            }
            String charValueString = m.group("charValue");
            assert charValueString.length() == 1;
            charValue = charValueString.charAt(0);
        }

        /**
         * Creates a PDF version from the last character of its string
         * representation
         *
         * <p>
         * Since versions 1.2 to 1.7 are supported, a valid PDF version is
         * uniquely specified by its last character (the digit 2 to 7).
         *
         * <p>
         * Version in this format is returned by
         * {@link com.itextpdf.text.pdf.PdfReader#getPdfVersion()}.
         *
         * @param charValue last character of the PDF version
         * @throws IllegalArgumentException if charValue does not represent a
         * valid version
         */
        public PdfVersion(char charValue) throws IllegalArgumentException {
            if (charValue < '2' || charValue > '7') {
                throw new IllegalArgumentException("Invalid or unsupported PDF version; use 2 to 7");
            }
            this.charValue = charValue;
        }

        @Override
        public String toString() {
            return String.format("1.%c", charValue);
        }

        /**
         * Returns the last character of the PDF version
         *
         * <p>
         * This format of PDF version is accepted for example by
         * {@link PdfStamper#PdfStamper(PdfReader, OutputStream, char)}
         *
         * @return last character of the PDF version
         */
        public char toChar() {
            return charValue;
        }

        @Override
        public int compareTo(PdfVersion o) {
            return charValue - o.charValue;
        }
    }

}
