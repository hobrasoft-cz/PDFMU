package cz.hobrasoft.pdfmu;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * Attaches one or more files to a PDF document
 *
 * <p>
 * Usage:
 * <ul>
 * <li>{@code pdfmu attach --to in.pdf --out out.pdf --attachment a.pdf}</li>
 * <li>{@code pdfmu attach --to in.pdf --out out.pdf --attachment a0.pdf --attachment a1.pdf}</li>
 * <li>{@code pdfmu attach --to inout.pdf --force --attachment a.pdf}</li>
 * </ul>
 *
 * @author <a href="mailto:filip.bartek@hobrasoft.cz">Filip Bartek</a>
 */
public class OperationAttach implements Operation {

    @Override
    public String getCommandName() {
        return "attach";
    }

    @Override
    public Subparser configureSubparser(Subparser subparser) {
        String help = "Attach files to a PDF document";

        subparser.help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationAttach.class);
        subparser.addArgument("-t", "--to")
                .help("PDF file to attach files to")
                .type(String.class)
                .required(true);
        subparser.addArgument("-a", "--attachment")
                .help("file to attach to TO")
                .type(String.class)
                .action(Arguments.append());

        return subparser;
    }

    @Override
    public void execute(Namespace namespace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
