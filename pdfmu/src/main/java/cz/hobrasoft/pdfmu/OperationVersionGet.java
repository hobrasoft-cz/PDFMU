package cz.hobrasoft.pdfmu;

import com.itextpdf.text.pdf.PdfReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class OperationVersionGet implements Operation {

    @Override
    public Subparser addParser(Subparsers subparsers) {
        String help = "Display PDF version of a PDF document";

        String metavarIn = "IN.pdf";

        // Add the subparser
        Subparser subparser = subparsers.addParser("get")
                .help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationVersionGet.class);

        // Add arguments to the subparser
        subparser.addArgument("in") // Positional alternative to "--in"
                .help("input PDF document")
                .metavar(metavarIn)
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead());

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        File inFile = namespace.get("in");
        assert inFile != null;

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

        // Close the PDF reader
        pdfReader.close();

        // Close the input stream
        try {
            inStream.close();
        } catch (IOException ex) {
            throw new OperationException("Could not close the input file.", ex);
        }
    }

}
