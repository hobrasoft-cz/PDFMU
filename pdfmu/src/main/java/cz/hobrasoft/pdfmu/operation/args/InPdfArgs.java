package cz.hobrasoft.pdfmu.operation.args;

import com.itextpdf.text.pdf.PdfReader;
import static cz.hobrasoft.pdfmu.PdfmuError.INPUT_CLOSE;
import static cz.hobrasoft.pdfmu.PdfmuError.INPUT_NOT_FOUND;
import static cz.hobrasoft.pdfmu.PdfmuError.INPUT_NOT_VALID_PDF;
import cz.hobrasoft.pdfmu.PdfmuUtils;
import cz.hobrasoft.pdfmu.operation.OperationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

public class InPdfArgs implements ArgsConfiguration, AutoCloseable {

    private final String name = "in";
    private final String help = "input PDF document";
    private final String metavar;

    private static final Logger logger = Logger.getLogger(InPdfArgs.class.getName());

    public InPdfArgs(String metavar) {
        this.metavar = metavar;
    }

    public InPdfArgs() {
        this("IN.pdf");
    }

    @Override
    public void addArguments(ArgumentParser parser) {
        parser.addArgument(name)
                .help(help)
                .metavar(metavar)
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead());
    }

    private File file = null;

    public File getFile() {
        return file;
    }

    private InputStream is = null;
    private PdfReader pdfReader = null;

    @Override
    public void setFromNamespace(Namespace namespace) {
        file = namespace.get(name);
        assert file != null; // Required argument (because it is positional)
    }

    public PdfReader open() throws OperationException {
        assert file != null;
        assert is == null;
        assert pdfReader == null;

        logger.info(String.format("Input file: %s", file));

        // Open the input stream
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new OperationException(INPUT_NOT_FOUND, ex,
                    PdfmuUtils.sortedMap(new String[]{"file"}, new Object[]{file}));
        }

        // Open the PDF reader
        try {
            pdfReader = new PdfReader(is);
        } catch (IOException ex) {
            throw new OperationException(INPUT_NOT_VALID_PDF, ex,
                    PdfmuUtils.sortedMap(new String[]{"file"}, new Object[]{file}));
        }

        return pdfReader;
    }

    @Override
    public void close() throws OperationException {
        // Close the PDF reader
        pdfReader.close();
        pdfReader = null;

        // Close the input stream
        try {
            is.close(); // May throw IOException
        } catch (IOException ex) {
            throw new OperationException(INPUT_CLOSE, ex);
        }
        is = null;
    }

    public PdfReader getPdfReader() {
        return pdfReader;
    }

}
