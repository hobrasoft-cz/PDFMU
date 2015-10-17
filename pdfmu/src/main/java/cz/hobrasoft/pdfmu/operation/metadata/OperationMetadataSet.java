package cz.hobrasoft.pdfmu.operation.metadata;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import cz.hobrasoft.pdfmu.jackson.EmptyResult;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationCommon;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.InOutPdfArgs;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class OperationMetadataSet extends OperationCommon {

    private static final Logger logger = Logger.getLogger(OperationMetadataSet.class.getName());

    private final MetadataParameters metadataParameters = new MetadataParameters();

    private final InOutPdfArgs inout = new InOutPdfArgs();

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Update metadata in a PDF document";

        // Configure the subparser
        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationMetadataSet.class);

        inout.addArguments(subparser);
        metadataParameters.addArguments(subparser);

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        inout.setFromNamespace(namespace);
        metadataParameters.setFromNamespace(namespace);
        set(inout, metadataParameters);
        writeResult(new EmptyResult());
    }

    private static void set(InOutPdfArgs inout, MetadataParameters metadataParameters) throws OperationException {
        inout.open();

        PdfReader reader = inout.getPdfReader();
        PdfStamper stp = inout.getPdfStamper();

        set(reader, stp, metadataParameters);

        inout.close();
    }

    private static void set(PdfReader reader, PdfStamper stamper, MetadataParameters metadataParameters) {
        Map<String, String> info = metadataParameters.getInfo(reader);
        set(stamper, info);
    }

    private static final List<String> ignoredProperties
            = Arrays.asList(new String[]{"Producer", "ModDate"});

    public static void set(PdfStamper stp, Map<String, String> info) {
        assert stp != null;
        assert info != null;

        for (String key : ignoredProperties) {
            if (info.containsKey(key)) {
                String value = info.get(key);
                logger.warning(String.format("Warning: The property %s is set automatically. The value \"%s\" will be ignored.", key, value));
            }
        }

        stp.setMoreInfo(info);
        logger.info("PDF metadata have been set.");
    }

    private static Operation instance = null;

    public static Operation getInstance() {
        if (instance == null) {
            instance = new OperationMetadataSet();
        }
        return instance;
    }

    private OperationMetadataSet() {
        // Singleton
    }

}
