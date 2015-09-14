package cz.hobrasoft.pdfmu.version;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import cz.hobrasoft.pdfmu.Operation;
import cz.hobrasoft.pdfmu.OperationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * Sets the PDF version of a PDF document
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationVersionSet implements Operation {

    private static final Logger logger = Logger.getLogger(OperationVersionSet.class.getName());

    @Override
    public String getCommandName() {
        return "set";
    }

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Set PDF version of a PDF document";

        String metavarIn = "IN.pdf";
        String metavarOut = "OUT.pdf";
        String metavarVersion = "VERSION";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationVersionSet.class);

        // Add arguments to the subparser
        subparser.addArgument("in") // Positional alternative to "--in"
                .help("input PDF document")
                .metavar(metavarIn)
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead());
        subparser.addArgument("-o", "--out")
                .help("output PDF document")
                .metavar(metavarOut)
                .type(Arguments.fileType().verifyCanCreate())
                .nargs("?");
        subparser.addArgument("--force")
                .help(String.format("overwrite %s if it exists", metavarOut))
                .type(boolean.class)
                .action(Arguments.storeTrue());
        subparser.addArgument("-v", "--version")
                .help(String.format("set PDF version to %s", metavarVersion))
                .metavar(metavarVersion)
                .type(PdfVersion.class)
                .nargs("?")
                .setDefault(new PdfVersion("1.6"));

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        File inFile = namespace.get("in");
        assert inFile != null;

        logger.info(String.format("Input PDF document: %s", inFile));

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
        logger.info(String.format("Input PDF document version: %s", inVersion));

        // Commence to set the PDF version of the output PDF document
        // Determine the desired PDF version
        PdfVersion outVersion = namespace.get("version");
        assert outVersion != null; // The argument "version" has a default value
        logger.info(String.format("Desired output PDF version: %s", outVersion));

        if (outVersion.compareTo(inVersion) < 0) {
            // The desired version is lower than the current version.
            throw new OperationException("Cannot lower the PDF version.");
            // TODO: Add --force-lower-version flag that enables lowering the version
        } else {
            File outFile = namespace.get("out");
            if (outFile == null) {
                logger.info("--out option not specified; assuming in-place version change");
                outFile = inFile;
            }

            logger.info(String.format("Output PDF document: %s", outFile));

            if (outFile.exists()) {
                logger.info("Output file already exists.");
            }

            if (!outFile.exists() || namespace.getBoolean("force")) {
                // Creating a new file or allowed to overwrite the old one
                setPdfVersion(outFile, pdfReader, outVersion.toChar());
            } else {
                throw new OperationException("Set --force flag to overwrite.");
            }
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

        logger.info("The PDF version has been successfully set.");

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

}
