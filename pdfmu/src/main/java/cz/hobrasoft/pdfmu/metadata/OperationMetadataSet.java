package cz.hobrasoft.pdfmu.metadata;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import cz.hobrasoft.pdfmu.Console;
import cz.hobrasoft.pdfmu.Operation;
import cz.hobrasoft.pdfmu.OperationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class OperationMetadataSet implements Operation {

    private final MetadataParameters metadataParameters = new MetadataParameters();

    @Override
    public String getCommandName() {
        return "set";
    }

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Update metadata in a PDF document";

        String metavarIn = "IN.pdf";
        String metavarOut = "OUT.pdf";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationMetadataSet.class);

        // Add arguments to the subparser
        // Positional arguments are required by default
        subparser.addArgument("in")
                .help("input PDF document")
                .metavar(metavarIn)
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead())
                .required(true);

        subparser.addArgument("-o", "--out")
                .help(String.format("output PDF document (default: <%s>)", metavarIn))
                .metavar(metavarOut)
                .type(Arguments.fileType().verifyCanCreate())
                .nargs("?");
        subparser.addArgument("-f", "--force")
                .help(String.format("overwrite %s if it exists", metavarOut))
                .type(boolean.class)
                .action(Arguments.storeTrue());

        metadataParameters.addArguments(subparser);

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        // Input file
        File inFile = namespace.get("in");
        assert inFile != null; // Required argument

        // Output file
        File outFile = namespace.get("out");

        boolean forceOverwrite = namespace.getBoolean("force");
        // Note: "force" argument is required

        metadataParameters.setFromNamespace(namespace);

        set(inFile, outFile, forceOverwrite, metadataParameters.getInfo());
    }

    private static void set(File inFile, File outFile, boolean forceOverwrite, Map<String, String> info) throws OperationException {
        assert inFile != null;

        Console.println(String.format("Input PDF document: %s", inFile));

        // Open the input stream
        FileInputStream inStream;
        try {
            inStream = new FileInputStream(inFile);
        } catch (FileNotFoundException ex) {
            throw new OperationException("Input file not found.", ex);
        }

        // Open the PDF reader
        // PdfReader parses a PDF document.
        PdfReader pdfReader;
        try {
            pdfReader = new PdfReader(inStream);
        } catch (IOException ex) {
            throw new OperationException("Could not open the input PDF document.", ex);
        }

        // Set `outFile` to `inFile` if not set
        // Handle calls of type `pdfmu signature add INOUT.pdf`
        if (outFile == null) {
            Console.println("Output file not specified. Assuming in-place operation.");
            outFile = inFile;
        }

        set(pdfReader, outFile, forceOverwrite, info);

        // Close the PDF reader
        pdfReader.close();

        // Close the input stream
        try {
            inStream.close();
        } catch (IOException ex) {
            throw new OperationException("Could not close the input file.", ex);
        }
    }

    private static void set(PdfReader pdfReader,
            File outFile,
            boolean forceOverwrite,
            Map<String, String> info) throws OperationException {
        assert outFile != null;

        Console.println(String.format("Output PDF document: %s", outFile));

        if (outFile.exists()) {
            Console.println("Output file already exists.");
            if (forceOverwrite) {
                Console.println("Overwriting the output file (--force flag is set).");
            } else {
                throw new OperationException("Set --force flag to overwrite.");
            }
        }

        set(pdfReader, outFile, info);
    }

    private static void set(PdfReader pdfReader,
            File outFile,
            Map<String, String> info) throws OperationException {
        assert outFile != null;

        // Open the output stream
        FileOutputStream os;
        try {
            os = new FileOutputStream(outFile);
        } catch (FileNotFoundException ex) {
            throw new OperationException("Could not open the output file.", ex);
        }

        PdfStamper stp;
        try {
            // TODO?: Make sure version is high enough
            stp = new PdfStamper(pdfReader, os, '\0');
        } catch (DocumentException | IOException ex) {
            throw new OperationException("Could not open the PDF stamper.", ex);
        }

        set(stp, info);

        // Close the PDF stamper
        try {
            stp.close();
        } catch (DocumentException | IOException ex) {
            throw new OperationException("Could not close PDF stamper.", ex);
        }

        // Close the output stream
        try {
            os.close();
        } catch (IOException ex) {
            throw new OperationException("Could not close the output file.", ex);
        }
    }

    private static void set(PdfStamper stp, Map<String, String> info) {
        assert stp != null;
        assert info != null;
        stp.setMoreInfo(info);
        Console.println("PDF metadata have been set.");
    }

}
