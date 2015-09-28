package cz.hobrasoft.pdfmu.operation.version;

import com.itextpdf.text.pdf.PdfReader;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationCommon;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.InPdfArgs;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * Displays the PDF version of a PDF document
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationVersionGet extends OperationCommon {

    private static final Logger logger = Logger.getLogger(OperationVersionGet.class.getName());

    @Override
    public String getCommandName() {
        // TODO: Make consistent with `pdfmu signature display`
        return "get";
    }

    private final InPdfArgs in = new InPdfArgs();

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Display PDF version of a PDF document";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationVersionGet.class);

        in.addArguments(subparser);

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        in.setFromNamespace(namespace);

        in.open();
        PdfReader pdfReader = in.getPdfReader();

        // Fetch the PDF version of the input PDF document
        PdfVersion inVersion = new PdfVersion(pdfReader.getPdfVersion());
        logger.info(String.format("Input PDF document version: %s", inVersion));

        in.close();
    }

    private static Operation instance = null;

    public static Operation getInstance() {
        if (instance == null) {
            instance = new OperationVersionGet();
        }
        return instance;
    }

    private OperationVersionGet() {
        // Singleton
    }

}
