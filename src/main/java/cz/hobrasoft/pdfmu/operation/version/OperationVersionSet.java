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
import cz.hobrasoft.pdfmu.jackson.VersionSet;
import cz.hobrasoft.pdfmu.operation.Operation;
import cz.hobrasoft.pdfmu.operation.OperationCommon;
import cz.hobrasoft.pdfmu.operation.OperationException;
import cz.hobrasoft.pdfmu.operation.args.InOutPdfArgs;
import cz.hobrasoft.pdfmu.operation.args.InPdfArgs;
import cz.hobrasoft.pdfmu.operation.args.OutPdfArgs;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * Sets the PDF version of a PDF document
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationVersionSet extends OperationCommon {

    private static final Logger logger = Logger.getLogger(OperationVersionSet.class.getName());

    private final InOutPdfArgs inout = new InOutPdfArgs(false);

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Set PDF version of a PDF document";
        String description = help + "\nIf there are signatures in the document, this operation invalidates them.";

        // Configure the subparser
        subparser.help(help)
                .description(description)
                .defaultHelp(true);

        inout.addArguments(subparser);

        String metavarVersion = "VERSION";
        subparser.addArgument("-v", "--version")
                .help(String.format("set PDF version to %s", metavarVersion))
                .metavar(metavarVersion)
                .type(PdfVersion.class)
                .setDefault(new PdfVersion("1.6"));

        subparser.addArgument("--only-if-lower")
                .help(String.format("only set version if the current version is lower than %s", metavarVersion))
                .type(boolean.class)
                .action(Arguments.storeTrue());

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        inout.setFromNamespace(namespace);
        PdfVersion outVersion = namespace.get("version");
        boolean onlyIfLower = namespace.get("only_if_lower");

        VersionSet result = execute(inout, outVersion, onlyIfLower);

        writeResult(result);
    }

    private static VersionSet execute(InOutPdfArgs inout, PdfVersion outVersion, boolean onlyIfLower) throws OperationException {
        InPdfArgs in = inout.getIn();
        OutPdfArgs out = inout.getOut();

        return execute(in, out, outVersion, onlyIfLower);
    }

    private static VersionSet execute(InPdfArgs in, OutPdfArgs out, PdfVersion outVersion, boolean onlyIfLower) throws OperationException {
        try { // in
            in.open();
            PdfReader pdfReader = in.getPdfReader();

            // Fetch the PDF version of the input PDF document
            PdfVersion inVersion = new PdfVersion(pdfReader.getPdfVersion());
            logger.info(String.format("Input PDF document version: %s", inVersion));

            // Commence to set the PDF version of the output PDF document
            // Determine the desired PDF version
            assert outVersion != null; // The argument "version" has a default value
            logger.info(String.format("Desired output PDF version: %s", outVersion));

            boolean set = true;
            if (outVersion.compareTo(inVersion) <= 0) {
                // The desired version is lower than the current version.
                if (onlyIfLower) {
                    set = false;
                    logger.info("The input PDF version is not lower than the desired version. No modification will be performed.");
                } else {
                    logger.warning("Setting the PDF version to a lower value.");
                }
            }

            if (set) {
                try { // out
                    out.open(pdfReader, false, outVersion.toChar());
                    out.close(true);
                } finally {
                    out.close(false);
                }
            }

            return new VersionSet(inVersion.toString(), outVersion.toString(), set);
        } finally {
            in.close();
        }
    }

    private static Operation instance = null;

    public static Operation getInstance() {
        if (instance == null) {
            instance = new OperationVersionSet();
        }
        return instance;
    }

    private OperationVersionSet() {
        // Singleton
    }

}
