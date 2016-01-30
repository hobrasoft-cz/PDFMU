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
package cz.hobrasoft.pdfmu.operation.version;

import com.itextpdf.text.pdf.PdfReader;
import cz.hobrasoft.pdfmu.jackson.VersionGet;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationCommon;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.InPdfArgs;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * Displays the PDF version of a PDF document
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationVersionGet extends OperationCommon {

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
        to.println(String.format("Input PDF document version: %s", inVersion));
        writeResult(new VersionGet(inVersion.toString()));

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
