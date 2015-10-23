package cz.hobrasoft.pdfmu.operation.args;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import static cz.hobrasoft.pdfmu.PdfmuError.OUTPUT_CLOSE;
import static cz.hobrasoft.pdfmu.PdfmuError.OUTPUT_EXISTS_FORCE_NOT_SET;
import static cz.hobrasoft.pdfmu.PdfmuError.OUTPUT_NOT_SPECIFIED;
import static cz.hobrasoft.pdfmu.PdfmuError.OUTPUT_OPEN;
import static cz.hobrasoft.pdfmu.PdfmuError.OUTPUT_STAMPER_CLOSE;
import static cz.hobrasoft.pdfmu.PdfmuError.OUTPUT_STAMPER_OPEN;
import cz.hobrasoft.pdfmu.operation.OperationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

public class OutPdfArgs implements ArgsConfiguration, AutoCloseable {

    private static final Logger logger = Logger.getLogger(OutPdfArgs.class.getName());

    private final String metavarIn;
    private final String metavarOut = "OUT.pdf";

    public OutPdfArgs(String metavarIn) {
        this.metavarIn = metavarIn;
    }

    @Override
    public void addArguments(ArgumentParser parser) {
        parser.addArgument("-o", "--out")
                .help(String.format("output PDF document (default: <%s>)", metavarIn))
                .metavar(metavarOut)
                .type(Arguments.fileType());
        parser.addArgument("-f", "--force")
                .help(String.format("overwrite %s if it exists", metavarOut))
                .type(boolean.class)
                .action(Arguments.storeTrue());
    }

    private File file = null;
    private boolean overwrite = false;

    @Override
    public void setFromNamespace(Namespace namespace) {
        file = namespace.get("out");
        overwrite = namespace.getBoolean("force");
    }

    public void setDefaultFile(File file) {
        if (this.file == null) {
            logger.info("Output file has not been specified. Assuming in-place operation.");
            this.file = file;
        }
    }

    private OutputStream os;
    private PdfStamper stp;

    private void openOs() throws OperationException {
        if (file == null) {
            throw new OperationException(OUTPUT_NOT_SPECIFIED);
        }
        assert os == null;

        logger.info(String.format("Output file: %s", file));

        if (file.exists()) {
            logger.info("Output file already exists.");
            if (overwrite) {
                logger.info("Overwriting the output file (--force flag is set).");
            } else {
                throw new OperationException(OUTPUT_EXISTS_FORCE_NOT_SET);
            }
        }

        // Open the output stream
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            throw new OperationException(OUTPUT_OPEN, ex);
        }
    }

    private void openStpSignature(PdfReader pdfReader, char pdfVersion, boolean append) throws OperationException {
        assert os != null;
        assert stp == null;

        try {
            // digitalsignatures20130304.pdf : Code sample 2.17
            // TODO?: Make sure version is high enough
            stp = PdfStamper.createSignature(pdfReader, os, pdfVersion, null, append);
        } catch (DocumentException | IOException ex) {
            throw new OperationException(OUTPUT_STAMPER_OPEN, ex);
        }
    }

    private void openStpNew(PdfReader pdfReader, char pdfVersion) throws OperationException {
        assert os != null;
        assert stp == null;

        // Open the PDF stamper
        try {
            stp = new PdfStamper(pdfReader, os, pdfVersion);
        } catch (DocumentException | IOException ex) {
            throw new OperationException(OUTPUT_STAMPER_OPEN, ex);
        }
    }

    public PdfStamper open(PdfReader pdfReader, boolean signature, char pdfVersion, boolean append) throws OperationException {
        assert file != null;

        openOs();

        if (signature) {
            openStpSignature(pdfReader, pdfVersion, append);
        } else {
            openStpNew(pdfReader, pdfVersion);
        }

        return stp;
    }

    public PdfStamper open(PdfReader pdfReader, boolean signature, char pdfVersion) throws OperationException {
        return open(pdfReader, signature, pdfVersion, true);
    }

    public PdfStamper open(PdfReader pdfReader, boolean signature) throws OperationException {
        return open(pdfReader, signature, '\0');
    }

    public PdfStamper open(PdfReader pdfReader) throws OperationException {
        return open(pdfReader, false);
    }

    public PdfStamper openSignature(PdfReader pdfReader) throws OperationException {
        return open(pdfReader, true);
    }

    @Override
    public void close() throws OperationException {
        try {
            stp.close();
        } catch (DocumentException | IOException ex) {
            throw new OperationException(OUTPUT_STAMPER_CLOSE, ex);
        }
        stp = null;

        try {
            os.close();
        } catch (IOException ex) {
            throw new OperationException(OUTPUT_CLOSE, ex);
        }
        os = null;
    }

    public PdfStamper getPdfStamper() {
        return stp;
    }

    public File getFile() {
        return file;
    }

}
