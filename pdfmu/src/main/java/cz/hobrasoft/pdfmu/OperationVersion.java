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
    private void setPdfVersion(File outFile, PdfReader inPdfReader, char outPdfVersion) {
        // Open file output stream
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(outFile);
        } catch (FileNotFoundException ex) {
            System.err.println("Could not open the output file: " + ex.getMessage());
        }

        if (os != null) {
            // Open PDF stamper
            PdfStamper pdfStamper = null;
            try {
                // Set version immediately when opening the stamper
                pdfStamper = new PdfStamper(inPdfReader, os, outPdfVersion);
            } catch (DocumentException | IOException ex) {
                System.err.println("Could not open PDF stamper: " + ex.getMessage());
            }

            System.out.println("The PDF version has been successfully set.");

            if (pdfStamper != null) {
                // Close PDF stamper
                try {
                    pdfStamper.close();
                } catch (DocumentException | IOException ex) {
                    System.err.println("Could not close PDF stamper: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public void execute(Namespace namespace) {
        File inFile = namespace.get("in");

        // "in" argument is required.
        // TODO: Lift the requirement here
        assert inFile != null;

        System.out.println(String.format("Input PDF document: %s", inFile));

        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(inFile);
        } catch (FileNotFoundException ex) {
            System.err.println("Input file not found: " + ex.getMessage());
        }

        if (inStream != null) {
            PdfReader pdfReader = null;
            try {
                pdfReader = new PdfReader(inStream);
            } catch (IOException e) {
                System.err.println("Could not open the input PDF document: " + e.getMessage());
            }

            if (pdfReader != null) {
                PdfVersion inVersion = new PdfVersion(pdfReader.getPdfVersion());

                System.out.println(String.format("Input PDF document version: %s", inVersion));

                if (!namespace.getBoolean("get")) {
                    PdfVersion outVersion = namespace.get("set");
                    System.out.println(String.format("Desired output PDF version: %s", outVersion));
                    if (outVersion.compareTo(inVersion) < 0) {
                        System.err.println(String.format("Cannot lower the PDF version."));
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
                            setPdfVersion(outFile, pdfReader, outVersion.toChar());
                        } else {
                            System.err.println("Set --force flag to overwrite.");
                        }
                    }
                } else {
                    System.out.println("--get flag is set; no modifications will be made.");
                }

                pdfReader.close();
            }
            try {
                inStream.close();
            } catch (IOException ex) {
                System.err.println("Could not close the input file: " + ex.getMessage());
            }
        }
    }

    @Override
    public Subparser addParser(Subparsers subparsers) {
        String help = "Set or display version of a PDF document";
        String metavarIn = "IN.pdf";
        String metavarOut = "OUT.pdf";
        String metavarSet = "VERSION";

        Subparser subparser = subparsers.addParser("version")
                .help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationVersion.class);
        subparser.addArgument("-i", "--in")
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead())
                .help("input PDF document")
                .required(true)
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
         * {@link com.itextpdf.text.pdf.PdfStamper#PdfStamper(com.itextpdf.text.pdf.PdfReader, OutputStream, char)}
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
