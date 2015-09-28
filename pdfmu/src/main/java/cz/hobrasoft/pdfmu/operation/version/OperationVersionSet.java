package cz.hobrasoft.pdfmu.operation.version;

import com.itextpdf.text.pdf.PdfReader;
import cz.hobrasoft.pdfmu.operation.args.InOutPdfArgs;
import cz.hobrasoft.pdfmu.operation.args.InPdfArgs;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.OutPdfArgs;
import java.util.logging.Logger;
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

    private final InOutPdfArgs inout = new InOutPdfArgs();

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Set PDF version of a PDF document";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationVersionSet.class);

        inout.addArguments(subparser);

        String metavarVersion = "VERSION";
        subparser.addArgument("-v", "--version")
                .help(String.format("set PDF version to %s", metavarVersion))
                .metavar(metavarVersion)
                .type(PdfVersion.class)
                .setDefault(new PdfVersion("1.6"));

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        inout.setFromNamespace(namespace);
        PdfVersion outVersion = namespace.get("version");

        execute(inout, outVersion);
    }

    private static void execute(InOutPdfArgs inout, PdfVersion outVersion) throws OperationException {
        InPdfArgs in = inout.getIn();
        OutPdfArgs out = inout.getOut();

        execute(in, out, outVersion);
    }

    private static void execute(InPdfArgs in, OutPdfArgs out, PdfVersion outVersion) throws OperationException {
        in.open();

        PdfReader pdfReader = in.getPdfReader();

        // Fetch the PDF version of the input PDF document
        PdfVersion inVersion = new PdfVersion(pdfReader.getPdfVersion());
        logger.info(String.format("Input PDF document version: %s", inVersion));

        // Commence to set the PDF version of the output PDF document
        // Determine the desired PDF version
        assert outVersion != null; // The argument "version" has a default value
        logger.info(String.format("Desired output PDF version: %s", outVersion));

        if (outVersion.compareTo(inVersion) < 0) {
            // The desired version is lower than the current version.
            throw new OperationException("Cannot lower the PDF version.");
            // TODO: Add --force-lower-version flag that enables lowering the version
        }

        out.open(pdfReader, false, outVersion.toChar());

        out.close();
        in.close();
    }

}
