package cz.hobrasoft.pdfmu.operation.metadata;

import com.itextpdf.text.pdf.PdfReader;
import cz.hobrasoft.pdfmu.Console;
import cz.hobrasoft.pdfmu.operation.OperationCommon;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.InPdfArgs;
import java.util.Map;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class OperationMetadataGet extends OperationCommon {

    @Override
    public String getCommandName() {
        return "get";
    }

    private final InPdfArgs in = new InPdfArgs();

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Display metadata in a PDF document";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationMetadataGet.class);

        in.addArguments(subparser);

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        in.setFromNamespace(namespace);

        in.open();
        get(in.getPdfReader());
        in.close();
    }

    private static void get(PdfReader pdfReader) {
        Map<String, String> properties = pdfReader.getInfo();

        MetadataParameters mp = new MetadataParameters();
        mp.setFromInfo(properties);

        {
            Console.indentMore("Properties:");
            for (Map.Entry<String, String> property : mp.getSorted().entrySet()) {
                String key = property.getKey();
                String value = property.getValue();
                Console.println(String.format("%s: %s", key, value));
            }
            Console.indentLess();
        }
    }

}
