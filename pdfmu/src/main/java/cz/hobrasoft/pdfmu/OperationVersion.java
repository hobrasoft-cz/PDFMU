package cz.hobrasoft.pdfmu;

import com.itextpdf.text.pdf.PdfReader;
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
        System.out.println(String.format("Input PDF document path: %s", inFilename));
        
        PdfReader pdfReader = null;
        try {
            pdfReader = new PdfReader(inFilename);
        } catch (IOException e) {
            System.err.println("Could not open the input PDF document: " + e.getMessage());
        }
        
        if (pdfReader != null) {
            System.out.println(String.format("Input PDF document version: 1.%c", pdfReader.getPdfVersion()));
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
