package cz.hobrasoft.pdfmu;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    @Override
    public void execute(Namespace namespace) {
        String inFilename = namespace.getString("in");
        assert inFilename != null; // Argument "in" is required
        // TODO: Lift the requirement here
        System.out.println(String.format("Input PDF document path: %s", inFilename));

        PdfReader pdfReader = null;
        try {
            pdfReader = new PdfReader(inFilename);
        } catch (IOException e) {
            System.err.println("Could not open the input PDF document: " + e.getMessage());
        }

        if (pdfReader != null) {
            System.out.println(String.format("Input PDF document version: 1.%c", pdfReader.getPdfVersion()));

            if (!namespace.getBoolean("get")) {
                String outFilename = namespace.getString("out");
                assert outFilename != null; // TODO: Handle `outFilename == null`

                System.out.println(String.format("Output PDF document path: %s", outFilename));

                // Open file output stream
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(outFilename);
                } catch (FileNotFoundException ex) {
                    System.err.println("Could not open the output PDF document: " + ex.getMessage());
                }

                if (os != null) {
                    char version = '6';
                    // TODO: Use "--set" argument

                    // TODO: Avoid lowering the version.
                    System.out.println(String.format("Setting PDF version to: %c", version));

                    // Open PDF stamper
                    PdfStamper pdfStamper = null;
                    try {
                        pdfStamper = new PdfStamper(pdfReader, os, version);
                    } catch (DocumentException | IOException ex) {
                        System.err.println("Could not open PDF stamper: " + ex.getMessage());
                    }

                    if (pdfStamper != null) {
                        // Close PDF stamper
                        try {
                            pdfStamper.close();
                        } catch (DocumentException | IOException ex) {
                            System.err.println("Could not close PDF stamper: " + ex.getMessage());
                        }
                    }
                }
            } else {
                System.out.println("--get argument present; no modifications will be made.");
            }

            pdfReader.close();
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
                .type(String.class)
                // TODO: Consider using `FileInputStream.class` as type
                // http://argparse4j.sourceforge.net/usage.html#argument-nargs
                // Also see:
                // http://argparse4j.sourceforge.net/usage.html#filetype
                .help("input PDF document")
                .required(true)
                .nargs("?")
                .metavar(metavarIn);
        subparser.addArgument("-o", "--out")
                .type(String.class)
                .help("output PDF document")
                .nargs("?")
                .metavar(metavarOut);
        subparser.addArgument("-s", "--set")
                .type(String.class)
                .help(String.format("set PDF version to %s", metavarSet))
                .nargs("?")
                .setDefault("1.6")
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

}
