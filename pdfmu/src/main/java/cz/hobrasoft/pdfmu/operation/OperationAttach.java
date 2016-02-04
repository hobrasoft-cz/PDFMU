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
package cz.hobrasoft.pdfmu.operation;

import com.itextpdf.text.pdf.PdfStamper;
import cz.hobrasoft.pdfmu.PdfmuUtils;
import static cz.hobrasoft.pdfmu.error.ErrorType.ATTACH_ATTACHMENT_EQUALS_OUTPUT;
import static cz.hobrasoft.pdfmu.error.ErrorType.ATTACH_FAIL;
import cz.hobrasoft.pdfmu.jackson.EmptyResult;
import cz.hobrasoft.pdfmu.operation.args.InOutPdfArgs;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * Attaches one or more files to a PDF document
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationAttach extends OperationCommon {

    private static final Logger logger = Logger.getLogger(OperationAttach.class.getName());

    private final String metavarIn = "IN.pdf";
    private final InOutPdfArgs inout = new InOutPdfArgs(metavarIn);

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Attach files to a PDF document";

        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationAttach.class);

        inout.addArguments(subparser);

        String metavarAttachment = "ATTACHMENT";

        // TODO: Try to reuse InPdfArgs
        subparser.addArgument("attachment")
                .help(String.format("file to attach to %s", metavarIn))
                .metavar(metavarAttachment)
                .type(Arguments.fileType().acceptSystemIn());
        subparser.addArgument("-r", "--rename")
                .help(String.format("attachment filename shown in the output PDF document (default: <%s>)", metavarAttachment))
                .metavar("FILENAME")
                .type(String.class);
        subparser.addArgument("-d", "--description")
                .help("attachment file description shown in the output PDF document (default: <none>)")
                .type(String.class);

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) throws OperationException {
        inout.setFromNamespace(namespace);

        File file = namespace.get("attachment");

        File outFile = inout.getOut().getFile();
        assert outFile != null;
        if (outFile.equals(file)) {
            throw new OperationException(ATTACH_ATTACHMENT_EQUALS_OUTPUT,
                    PdfmuUtils.sortedMap(
                            new SimpleEntry<String, Object>("outputFile", outFile),
                            new SimpleEntry<String, Object>("attachmentFile", file)));
        }

        String description = namespace.getString("description");
        String fileDisplay = namespace.getString("rename");

        if (fileDisplay == null) {
            fileDisplay = file.getName();
        }

        inout.open();

        execute(inout.getPdfStamper(), description, file.getPath(), fileDisplay);

        inout.close();

        writeResult(new EmptyResult());
    }

    private static final Pattern filenameWithExtension = Pattern.compile(".*\\.[^\\.]+");

    private static void execute(PdfStamper stp, String description, String file, String fileDisplay) throws OperationException {
        {
            assert stp != null;
            assert file != null;
            assert fileDisplay != null; // We use the attachment file name by default

            logger.info(String.format("Attached file: %s", file));
            logger.info(String.format("Description: %s", (description != null ? description : "<none>")));
            logger.info(String.format("Display name: %s", (fileDisplay != null ? fileDisplay : "<none>")));
            {
                if (fileDisplay == null) {
                    logger.warning("Display name has not been set. Adobe Reader XI does not allow opening or saving such attachment.");
                } else {
                    Matcher m = filenameWithExtension.matcher(fileDisplay);
                    if (!m.matches()) {
                        logger.warning("Display name does not contain a file extension. Adobe Reader XI does not allow opening or saving such attachment.");
                    }
                }
            }
        }
        try {
            stp.addFileAttachment(description, null, file, fileDisplay);
        } catch (IOException ex) {
            throw new OperationException(ATTACH_FAIL, ex,
                    PdfmuUtils.sortedMap(new String[]{"file"}, new Object[]{file}));
        }
        logger.info(String.format("The file \"%s\" has been attached.", file));
    }

    private static Operation instance = null;

    public static Operation getInstance() {
        if (instance == null) {
            instance = new OperationAttach();
        }
        return instance;
    }

    private OperationAttach() {
        // Singleton
    }

}
