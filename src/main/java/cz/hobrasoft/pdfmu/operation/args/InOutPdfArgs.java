/* 
 * Copyright (C) 2016 Hobrasoft s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.hobrasoft.pdfmu.operation.args;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import cz.hobrasoft.pdfmu.operation.OperationException;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

// Handle unset output file (in-place change)
public class InOutPdfArgs implements ArgsConfiguration, AutoCloseable {

    private InPdfArgs in;
    private OutPdfArgs out;

    public InOutPdfArgs() {
        // Allow append by default
        this(true);
    }

    public InOutPdfArgs(boolean allowAppend) {
        this("IN.pdf", allowAppend);
    }

    public InOutPdfArgs(String metavarIn) {
        // Allow append by default
        this(metavarIn, true);
    }

    public InOutPdfArgs(String metavarIn, boolean allowAppend) {
        in = new InPdfArgs(metavarIn);
        out = new OutPdfArgs(metavarIn, allowAppend);
    }

    @Override
    public void addArguments(ArgumentParser parser) {
        in.addArguments(parser);
        out.addArguments(parser);
    }

    @Override
    public void setFromNamespace(Namespace namespace) {
        in.setFromNamespace(namespace);
        out.setFromNamespace(namespace);
        out.setDefaultFile(in.getFile());
    }

    public void open() throws OperationException {
        open(false);
    }

    public void openSignature() throws OperationException {
        open(true);
    }

    public void open(boolean signature) throws OperationException {
        open(signature, '\0');
    }

    public void open(boolean signature, char pdfVersion) throws OperationException {
        PdfReader reader = in.open();
        out.open(reader, signature, pdfVersion);
    }

    public PdfReader getPdfReader() {
        return in.getPdfReader();
    }

    public PdfStamper getPdfStamper() {
        return out.getPdfStamper();
    }

    @Override
    public void close() throws OperationException {
        out.close();
        in.close();
    }

    public InPdfArgs getIn() {
        return in;
    }

    // :)
    public OutPdfArgs getOut() {
        return out;
    }

}
