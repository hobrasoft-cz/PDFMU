package cz.hobrasoft.pdfmu;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

public class OutPdfArgs implements ArgsConfiguration, AutoCloseable {

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
                .type(Arguments.fileType().verifyCanCreate());
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
            Console.println("Output file has not been specified. Assuming in-place operation.");
            this.file = file;
        }
    }

    private OutputStream os;
    private PdfStamper stp;

    private void openOs() throws OperationException {
        if (file == null) {
            throw new OperationException("Output file has not been specified.");
        }
        assert os == null;
        
        Console.println(String.format("Output file: %s", file));

        if (file.exists()) {
            Console.println("Output file already exists.");
            if (overwrite) {
                Console.println("Overwriting the output file (--force flag is set).");
            } else {
                throw new OperationException("Set --force flag to overwrite.");
            }
        }

        // Open the output stream
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            throw new OperationException("Could not open the output stream.", ex);
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
            throw new OperationException("Could not open the PDF stamper.", ex);
        }
    }

    private void openStpNew(PdfReader pdfReader, char pdfVersion) throws OperationException {
        assert os != null;
        assert stp == null;

        // Open the PDF stamper
        try {
            stp = new PdfStamper(pdfReader, os, pdfVersion);
        } catch (DocumentException | IOException ex) {
            throw new OperationException("Could not open the PDF stamper.", ex);
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
            throw new OperationException("Could not close the PDF stamper.", ex);
        }
        stp = null;

        try {
            os.close();
        } catch (IOException ex) {
            throw new OperationException("Could not close the output stream.", ex);
        }
        os = null;
    }

    public PdfStamper getPdfStamper() {
        return stp;
    }

}
