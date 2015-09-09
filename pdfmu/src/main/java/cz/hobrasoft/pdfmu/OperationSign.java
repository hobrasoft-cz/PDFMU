package cz.hobrasoft.pdfmu;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * Adds a digital signature to a PDF document
 *
 * @author Filip
 */
public class OperationSign implements Operation {

    @Override
    public void execute(Namespace namespace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Subparser addParser(Subparsers subparsers) {
        String help = "Digitally sign a PDF document";

        String metavarIn = "IN.pdf";
        String metavarOut = "OUT.pdf";

        // Add the subparser
        Subparser subparser = subparsers.addParser("sign")
                .help(help)
                .description(help)
                .defaultHelp(true)
                .setDefault("command", OperationSign.class);

        // Add arguments to the subparser
        { // Input PDF document
            String inHelp = "input PDF document";
            ArgumentType inType = Arguments.fileType()
                    .acceptSystemIn()
                    .verifyCanRead();

            subparser.addArgument("-i", "--in")
                    .help(inHelp)
                    .metavar(metavarIn)
                    .type(inType);
            subparser.addArgument("in") // Positional alternative to "--in"
                    .help(inHelp)
                    .metavar(metavarIn)
                    .type(inType)
                    .nargs("?"); // Positional arguments are required by default
        }
        subparser.addArgument("-o", "--out")
                .help(String.format("output PDF document (default: <%s>)", metavarIn))
                .metavar(metavarOut)
                .type(Arguments.fileType().verifyCanCreate())
                .nargs("?");
        subparser.addArgument("-f", "--force")
                .help(String.format("overwrite %s if it exists", metavarOut))
                .type(boolean.class)
                .action(Arguments.storeTrue());

        return subparser;
    }

}
