package cz.hobrasoft.pdfmu.operation.metadata;

import com.itextpdf.text.pdf.PdfReader;
import cz.hobrasoft.pdfmu.jackson.MetadataGet;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationCommon;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.InPdfArgs;
import java.util.Map;
import java.util.SortedMap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class OperationMetadataGet extends OperationCommon {

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
        SortedMap<String, String> properties = get(in.getPdfReader());
        in.close();

        writeResult(new MetadataGet(properties));
    }

    private SortedMap<String, String> get(PdfReader pdfReader) {
        Map<String, String> properties = pdfReader.getInfo();

        MetadataParameters mp = new MetadataParameters();
        mp.setFromInfo(properties);

        SortedMap<String, String> propertiesSorted = mp.getSorted();

        {
            to.indentMore("Properties:");
            for (Map.Entry<String, String> property : propertiesSorted.entrySet()) {
                String key = property.getKey();
                String value = property.getValue();
                to.println(String.format("%s: %s", key, value));
            }
            to.indentLess();
        }

        return propertiesSorted;
    }

    private static Operation instance = null;

    public static Operation getInstance() {
        if (instance == null) {
            instance = new OperationMetadataGet();
        }
        return instance;
    }

    private OperationMetadataGet() {
        // Singleton
    }

}
