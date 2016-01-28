package cz.hobrasoft.pdfmu.operation.args;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import cz.hobrasoft.pdfmu.PdfmuUtils;
import static cz.hobrasoft.pdfmu.error.ErrorType.OUTPUT_CLOSE;
import static cz.hobrasoft.pdfmu.error.ErrorType.OUTPUT_EXISTS_FORCE_NOT_SET;
import static cz.hobrasoft.pdfmu.error.ErrorType.OUTPUT_NOT_SPECIFIED;
import static cz.hobrasoft.pdfmu.error.ErrorType.OUTPUT_OPEN;
import static cz.hobrasoft.pdfmu.error.ErrorType.OUTPUT_STAMPER_CLOSE;
import static cz.hobrasoft.pdfmu.error.ErrorType.OUTPUT_STAMPER_OPEN;
import static cz.hobrasoft.pdfmu.error.ErrorType.OUTPUT_WRITE;
import cz.hobrasoft.pdfmu.operation.OperationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

public class OutPdfArgs implements ArgsConfiguration, AutoCloseable {

    private static final Logger logger = Logger.getLogger(OutPdfArgs.class.getName());

    private final String metavarIn;
    private final String metavarOut = "OUT.pdf";
    private final boolean allowAppend;

    public OutPdfArgs(String metavarIn, boolean allowAppend) {
        this.metavarIn = metavarIn;
        this.allowAppend = allowAppend;
    }

    @Override
    public void addArguments(ArgumentParser parser) {
        parser.addArgument("-o", "--out")
                .help(String.format("output PDF document (default: <%s>)", metavarIn))
                .metavar(metavarOut)
                .type(Arguments.fileType());

        if (allowAppend) {
            parser.addArgument("--append")
                    .help("append to the document, creating a new revision")
                    .type(boolean.class)
                    .setDefault(true);
        }

        parser.addArgument("-f", "--force")
                .help(String.format("overwrite %s if it exists", metavarOut))
                .type(boolean.class)
                .action(Arguments.storeTrue());
    }

    private File file = null;
    private boolean overwrite = false;
    private boolean append = false;

    @Override
    public void setFromNamespace(Namespace namespace) {
        file = namespace.get("out");
        overwrite = namespace.getBoolean("force");

        if (allowAppend) {
            append = namespace.getBoolean("append");
        } else {
            append = false;
        }
    }

    public void setDefaultFile(File file) {
        if (this.file == null) {
            logger.info("Output file has not been specified. Assuming in-place operation.");
            this.file = file;
        }
    }

    private ByteArrayOutputStream os;
    private PdfStamper stp;

    private void openOs() throws OperationException {
        assert os == null;

        // Initialize the array length to the file size
        // because the whole file will have to fit in the array anyway.
        os = new ByteArrayOutputStream((int) file.length());
    }

    private void openStpSignature(PdfReader pdfReader, char pdfVersion) throws OperationException {
        assert os != null;
        assert stp == null;

        try {
            // digitalsignatures20130304.pdf : Code sample 2.17
            // TODO?: Make sure version is high enough
            stp = PdfStamper.createSignature(pdfReader, os, pdfVersion, null, append);
        } catch (DocumentException | IOException ex) {
            throw new OperationException(OUTPUT_STAMPER_OPEN, ex,
                    PdfmuUtils.sortedMap(new SimpleEntry<String, Object>("outputFile", file)));
        }
    }

    private void openStpNew(PdfReader pdfReader, char pdfVersion) throws OperationException {
        assert os != null;
        assert stp == null;

        // Open the PDF stamper
        try {
            stp = new PdfStamper(pdfReader, os, pdfVersion, append);
        } catch (DocumentException | IOException ex) {
            throw new OperationException(OUTPUT_STAMPER_OPEN, ex,
                    PdfmuUtils.sortedMap(new SimpleEntry<String, Object>("outputFile", file)));
        }
    }

    /**
     * Returns a {@link PdfStamper} associated with the internal buffer. Using a
     * buffer instead of an actual file means that the operation can be rolled
     * back completely, leaving the output file untouched. Call {@link #close}
     * to save the content of the buffer to the output file.
     *
     * @param pdfReader the input {@link PdfReader} to operate on
     * @param signature shall we be signing the document?
     * @param pdfVersion the last character of the PDF version number ('2' to
     * '7'), or '\0' to keep the original version
     *
     * @throws OperationException if an error occurs
     */
    public PdfStamper open(PdfReader pdfReader, boolean signature, char pdfVersion) throws OperationException {
        if (file == null) {
            throw new OperationException(OUTPUT_NOT_SPECIFIED);
        }
        assert file != null;

        logger.info(String.format("Output file: %s", file));
        if (file.exists()) {
            logger.info("Output file already exists.");
            if (overwrite) {
                logger.info("Will overwrite the output file (--force flag is set).");
            } else {
                throw new OperationException(OUTPUT_EXISTS_FORCE_NOT_SET,
                        PdfmuUtils.sortedMap(new SimpleEntry<String, Object>("outputFile", file)));
            }
        }

        openOs();

        if (signature) {
            openStpSignature(pdfReader, pdfVersion);
        } else {
            openStpNew(pdfReader, pdfVersion);
        }

        return stp;
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

    /**
     * Writes the content of the internal buffer to the output file.
     *
     * @throws OperationException if an error occurs when closing the
     * {@link PdfStamper} or when writing to the output file
     */
    @Override
    public void close() throws OperationException {
        try {
            stp.close();
        } catch (DocumentException | IOException ex) {
            throw new OperationException(OUTPUT_STAMPER_CLOSE, ex,
                    PdfmuUtils.sortedMap(new SimpleEntry<String, Object>("outputFile", file)));
        }
        stp = null;

        assert file != null;
        logger.info(String.format("Writing the output of the operation to the output file: %s", file));

        // Save the content of `os` to `file`.
        { // fileOs
            OutputStream fileOs = null;
            try {
                fileOs = new FileOutputStream(file);
            } catch (FileNotFoundException ex) {
                throw new OperationException(OUTPUT_OPEN, ex,
                        PdfmuUtils.sortedMap(new SimpleEntry<String, Object>("outputFile", file)));

            }
            assert fileOs != null;
            try {
                assert os != null;
                os.writeTo(fileOs);
            } catch (IOException ex) {
                throw new OperationException(OUTPUT_WRITE, ex,
                        PdfmuUtils.sortedMap(new SimpleEntry<String, Object>("outputFile", file)));
            }
            try {
                fileOs.close();
            } catch (IOException ex) {
                throw new OperationException(OUTPUT_CLOSE, ex,
                        PdfmuUtils.sortedMap(new SimpleEntry<String, Object>("outputFile", file)));
            }
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
